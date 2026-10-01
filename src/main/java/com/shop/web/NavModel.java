package com.shop.web;

import com.shop.dao.Daos;
import com.shop.service.CatalogService;
import com.shop.service.CategoryNode;
import java.util.List;

/**
 * Dữ liệu cho thanh điều hướng (menu danh mục, số món trong giỏ/yêu thích).
 * Chỉ truy vấn khi trang thực sự hiển thị header.
 */
public final class NavModel {

    private static final CatalogService CATALOG = new CatalogService();

    private final SessionUser user;
    private List<CategoryNode> categories;
    private Integer cartCount;
    private Integer wishlistCount;

    public NavModel(SessionUser user) {
        this.user = user;
    }

    public List<CategoryNode> getCategories() {
        if (categories == null) {
            categories = CATALOG.navigation();
        }
        return categories;
    }

    public int getCartCount() {
        if (cartCount == null) {
            cartCount = user != null && user.isCustomer() ? Daos.carts().countItems(user.getId()) : 0;
        }
        return cartCount;
    }

    public int getWishlistCount() {
        if (wishlistCount == null) {
            wishlistCount = user != null && user.isCustomer() ? Daos.wishlists().count(user.getId()) : 0;
        }
        return wishlistCount;
    }
}
