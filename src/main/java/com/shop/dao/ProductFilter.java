package com.shop.dao;

import java.math.BigDecimal;
import java.util.Set;

/**
 * Điều kiện tìm sản phẩm. Lọc và sắp theo giá dùng giá bán cuối (đã áp khuyến mãi).
 *
 * @param sort newest | name_asc | name_desc | price_asc | price_desc
 */
public record ProductFilter(Set<Long> categoryIds, String keyword, Set<String> sizes, Set<String> colors,
                            BigDecimal minPrice, BigDecimal maxPrice, String sort,
                            boolean activeOnly, int page, int pageSize) {

    public boolean hasPriceCriteria() {
        return minPrice != null || maxPrice != null || "price_asc".equals(sort) || "price_desc".equals(sort);
    }
}
