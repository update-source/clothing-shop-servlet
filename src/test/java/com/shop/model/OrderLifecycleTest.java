package com.shop.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.shop.model.account.Customer;
import com.shop.model.account.Employee;
import com.shop.model.catalog.Product;
import com.shop.model.catalog.ProductVariant;
import com.shop.model.discount.PercentDiscount;
import com.shop.model.discount.Voucher;
import com.shop.model.order.Order;
import com.shop.model.order.OrderStatus;
import com.shop.model.order.Payment;
import com.shop.model.order.PaymentMethod;
import com.shop.model.order.PaymentStatus;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

/** Các kịch bản tiêu biểu A, C, D trong đặc tả và các quy tắc vòng đời đơn. */
class OrderLifecycleTest {

    private static final BigDecimal SHIP = Fixtures.vnd("30000");

    private final Product shirt = Fixtures.product("Áo thun basic", "200000");
    private final ProductVariant blackM = Fixtures.variant(shirt, "M", "Đen", 10);
    private final Customer customer = Fixtures.customer();
    private final Employee staff = Fixtures.staff();
    private final Voucher sale10 = Fixtures.voucher(
            new PercentDiscount(Fixtures.vnd("10"), Fixtures.vnd("50000")), "0", 100, 2);

    private Order place(PaymentMethod method, Voucher voucher) {
        customer.getCart().addItem(blackM, 2);
        Order order = customer.checkout(Fixtures.address(), method, voucher, SHIP, "Giao giờ hành chính");
        order.assignId(Fixtures.nextId());
        order.pay();
        return order;
    }

    @Test
    void scenarioA_vnpayWithVoucher() {
        Order order = place(PaymentMethod.VNPAY, sale10);

        assertEquals(OrderStatus.PENDING, order.getStatus());
        assertEquals(2, blackM.getReservedQuantity());
        assertEquals(1, sale10.getUsedCount());
        assertTrue(customer.getCart().isEmpty());
        // 400.000 - 40.000 (10%) + 30.000 phí ship
        assertEquals(0, order.getTotal().compareTo(Fixtures.vnd("390000")));

        assertThrows(DomainException.class, () -> order.confirm(staff));
        order.findPayment(PaymentStatus.UNPAID).orElseThrow().markPaid("VNP123");
        assertTrue(order.isPaid());

        order.confirm(staff);
        assertEquals(8, blackM.getStockQuantity());
        assertEquals(0, blackM.getReservedQuantity());
        assertEquals(staff, order.getHandledBy());

        order.ship(staff);
        order.complete();
        assertEquals(OrderStatus.COMPLETED, order.getStatus());
        assertEquals(0, customer.getTotalSpent().compareTo(Fixtures.vnd("390000")));
        assertTrue(customer.hasPurchased(shirt));
    }

    @Test
    void scenarioC_cancelConfirmedCodOrder() {
        Order order = place(PaymentMethod.COD, sale10);
        order.confirm(staff);
        assertEquals(8, blackM.getStockQuantity());

        order.cancel();
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        assertEquals(10, blackM.getStockQuantity());
        assertEquals(0, sale10.getUsedCount());
        assertEquals(PaymentStatus.UNPAID, order.getPayments().get(0).getStatus());
    }

    @Test
    void scenarioD_codCustomerRefusesDelivery() {
        Order order = place(PaymentMethod.COD, sale10);
        order.confirm(staff);
        order.ship(staff);

        order.failDelivery();
        assertEquals(OrderStatus.DELIVERY_FAILED, order.getStatus());
        assertEquals(10, blackM.getStockQuantity());
        assertEquals(0, sale10.getUsedCount());
        assertEquals(PaymentStatus.UNPAID, order.getPayments().get(0).getStatus());
    }

    @Test
    void cancelPendingReleasesReservationAndRefundsPaidVnpay() {
        Order order = place(PaymentMethod.VNPAY, null);
        Payment payment = order.findPayment(PaymentStatus.UNPAID).orElseThrow();
        payment.markPaid("VNP1");

        order.cancel();
        assertEquals(0, blackM.getReservedQuantity());
        assertEquals(10, blackM.getStockQuantity());
        assertEquals(PaymentStatus.REFUNDED, payment.getStatus());
        assertTrue(order.isRefunded());
        assertFalse(order.isPaid());
    }

    @Test
    void codCompletionMarksPaymentPaid() {
        Order order = place(PaymentMethod.COD, null);
        order.confirm(staff);
        order.ship(staff);
        order.complete();
        assertTrue(order.isPaid());
    }

    @Test
    void returnGoodsRestocksAndRefunds() {
        Order order = place(PaymentMethod.COD, null);
        order.confirm(staff);
        order.ship(staff);
        order.complete();

        order.returnGoods();
        assertEquals(OrderStatus.RETURNED, order.getStatus());
        assertEquals(10, blackM.getStockQuantity());
        assertEquals(PaymentStatus.REFUNDED, order.getPayments().get(0).getStatus());
        assertEquals(0, customer.getTotalSpent().signum());
    }

    @Test
    void invalidTransitionsAreRejected() {
        Order order = place(PaymentMethod.COD, null);
        assertThrows(DomainException.class, () -> order.ship(staff));
        assertThrows(DomainException.class, order::complete);
        order.confirm(staff);
        order.ship(staff);
        assertThrows(DomainException.class, order::cancel);
        assertFalse(order.canCancel());
    }

    @Test
    void retryingVnpayFailsAbandonedAttempt() {
        Order order = place(PaymentMethod.VNPAY, null);
        Payment first = order.getPayments().get(0);
        Payment second = order.pay();
        assertEquals(PaymentStatus.FAILED, first.getStatus());
        assertEquals(PaymentStatus.UNPAID, second.getStatus());
    }

    @Test
    void overdueVnpayOrderIsDetected() {
        Order order = place(PaymentMethod.VNPAY, null);
        assertFalse(order.isPaymentOverdue(LocalDateTime.now(), Duration.ofMinutes(15)));
        assertTrue(order.isPaymentOverdue(LocalDateTime.now().plusMinutes(16), Duration.ofMinutes(15)));
    }

    @Test
    void perCustomerVoucherLimitIsFreedByCancellation() {
        Voucher once = Fixtures.voucher(new PercentDiscount(Fixtures.vnd("5"), null), "0", 100, 1);
        Order first = place(PaymentMethod.COD, once);
        assertEquals(1, customer.countVoucherUsage(once));

        customer.getCart().addItem(blackM, 1);
        assertThrows(DomainException.class,
                () -> customer.checkout(Fixtures.address(), PaymentMethod.COD, once, SHIP, null));

        first.cancel();
        assertEquals(0, customer.countVoucherUsage(once));
    }

    @Test
    void orderKeepsItsOwnCopyOfShippingAddress() {
        Order order = place(PaymentMethod.COD, null);
        assertEquals(Fixtures.address(), order.getShippingAddress());
        assertNull(order.getShippingAddress().getId());
    }
}
