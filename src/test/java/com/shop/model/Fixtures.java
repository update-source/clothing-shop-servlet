package com.shop.model;

import com.shop.model.account.Address;
import com.shop.model.account.AddressBook;
import com.shop.model.account.Customer;
import com.shop.model.account.CustomerLevel;
import com.shop.model.account.Employee;
import com.shop.model.account.EmployeeRole;
import com.shop.model.account.Gender;
import com.shop.model.catalog.Cart;
import com.shop.model.catalog.Category;
import com.shop.model.catalog.Product;
import com.shop.model.catalog.ProductVariant;
import com.shop.model.discount.DiscountPolicy;
import com.shop.model.discount.Period;
import com.shop.model.discount.Voucher;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicLong;

/** Dựng nhanh các đối tượng nghiệp vụ cho unit test (có id như đã lưu). */
public final class Fixtures {

    private static final AtomicLong SEQ = new AtomicLong(1);
    private static final String HASH = com.shop.util.PasswordHasher.hash("secret1");

    private Fixtures() {
    }

    public static long nextId() {
        return SEQ.getAndIncrement();
    }

    public static Product product(String name, String price) {
        Category category = new Category(nextId(), "Áo thun", null);
        return new Product(nextId(), name, "Mô tả", new BigDecimal(price), true, category, "images/cloth_1.jpg",
                Lazy.of(new ArrayList<>()), Lazy.of(new ArrayList<>()), Lazy.of(new ArrayList<>()));
    }

    public static ProductVariant variant(Product product, String size, String color, int stock) {
        ProductVariant v = product.addVariant(size, color, stock);
        v.assignId(nextId());
        return v;
    }

    public static Customer customer() {
        long id = nextId();
        return new Customer(id, "khach" + id, HASH, "Khách " + id, Gender.FEMALE,
                LocalDate.of(2000, 1, 1), "khach" + id + "@mail.vn", "0901234567", true,
                Lazy.of(new AddressBook()), Lazy.of(new Cart()),
                Lazy.of(new ArrayList<>()), Lazy.of(new ArrayList<>()));
    }

    public static Employee staff() {
        long id = nextId();
        return new Employee(id, "staff" + id, HASH, "Nhân viên", Gender.MALE, null,
                "staff" + id + "@shop.vn", "0911111111", true, LocalDate.now(), EmployeeRole.STAFF);
    }

    public static Address address() {
        return new Address("Nguyễn Văn A", "0901234567", "12 Lê Lợi", "Phường Bến Thành", "TP. Hồ Chí Minh");
    }

    public static Period ongoing() {
        LocalDateTime now = LocalDateTime.now();
        return new Period(now.minusDays(1), now.plusDays(1));
    }

    public static Voucher voucher(DiscountPolicy policy, String minOrder, int usageLimit, int perCustomer) {
        Voucher v = Voucher.create("SALE10", EnumSet.allOf(CustomerLevel.class), new BigDecimal(minOrder),
                usageLimit, perCustomer, policy, ongoing());
        v.assignId(nextId());
        return v;
    }

    public static BigDecimal vnd(String amount) {
        return new BigDecimal(amount);
    }
}
