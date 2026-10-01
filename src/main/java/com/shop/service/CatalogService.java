package com.shop.service;

import com.shop.dao.Daos;
import com.shop.dao.Page;
import com.shop.dao.ProductFilter;
import com.shop.model.catalog.Category;
import com.shop.model.catalog.Product;
import com.shop.model.discount.Promotion;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** Truy vấn phía khách cho danh mục, sản phẩm và khuyến mãi. */
public class CatalogService {

    public static final int PAGE_SIZE = 9;

    /** Tiêu chí tìm kiếm từ trang cửa hàng. */
    public record ShopQuery(Long categoryId, String keyword, Set<String> sizes, Set<String> colors,
                            BigDecimal minPrice, BigDecimal maxPrice, String sort, int page) {
    }

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

    public Map<Long, Long> productCounts() {
        return Daos.categories().countActiveProductsRecursive();
    }

    public Optional<Category> category(Long id) {
        return id == null ? Optional.empty() : Daos.categories().findById(id);
    }

    public Page<Product> search(ShopQuery q) {
        Set<Long> categoryIds = q.categoryId() == null ? null : Daos.categories().selfAndDescendantIds(q.categoryId());
        return Daos.products().search(new ProductFilter(categoryIds, q.keyword(), q.sizes(), q.colors(),
                q.minPrice(), q.maxPrice(), q.sort(), true, q.page(), PAGE_SIZE));
    }

    public Optional<Product> product(long id) {
        return Daos.products().findById(id);
    }

    /** Sản phẩm cùng danh mục (không gồm chính nó). */
    public List<Product> related(Product product, int limit) {
        Set<Long> ids = Daos.categories().selfAndDescendantIds(product.getCategory().getId());
        return Daos.products().search(new ProductFilter(ids, null, null, null, null, null, "newest",
                        true, 1, limit + 1)).items().stream()
                .filter(p -> !p.equals(product)).limit(limit).toList();
    }

    public List<String> sizes() {
        return Daos.products().findDistinctSizes();
    }

    public List<String> colors() {
        return Daos.products().findDistinctColors();
    }

    public List<Product> featured(int limit) {
        return Daos.products().findNewest(limit);
    }

    public List<Promotion> ongoingPromotions() {
        return Daos.promotions().findOngoing(LocalDateTime.now());
    }
}
