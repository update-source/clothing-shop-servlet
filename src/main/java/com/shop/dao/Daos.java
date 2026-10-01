package com.shop.dao;

/**
 * Nơi giữ các DAO dùng chung. DAO không có trạng thái nên dùng một thể hiện duy nhất;
 * DAO gọi lẫn nhau qua đây để tránh vòng lặp khởi tạo.
 */
public final class Daos {

    private static final UserDao USERS = new UserDao();
    private static final AddressDao ADDRESSES = new AddressDao();
    private static final CategoryDao CATEGORIES = new CategoryDao();
    private static final ProductDao PRODUCTS = new ProductDao();
    private static final ReviewDao REVIEWS = new ReviewDao();
    private static final CartDao CARTS = new CartDao();
    private static final WishlistDao WISHLISTS = new WishlistDao();
    private static final VoucherDao VOUCHERS = new VoucherDao();
    private static final PromotionDao PROMOTIONS = new PromotionDao();
    private static final OrderDao ORDERS = new OrderDao();

    private Daos() {
    }

    public static UserDao users() {
        return USERS;
    }

    public static AddressDao addresses() {
        return ADDRESSES;
    }

    public static CategoryDao categories() {
        return CATEGORIES;
    }

    public static ProductDao products() {
        return PRODUCTS;
    }

    public static ReviewDao reviews() {
        return REVIEWS;
    }

    public static CartDao carts() {
        return CARTS;
    }

    public static WishlistDao wishlists() {
        return WISHLISTS;
    }

    public static VoucherDao vouchers() {
        return VOUCHERS;
    }

    public static PromotionDao promotions() {
        return PROMOTIONS;
    }

    public static OrderDao orders() {
        return ORDERS;
    }
}
