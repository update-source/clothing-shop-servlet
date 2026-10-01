package com.shop.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.shop.dao.Daos;
import com.shop.model.account.Customer;
import com.shop.model.catalog.Product;
import com.shop.model.catalog.ProductVariant;
import com.shop.model.order.Order;
import com.shop.model.order.OrderStatus;
import com.shop.model.order.PaymentMethod;
import com.shop.persistence.DataSeeder;
import com.shop.persistence.Jdbc;
import com.shop.persistence.TestDatabase;
import com.shop.persistence.Tx;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** Đơn VNPAY quá hạn thanh toán tự huỷ: nhả hàng đang giữ để người khác mua được. */
class PaymentExpiryIntegrationTest {

    @BeforeAll
    static void start() {
        TestDatabase.start();
        DataSeeder.seedIfEmpty();
    }

    @AfterAll
    static void stop() {
        TestDatabase.stop();
    }

    @Test
    void overdueUnpaidVnpayOrderIsCancelledAndStockReleased() {
        long orderId = Tx.inTransaction(() -> {
            Customer lan = (Customer) Daos.users().findByUsername("khachhang").orElseThrow();
            Product jacket = Daos.products().findAllForAdmin("Áo khoác gió").get(0);
            ProductVariant blackM = jacket.findVariant("M", "Đen").orElseThrow();
            lan.getCart().addItem(blackM, 2);
            Order order = lan.checkout(lan.getAddressBook().getDefault(), PaymentMethod.VNPAY, null,
                    new BigDecimal("25000"), null);
            order.pay();
            Daos.orders().insert(order);
            Daos.carts().save(lan.getId(), lan.getCart());
            return order.getId();
        });
        Tx.inTransaction(() -> Jdbc.update("UPDATE orders SET order_date = ? WHERE id = ?",
                LocalDateTime.now().minusHours(1), orderId));
        Tx.release();

        int cancelled = Tx.withConnection(() -> new PaymentService().cancelOverdueVnpayOrders());
        assertTrue(cancelled >= 1);

        Order reloaded = Tx.withConnection(() -> Daos.orders().findById(orderId).orElseThrow());
        assertEquals(OrderStatus.CANCELLED, reloaded.getStatus());
        assertEquals(0, reloaded.getDetails().get(0).getVariant().getReservedQuantity());
    }
}
