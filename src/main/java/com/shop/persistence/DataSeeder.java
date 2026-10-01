package com.shop.persistence;

import com.shop.dao.Daos;
import com.shop.model.account.Address;
import com.shop.model.account.Customer;
import com.shop.model.account.CustomerLevel;
import com.shop.model.account.Employee;
import com.shop.model.account.EmployeeRole;
import com.shop.model.account.Gender;
import com.shop.model.catalog.Category;
import com.shop.model.catalog.Product;
import com.shop.model.catalog.Review;
import com.shop.model.discount.FixedAmountDiscount;
import com.shop.model.discount.PercentDiscount;
import com.shop.model.discount.Period;
import com.shop.model.discount.Promotion;
import com.shop.model.discount.Voucher;
import com.shop.model.order.Order;
import com.shop.model.order.PaymentMethod;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumSet;

/**
 * Tạo dữ liệu mẫu khi CSDL còn trống. Mọi thứ được tạo qua chính các hành vi của đối tượng nghiệp vụ
 * (đăng ký, thêm biến thể, đặt hàng, xác nhận, giao, đánh giá) rồi mới lưu.
 * <p>
 * Tài khoản mẫu: admin / admin123 (ADMIN), staff / staff123 (STAFF), khachhang / 123456 (khách).
 */
public final class DataSeeder {

    private DataSeeder() {
    }

    public static void seedIfEmpty() {
        Tx.withConnection(() -> {
            if (Jdbc.count("SELECT COUNT(*) FROM users") == 0) {
                Tx.inTransaction(DataSeeder::seed);
            }
            return null;
        });
    }

    private static void seed() {
        // ---- Tài khoản
        Employee admin = Employee.hire("admin", "admin123", "Quản trị viên", Gender.MALE, null,
                "admin@shop.vn", "0900000001", EmployeeRole.ADMIN, LocalDate.now().minusYears(2));
        Employee staff = Employee.hire("staff", "staff123", "Trần Văn Bình", Gender.MALE, null,
                "staff@shop.vn", "0900000002", EmployeeRole.STAFF, LocalDate.now().minusMonths(6));
        Customer lan = Customer.register("khachhang", "123456", "Nguyễn Thị Lan", Gender.FEMALE,
                LocalDate.of(2001, 5, 20), "lan.nguyen@example.com", "0901234567");
        Daos.users().insert(admin);
        Daos.users().insert(staff);
        Daos.users().insert(lan);
        lan.getAddressBook().add(new Address("Nguyễn Thị Lan", "0901234567", "1 Võ Văn Ngân",
                "Phường Thủ Đức", "TP. Hồ Chí Minh"));
        lan.getAddressBook().add(new Address("Nguyễn Thị Lan", "0901234567", "25 Lý Thường Kiệt",
                "Phường Cửa Nam", "Hà Nội"));
        Daos.addresses().saveBook(lan.getId(), lan.getAddressBook());

        // ---- Danh mục
        Category ao = category("Áo", null);
        Category aoThun = category("Áo thun", ao);
        Category aoPolo = category("Áo polo", ao);
        Category aoSoMi = category("Áo sơ mi", ao);
        Category aoKhoac = category("Áo khoác", ao);
        Category quan = category("Quần", null);
        Category quanJean = category("Quần jean", quan);
        Category quanShort = category("Quần short", quan);
        Category giay = category("Giày dép", null);
        Category sneaker = category("Sneaker", giay);

        // ---- Sản phẩm & biến thể (tồn kho "Áo thun basic" lấy theo bảng ví dụ trong đặc tả)
        Product basic = product("Áo thun basic", "Áo thun cotton 100% co giãn 4 chiều, form regular, "
                + "thấm hút mồ hôi tốt — món đồ cơ bản không thể thiếu trong tủ.", "199000", aoThun,
                "images/cloth_3.jpg", "S:Đen:12", "M:Đen:0", "L:Đen:7", "S:Trắng:5", "M:Trắng:9", "L:Trắng:3");
        product("Áo ba lỗ thể thao", "Áo tank top vải mè thoáng khí, phù hợp tập gym và chạy bộ.", "149000",
                aoThun, "images/cloth_1.jpg", "M:Xám:15", "L:Xám:10", "XL:Xám:6", "M:Đen:8", "L:Đen:12");
        Product polo = product("Áo polo cổ bẻ", "Áo polo vải cá sấu cao cấp, cổ dệt bo, giữ form sau nhiều lần giặt.",
                "299000", aoPolo, "images/cloth_2.jpg", "S:Xanh navy:6", "M:Xanh navy:10", "L:Xanh navy:8",
                "XL:Xanh navy:4", "M:Trắng:7", "L:Trắng:5");
        product("Áo thun oversize in chữ", "Form rộng thoải mái, hình in lụa bền màu, chất cotton dày dặn.",
                "259000", aoThun, "images/cloth_1.jpg", "M:Trắng:10", "L:Trắng:10", "M:Be:6", "L:Be:4");
        product("Áo sơ mi Oxford dài tay", "Vải Oxford dày dặn, ít nhăn, phù hợp đi làm lẫn đi chơi.", "389000",
                aoSoMi, "images/cloth_2.jpg", "S:Xanh nhạt:5", "M:Xanh nhạt:9", "L:Xanh nhạt:6", "M:Trắng:8");
        product("Áo khoác gió chống nước", "Lớp vải dù chống nước nhẹ, có mũ trùm, gấp gọn bỏ túi.", "549000",
                aoKhoac, "images/cloth_3.jpg", "M:Đen:7", "L:Đen:5", "XL:Đen:3", "L:Xanh rêu:4");
        product("Quần jean slim fit", "Denim co giãn nhẹ, ống côn vừa phải, wash màu xanh cổ điển.", "459000",
                quanJean, "images/men.jpg", "29:Xanh đậm:6", "30:Xanh đậm:8", "31:Xanh đậm:8", "32:Xanh đậm:5");
        product("Quần short kaki", "Kaki mềm, lưng chun phối dây rút, thoải mái cho ngày hè.", "229000",
                quanShort, "images/women.jpg", "M:Be:10", "L:Be:8", "M:Đen:6", "L:Đen:6");
        Product shoe = product("Giày sneaker trắng", "Đế cao su đúc nguyên khối, êm chân, dễ phối đồ.", "899000",
                sneaker, "images/shoe_1.jpg", "39:Trắng:5", "40:Trắng:8", "41:Trắng:6", "42:Trắng:3");
        product("Giày chạy bộ nhẹ", "Thân lưới thoáng khí, đế EVA nhẹ, đệm êm cho quãng đường dài.", "1099000",
                sneaker, "images/shoe.png", "40:Đen:4", "41:Đen:6", "42:Đen:2");

        // ---- Khuyến mãi (tự áp lên giá sản phẩm)
        LocalDateTime now = LocalDateTime.now();
        Promotion october = Promotion.create("Sale tháng 10 — giảm 10%",
                new PercentDiscount(new BigDecimal("10"), new BigDecimal("50000")),
                new Period(now.minusDays(1), now.plusDays(60)));
        october.addProduct(basic);
        october.addProduct(polo);
        Daos.promotions().insert(october);
        Promotion flash = Promotion.create("Flash sale giày — giảm 100.000 ₫",
                new FixedAmountDiscount(new BigDecimal("100000")), new Period(now.minusDays(1), now.plusDays(15)));
        flash.addProduct(shoe);
        Daos.promotions().insert(flash);

        // ---- Voucher (khách tự nhập)
        Daos.vouchers().insert(Voucher.create("WELCOME30K", EnumSet.allOf(CustomerLevel.class),
                new BigDecimal("199000"), 1000, 1, new FixedAmountDiscount(new BigDecimal("30000")),
                new Period(now.minusDays(1), now.plusYears(1))));
        Daos.vouchers().insert(Voucher.create("SALE10", EnumSet.allOf(CustomerLevel.class),
                new BigDecimal("300000"), 200, 3,
                new PercentDiscount(new BigDecimal("10"), new BigDecimal("50000")),
                new Period(now.minusDays(1), now.plusMonths(3))));
        Daos.vouchers().insert(Voucher.create("VIP100K", EnumSet.of(CustomerLevel.REGULAR, CustomerLevel.LOYAL),
                new BigDecimal("1000000"), 100, 2, new FixedAmountDiscount(new BigDecimal("100000")),
                new Period(now.minusDays(1), now.plusMonths(6))));

        // ---- Một đơn đã hoàn tất của khách mẫu (để có lịch sử mua và đánh giá)
        lan.getCart().addItem(basic.findVariant("M", "Trắng").orElseThrow(), 2);
        Order order = lan.checkout(lan.getAddressBook().getDefault(), PaymentMethod.COD, null,
                new BigDecimal("25000"), "Giao giờ hành chính");
        order.pay();
        Daos.orders().insert(order);
        order.confirm(staff);
        order.ship(staff);
        order.complete();
        Daos.orders().update(order);
        Review review = lan.writeReview(basic, 5, "Vải mát, mặc đúng size, giao hàng nhanh. Sẽ ủng hộ tiếp!");
        Daos.reviews().insert(review, basic.getId());
    }

    private static Category category(String name, Category parent) {
        Category c = Category.create(name, parent);
        Daos.categories().save(c);
        return c;
    }

    /** {@code variants}: "size:màu:tồn kho". */
    private static Product product(String name, String description, String price, Category category,
                                   String image, String... variants) {
        Product p = Product.create(name, description, new BigDecimal(price), category, image);
        for (String spec : variants) {
            String[] parts = spec.split(":");
            p.addVariant(parts[0], parts[1], Integer.parseInt(parts[2]));
        }
        Daos.products().insert(p);
        return p;
    }
}
