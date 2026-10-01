package com.shop.model.account;

import com.shop.model.Check;
import com.shop.model.DomainException;
import com.shop.model.Lazy;
import com.shop.model.catalog.Cart;
import com.shop.model.catalog.Product;
import com.shop.model.catalog.Review;
import com.shop.model.discount.Voucher;
import com.shop.model.order.Order;
import com.shop.model.order.OrderStatus;
import com.shop.model.order.PaymentMethod;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Khách hàng — người mua. Những gì đặc trưng cho khách đều đến từ các mối quan hệ:
 * sổ địa chỉ, giỏ hàng, danh sách yêu thích và lịch sử đơn hàng.
 */
public class Customer extends User {

    private final Lazy<AddressBook> addressBook;
    private final Lazy<Cart> cart;
    private final Lazy<List<Product>> wishlist;
    private final Lazy<List<Order>> orders;

    public Customer(Long id, String username, String passwordHash, String fullName, Gender gender,
                    LocalDate dob, String email, String phone, boolean active,
                    Lazy<AddressBook> addressBook, Lazy<Cart> cart,
                    Lazy<List<Product>> wishlist, Lazy<List<Order>> orders) {
        super(id, username, passwordHash, fullName, gender, dob, email, phone, active);
        this.addressBook = addressBook;
        this.cart = cart;
        this.wishlist = wishlist;
        this.orders = orders;
    }

    /** Đăng ký tài khoản khách hàng mới, kèm sổ địa chỉ và giỏ hàng rỗng. */
    public static Customer register(String username, String rawPassword, String fullName, Gender gender,
                                    LocalDate dob, String email, String phone) {
        return new Customer(null, validUsername(username), hashNewPassword(rawPassword),
                Check.text(fullName, "Họ tên", 100), gender, validDob(dob), Check.email(email),
                Check.phone(phone), true,
                Lazy.of(new AddressBook()), Lazy.of(new Cart()),
                Lazy.of(new ArrayList<>()), Lazy.of(new ArrayList<>()));
    }

    // ---------------------------------------------------------------- yêu thích

    public void addToWishlist(Product product) {
        if (!product.isActive()) {
            throw new DomainException("Sản phẩm đã ngừng bán");
        }
        if (!isInWishlist(product)) {
            wishlist.get().add(product);
        }
    }

    public void removeFromWishlist(Product product) {
        wishlist.get().remove(product);
    }

    public boolean isInWishlist(Product product) {
        return wishlist.get().contains(product);
    }

    // ---------------------------------------------------------------- đặt hàng

    /**
     * Đặt hàng từ giỏ: chọn cùng lúc địa chỉ giao, phương thức thanh toán và voucher (nếu có).
     * Đơn tự tạo các dòng đơn, giữ hàng và ghi nhận lượt voucher; sau đó giỏ được làm trống.
     */
    public Order checkout(Address address, PaymentMethod method, Voucher voucher,
                          BigDecimal shippingFee, String note) {
        if (address == null) {
            throw new DomainException("Vui lòng chọn địa chỉ giao hàng");
        }
        if (method == null) {
            throw new DomainException("Vui lòng chọn phương thức thanh toán");
        }
        Cart myCart = getCart();
        if (myCart.isEmpty()) {
            throw new DomainException("Giỏ hàng đang trống");
        }
        Order order = new Order(this, myCart.getItems(), address, method, voucher,
                shippingFee, note, LocalDateTime.now());
        myCart.clear();
        orders.get().add(order);
        return order;
    }

    // ---------------------------------------------------------------- đánh giá

    public Review writeReview(Product product, int rating, String comment) {
        if (!hasPurchased(product)) {
            throw new DomainException("Bạn chỉ có thể đánh giá sản phẩm đã mua và nhận hàng thành công");
        }
        Review review = new Review(this, rating, comment);
        product.addReview(review);
        return review;
    }

    /** Đã mua = có đơn COMPLETED chứa sản phẩm này. */
    public boolean hasPurchased(Product product) {
        return orders.get().stream()
                .filter(o -> o.getStatus() == OrderStatus.COMPLETED)
                .anyMatch(o -> o.contains(product));
    }

    // ---------------------------------------------------------------- voucher & hạng

    /** Số lần đã dùng voucher — không tính các đơn đã trả lại lượt (huỷ, giao thất bại). */
    public int countVoucherUsage(Voucher voucher) {
        return (int) orders.get().stream().filter(o -> o.holdsVoucherUsage(voucher)).count();
    }

    /** Tổng chi tiêu = tổng tiền các đơn đã hoàn tất. */
    public BigDecimal getTotalSpent() {
        return orders.get().stream()
                .filter(o -> o.getStatus() == OrderStatus.COMPLETED)
                .map(Order::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public CustomerLevel getLevel() {
        return CustomerLevel.fromSpent(getTotalSpent());
    }

    // ---------------------------------------------------------------- quan hệ

    public AddressBook getAddressBook() {
        return addressBook.get();
    }

    public Cart getCart() {
        return cart.get();
    }

    public List<Product> getWishlist() {
        return Collections.unmodifiableList(wishlist.get());
    }

    public List<Order> getOrders() {
        return Collections.unmodifiableList(orders.get());
    }
}
