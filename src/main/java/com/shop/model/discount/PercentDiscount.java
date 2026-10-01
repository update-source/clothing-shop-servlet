package com.shop.model.discount;

import com.shop.model.DomainException;
import com.shop.util.Money;
import java.math.BigDecimal;
import java.math.RoundingMode;

/** Giảm theo phần trăm, có mức giảm tối đa (null = không giới hạn). */
public final class PercentDiscount implements DiscountPolicy {

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private final BigDecimal percent;
    private final BigDecimal maxDiscount;

    public PercentDiscount(BigDecimal percent, BigDecimal maxDiscount) {
        if (percent == null || percent.signum() <= 0 || percent.compareTo(HUNDRED) > 0) {
            throw new DomainException("Phần trăm giảm phải trong khoảng (0, 100]");
        }
        if (maxDiscount != null && maxDiscount.signum() <= 0) {
            throw new DomainException("Mức giảm tối đa phải lớn hơn 0");
        }
        this.percent = percent;
        this.maxDiscount = maxDiscount;
    }

    @Override
    public BigDecimal calculate(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal discount = amount.multiply(percent).divide(HUNDRED, 0, RoundingMode.HALF_UP);
        if (maxDiscount != null) {
            discount = discount.min(maxDiscount);
        }
        return discount.min(amount);
    }

    @Override
    public String describe() {
        String text = "Giảm " + percent.stripTrailingZeros().toPlainString() + "%";
        return maxDiscount == null ? text : text + " (tối đa " + Money.format(maxDiscount) + ")";
    }

    public BigDecimal getPercent() {
        return percent;
    }

    public BigDecimal getMaxDiscount() {
        return maxDiscount;
    }
}
