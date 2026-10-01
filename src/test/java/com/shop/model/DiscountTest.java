package com.shop.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.shop.model.account.Customer;
import com.shop.model.account.CustomerLevel;
import com.shop.model.catalog.Product;
import com.shop.model.discount.FixedAmountDiscount;
import com.shop.model.discount.PercentDiscount;
import com.shop.model.discount.Period;
import com.shop.model.discount.Promotion;
import com.shop.model.discount.Voucher;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.EnumSet;
import org.junit.jupiter.api.Test;

class DiscountTest {

    @Test
    void percentDiscountIsCappedAtMaxDiscount() {
        PercentDiscount tenPercentMax50k = new PercentDiscount(Fixtures.vnd("10"), Fixtures.vnd("50000"));
        assertEquals(0, tenPercentMax50k.calculate(Fixtures.vnd("300000")).compareTo(Fixtures.vnd("30000")));
        assertEquals(0, tenPercentMax50k.calculate(Fixtures.vnd("900000")).compareTo(Fixtures.vnd("50000")));
    }

    @Test
    void fixedDiscountNeverExceedsAmount() {
        FixedAmountDiscount minus30k = new FixedAmountDiscount(Fixtures.vnd("30000"));
        assertEquals(0, minus30k.calculate(Fixtures.vnd("100000")).compareTo(Fixtures.vnd("30000")));
        assertEquals(0, minus30k.calculate(Fixtures.vnd("20000")).compareTo(Fixtures.vnd("20000")));
    }

    @Test
    void periodContainsItsBoundaries() {
        LocalDateTime start = LocalDateTime.of(2026, 11, 11, 0, 0);
        Period p = new Period(start, start.plusDays(1));
        assertTrue(p.contains(start));
        assertTrue(p.contains(start.plusDays(1)));
        assertFalse(p.contains(start.minusSeconds(1)));
        assertThrows(DomainException.class, () -> new Period(start, start));
    }

    @Test
    void productTakesHighestOngoingDiscount() {
        Product shirt = Fixtures.product("Áo thun", "200000");
        LocalDateTime now = LocalDateTime.now();
        Promotion small = Promotion.create("Giảm 10%", new PercentDiscount(Fixtures.vnd("10"), null), Fixtures.ongoing());
        Promotion big = Promotion.create("Giảm 50k", new FixedAmountDiscount(Fixtures.vnd("50000")), Fixtures.ongoing());
        Promotion expired = Promotion.create("Đã hết", new FixedAmountDiscount(Fixtures.vnd("150000")),
                new Period(now.minusDays(10), now.minusDays(5)));
        small.addProduct(shirt);
        big.addProduct(shirt);
        expired.addProduct(shirt);
        assertEquals(0, shirt.getFinalPrice(now).compareTo(Fixtures.vnd("150000")));
        assertTrue(big.includes(shirt));
        assertEquals(3, shirt.getPromotions().size());
        big.removeProduct(shirt);
        assertEquals(0, shirt.getFinalPrice(now).compareTo(Fixtures.vnd("180000")));
    }

    @Test
    void voucherValidityChecksActivePeriodAndUsage() {
        Voucher v = Fixtures.voucher(new FixedAmountDiscount(Fixtures.vnd("30000")), "0", 1, 1);
        LocalDateTime now = LocalDateTime.now();
        assertTrue(v.isValid(now));
        assertFalse(v.isValid(now.plusDays(3)));
        v.redeem();
        assertFalse(v.isValid(now));
        assertThrows(DomainException.class, v::redeem);
        v.release();
        v.deactivate();
        assertFalse(v.isValid(now));
    }

    @Test
    void voucherApplicabilityChecksTierAndMinimum() {
        Customer casual = Fixtures.customer();
        Voucher loyalOnly = Voucher.create("VIP50", EnumSet.of(CustomerLevel.LOYAL), Fixtures.vnd("0"),
                10, 1, new FixedAmountDiscount(Fixtures.vnd("50000")), Fixtures.ongoing());
        assertFalse(loyalOnly.isApplicable(casual, Fixtures.vnd("1000000")));
        assertNotNull(loyalOnly.whyNotUsable(casual, Fixtures.vnd("1000000"), LocalDateTime.now()));

        Voucher min300k = Fixtures.voucher(new FixedAmountDiscount(Fixtures.vnd("30000")), "300000", 10, 1);
        assertFalse(min300k.isApplicable(casual, Fixtures.vnd("299000")));
        assertTrue(min300k.isApplicable(casual, Fixtures.vnd("300000")));
        assertNull(min300k.whyNotUsable(casual, Fixtures.vnd("300000"), LocalDateTime.now()));
    }

    @Test
    void customerLevelFollowsSpendingThresholds() {
        assertEquals(CustomerLevel.CASUAL, CustomerLevel.fromSpent(BigDecimal.ZERO));
        assertEquals(CustomerLevel.REGULAR, CustomerLevel.fromSpent(Fixtures.vnd("2000000")));
        assertEquals(CustomerLevel.LOYAL, CustomerLevel.fromSpent(Fixtures.vnd("15000000")));
    }

    @Test
    void invalidVoucherCodeRejected() {
        assertThrows(DomainException.class, () -> Voucher.create("a b", EnumSet.allOf(CustomerLevel.class),
                BigDecimal.ZERO, 1, 1, new FixedAmountDiscount(BigDecimal.TEN), Fixtures.ongoing()));
    }
}
