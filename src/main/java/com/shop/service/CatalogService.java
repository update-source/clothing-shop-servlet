package com.shop.service;

import com.shop.dao.Daos;
import com.shop.model.catalog.Category;
import com.shop.model.catalog.Product;
import com.shop.model.discount.Promotion;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Truy vấn phía khách cho danh mục, sản phẩm và khuyến mãi. */
public class CatalogService {

    /** Cây danh mục hai cấp cho menu và thanh lọc. */
    public List<CategoryNode> navigation() {
        List<Category> all = Daos.categories().findAll();
        Map<Long, Long> counts = Daos.categories().countActiveProductsRecursive();
        List<CategoryNode> nodes = new ArrayList<>();
        for (Category root : all) {
            if (root.getParent() == null) {
                List<Category> children = all.stream()
                        .filter(c -> c.getParent() != null && c.getParent().equals(root)).toList();
                nodes.add(new CategoryNode(root, children, counts.getOrDefault(root.getId(), 0L)));
            }
        }
        return nodes;
    }

    public List<Product> featured(int limit) {
        return Daos.products().findNewest(limit);
    }

    public List<Promotion> ongoingPromotions() {
        return Daos.promotions().findOngoing(LocalDateTime.now());
    }
}
