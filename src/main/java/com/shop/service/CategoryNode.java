package com.shop.service;

import com.shop.model.catalog.Category;
import java.util.List;

/** Một danh mục gốc cùng các danh mục con và số sản phẩm đang bán (đã cộng dồn). */
public record CategoryNode(Category category, List<Category> children, long productCount) {

    public Category getCategory() {
        return category;
    }

    public List<Category> getChildren() {
        return children;
    }

    public long getProductCount() {
        return productCount;
    }
}
