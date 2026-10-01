package com.shop.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.shop.dao.Daos;
import com.shop.dao.Page;
import com.shop.dao.ProductFilter;
import com.shop.model.DomainException;
import com.shop.model.account.Customer;
import com.shop.model.account.CustomerLevel;
import com.shop.model.account.Employee;
import com.shop.model.catalog.Product;
import com.shop.model.catalog.ProductVariant;
import com.shop.model.discount.Voucher;
import com.shop.model.order.Order;
import com.shop.model.order.OrderStatus;
import com.shop.model.order.PaymentMethod;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/** Kiểm tra DAO trên H2: dữ liệu mẫu, nạp lại đối tượng và các bảo vệ khi ghi đồng thời. */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PersistenceIntegrationTest {

    @BeforeAll
    static void start() {
        TestDatabase.start();
        DataSeeder.seedIfEmpty();
    }

    @AfterAll
    static void stop() {
        TestDatabase.stop();
    }

    @AfterEach
    void releaseConnection() {
        Tx.release();
    }

    private static Customer lan() {
        return (Customer) Daos.users().findByUsername("khachhang").orElseThrow();
    }

    private static Product product(String name) {
        return Daos.products().findAllForAdmin(name).get(0);
    }

    @Test
    @org.junit.jupiter.api.Order(1)
    void seededCustomerHasHistoryAndReview() {
        Customer lan = lan();
        assertTrue(lan.verifyPassword("123456"));
        assertEquals(2, lan.getAddressBook().getAddresses().size());
        assertEquals(1, lan.getOrders().size());
        assertEquals(OrderStatus.COMPLETED, lan.getOrders().get(0).getStatus());
        assertTrue(lan.getOrders().get(0).isPaid());
        assertEquals(CustomerLevel.CASUAL, lan.getLevel());

        Product basic = product("Áo thun basic");
        assertTrue(lan.hasPurchased(basic));
        assertTrue(basic.hasReviewBy(lan));
        assertEquals(5.0, basic.getAverageRating());
        assertEquals(6, basic.getVariants().size());
        assertEquals(34, basic.getTotalStock(), "36 trong kho trừ 2 cái đã bán");
    }

    @Test
    @org.junit.jupiter.api.Order(2)
    void promotionsAreLoadedForPricing() {
        Product basic = product("Áo thun basic");
        // 199.000 - 10% (19.900) = 179.100
        assertEquals(0, basic.getFinalPrice(LocalDateTime.now()).compareTo(new BigDecimal("179100")));
        Product shoe = product("Giày sneaker trắng");
        assertEquals(0, shoe.getFinalPrice(LocalDateTime.now()).compareTo(new BigDecimal("799000")));
    }

    @Test
    @org.junit.jupiter.api.Order(3)
    void searchFiltersByCategoryTreeSizeAndPrice() {
        long aoId = Daos.categories().findAll().stream().filter(c -> c.getName().equals("Áo"))
                .findFirst().orElseThrow().getId();
        Set<Long> ao = Daos.categories().selfAndDescendantIds(aoId);
        Page<Product> shirts = Daos.products().search(new ProductFilter(ao, null, null, null,
                null, null, "newest", true, 1, 50));
        assertEquals(6, shirts.total());

        Page<Product> cheapXl = Daos.products().search(new ProductFilter(null, null, Set.of("XL"), null,
                null, new BigDecimal("300000"), "price_asc", true, 1, 50));
        assertTrue(cheapXl.items().stream().allMatch(p -> p.getSizes().contains("XL")));
        assertEquals("Áo ba lỗ thể thao", cheapXl.items().get(0).getName());
    }

    @Test
    @org.junit.jupiter.api.Order(6)
    void checkoutPersistsReservationVoucherAndClearsCart() {
        Tx.inTransaction(() -> {
            Customer lan = lan();
            Product polo = product("Áo polo cổ bẻ");
            ProductVariant navyM = polo.findVariant("M", "Xanh navy").orElseThrow();
            lan.getCart().addItem(navyM, 2);
            Daos.carts().save(lan.getId(), lan.getCart());
        });
        Tx.release();

        Order placed = Tx.inTransaction(() -> {
            Customer lan = lan();
            assertEquals(2, lan.getCart().getItemCount());
            Voucher sale10 = Daos.vouchers().findByCode("sale10").orElseThrow();
            Order order = lan.checkout(lan.getAddressBook().getDefault(), PaymentMethod.VNPAY, sale10,
                    new BigDecimal("25000"), null);
            order.pay();
            Daos.orders().insert(order);
            Daos.carts().save(lan.getId(), lan.getCart());
            return order;
        });
        Tx.release();

        Order reloaded = Daos.orders().findById(placed.getId()).orElseThrow();
        assertEquals(OrderStatus.PENDING, reloaded.getStatus());
        assertEquals(2, reloaded.getDetails().get(0).getVariant().getReservedQuantity());
        assertEquals(1, reloaded.getVoucher().getUsedCount());
        assertEquals(0, Daos.carts().countItems(reloaded.getCustomer().getId()));
        assertEquals(0, reloaded.getTotal().compareTo(placed.getTotal()));

        // Huỷ đơn: nhả hàng giữ và trả lượt voucher
        Tx.inTransaction(() -> {
            Order o = Daos.orders().findById(placed.getId()).orElseThrow();
            o.cancel();
            Daos.orders().update(o);
        });
        Tx.release();
        Order cancelled = Daos.orders().findById(placed.getId()).orElseThrow();
        assertEquals(OrderStatus.CANCELLED, cancelled.getStatus());
        assertEquals(0, cancelled.getDetails().get(0).getVariant().getReservedQuantity());
        assertEquals(0, cancelled.getVoucher().getUsedCount());
    }

    @Test
    @org.junit.jupiter.api.Order(5)
    void staleStockSnapshotCannotOversell() {
        // Kịch bản B ở tầng lưu trữ: hai người cùng nạp biến thể còn đúng 1 cái.
        Product shorts = product("Giày chạy bộ nhẹ");
        ProductVariant first = shorts.findVariant("42", "Đen").orElseThrow();
        ProductVariant second = product("Giày chạy bộ nhẹ").findVariant("42", "Đen").orElseThrow();
        first.reserve(2);
        Daos.products().saveVariant(first);
        second.reserve(1);
        assertThrows(DomainException.class, () -> Daos.products().saveVariant(second));
    }

    @Test
    @org.junit.jupiter.api.Order(7)
    void concurrentStatusChangeIsRejected() {
        Order a = Daos.orders().findByStatus(OrderStatus.COMPLETED).get(0);
        Order b = Daos.orders().findById(a.getId()).orElseThrow();
        Tx.inTransaction(() -> {
            a.returnGoods();
            Daos.orders().update(a);
        });
        b.returnGoods();
        assertThrows(DomainException.class, () -> Tx.inTransaction(() -> Daos.orders().update(b)));
    }

    @Test
    @org.junit.jupiter.api.Order(4)
    void employeesAreLoadedWithRoles() {
        List<Employee> staff = Daos.users().search("EMPLOYEE", null).stream().map(u -> (Employee) u).toList();
        assertEquals(2, staff.size());
        assertTrue(staff.stream().anyMatch(Employee::isAdmin));
        assertFalse(Daos.users().existsEmail("new@mail.vn", null));
    }
}
