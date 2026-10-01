package com.shop.dao;

import com.shop.model.DomainException;
import com.shop.model.Lazy;
import com.shop.model.catalog.Category;
import com.shop.model.catalog.Product;
import com.shop.model.catalog.ProductVariant;
import com.shop.model.discount.Promotion;
import com.shop.persistence.Jdbc;
import com.shop.persistence.Tracker;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Nạp/lưu sản phẩm. Các sản phẩm nạp cùng nhau dùng chung một "lô": biến thể và khuyến mãi
 * của cả lô được nạp bằng một truy vấn ở lần truy cập đầu tiên (tránh N+1).
 */
public class ProductDao {

    private static final String SELECT =
            "SELECT p.id, p.category_id, p.name, p.description, p.base_price, p.active, p.image_url FROM products p";

    private record Row(long id, long categoryId, String name, String description,
                       BigDecimal basePrice, boolean active, String imageUrl) {
    }

    private static Row row(ResultSet rs) throws SQLException {
        return new Row(rs.getLong("id"), rs.getLong("category_id"), rs.getString("name"),
                rs.getString("description"), rs.getBigDecimal("base_price"), rs.getBoolean("active"),
                rs.getString("image_url"));
    }

    /** Lô sản phẩm nạp cùng nhau. */
    private static final class Batch {
        private final Map<Long, Product> products = new LinkedHashMap<>();
        private Map<Long, List<ProductVariant>> variants;
        private Map<Long, List<Promotion>> promotions;

        synchronized List<ProductVariant> variantsOf(long productId) {
            if (variants == null) {
                variants = loadVariants(products);
            }
            return variants.computeIfAbsent(productId, k -> new ArrayList<>());
        }

        synchronized List<Promotion> promotionsOf(long productId) {
            if (promotions == null) {
                promotions = Daos.promotions().findByProductIds(products.keySet());
            }
            return promotions.computeIfAbsent(productId, k -> new ArrayList<>());
        }
    }

    private List<Product> build(List<Row> rows) {
        if (rows.isEmpty()) {
            return new ArrayList<>();
        }
        Map<Long, Category> categories = Daos.categories().findAllMap();
        Batch batch = new Batch();
        for (Row r : rows) {
            long id = r.id();
            Product p = new Product(id, r.name(), r.description(), r.basePrice(), r.active(),
                    categories.get(r.categoryId()), r.imageUrl(),
                    Lazy.from(() -> batch.variantsOf(id)),
                    Lazy.from(() -> Daos.reviews().findByProduct(id)),
                    Lazy.from(() -> batch.promotionsOf(id)));
            batch.products.put(id, p);
        }
        return new ArrayList<>(batch.products.values());
    }

    private static Map<Long, List<ProductVariant>> loadVariants(Map<Long, Product> products) {
        Map<Long, List<ProductVariant>> result = new LinkedHashMap<>();
        if (products.isEmpty()) {
            return result;
        }
        List<Object> ids = new ArrayList<>(products.keySet());
        Jdbc.query("SELECT id, product_id, size, color, stock_quantity, reserved_quantity "
                        + "FROM product_variants WHERE product_id IN (" + Jdbc.in(ids) + ") ORDER BY product_id, id",
                rs -> {
                    long productId = rs.getLong("product_id");
                    ProductVariant v = new ProductVariant(rs.getLong("id"), products.get(productId),
                            rs.getString("size"), rs.getString("color"),
                            rs.getInt("stock_quantity"), rs.getInt("reserved_quantity"));
                    Tracker.remember(v, new int[]{v.getStockQuantity(), v.getReservedQuantity()});
                    result.computeIfAbsent(productId, k -> new ArrayList<>()).add(v);
                    return v;
                }, ids.toArray());
        return result;
    }

    // ------------------------------------------------------------------ truy vấn

    public Optional<Product> findById(long id) {
        return build(Jdbc.query(SELECT + " WHERE p.id = ?", ProductDao::row, id)).stream().findFirst();
    }

    /** Các sản phẩm theo id, giữ thứ tự của danh sách id. */
    public Map<Long, Product> findByIds(Collection<Long> ids) {
        Map<Long, Product> result = new LinkedHashMap<>();
        if (ids.isEmpty()) {
            return result;
        }
        List<Object> params = new ArrayList<>(new LinkedHashSet<>(ids));
        Map<Long, Product> found = new LinkedHashMap<>();
        build(Jdbc.query(SELECT + " WHERE p.id IN (" + Jdbc.in(params) + ")", ProductDao::row, params.toArray()))
                .forEach(p -> found.put(p.getId(), p));
        for (Long id : ids) {
            if (found.containsKey(id)) {
                result.put(id, found.get(id));
            }
        }
        return result;
    }

    /** Các biến thể theo id; mỗi biến thể gắn với đúng sản phẩm của nó (nạp theo lô). */
    public Map<Long, ProductVariant> findVariants(Collection<Long> variantIds) {
        Map<Long, ProductVariant> result = new LinkedHashMap<>();
        if (variantIds.isEmpty()) {
            return result;
        }
        List<Object> params = new ArrayList<>(new LinkedHashSet<>(variantIds));
        Map<Long, Long> productOf = new LinkedHashMap<>();
        Jdbc.query("SELECT id, product_id FROM product_variants WHERE id IN (" + Jdbc.in(params) + ")",
                rs -> productOf.put(rs.getLong("id"), rs.getLong("product_id")), params.toArray());
        Map<Long, Product> products = findByIds(new LinkedHashSet<>(productOf.values()));
        for (Long vid : variantIds) {
            Product p = products.get(productOf.get(vid));
            if (p != null) {
                p.findVariant(vid).ifPresent(v -> result.put(vid, v));
            }
        }
        return result;
    }

    public Page<Product> search(ProductFilter f) {
        StringBuilder where = new StringBuilder(" WHERE 1 = 1");
        List<Object> params = new ArrayList<>();
        if (f.activeOnly()) {
            where.append(" AND p.active = TRUE");
        }
        if (f.categoryIds() != null && !f.categoryIds().isEmpty()) {
            where.append(" AND p.category_id IN (").append(Jdbc.in(f.categoryIds())).append(")");
            params.addAll(f.categoryIds());
        }
        if (f.keyword() != null && !f.keyword().isBlank()) {
            where.append(" AND (LOWER(p.name) LIKE ? OR LOWER(p.description) LIKE ?)");
            String kw = "%" + f.keyword().trim().toLowerCase() + "%";
            params.add(kw);
            params.add(kw);
        }
        boolean hasSizes = f.sizes() != null && !f.sizes().isEmpty();
        boolean hasColors = f.colors() != null && !f.colors().isEmpty();
        if (hasSizes || hasColors) {
            where.append(" AND EXISTS (SELECT 1 FROM product_variants v WHERE v.product_id = p.id");
            if (hasSizes) {
                where.append(" AND v.size IN (").append(Jdbc.in(f.sizes())).append(")");
                params.addAll(f.sizes());
            }
            if (hasColors) {
                where.append(" AND v.color IN (").append(Jdbc.in(f.colors())).append(")");
                params.addAll(f.colors());
            }
            where.append(")");
        }
        String order = switch (f.sort() == null ? "" : f.sort()) {
            case "name_asc" -> " ORDER BY p.name ASC";
            case "name_desc" -> " ORDER BY p.name DESC";
            default -> " ORDER BY p.created_at DESC, p.id DESC";
        };
        int page = Math.max(1, f.page());
        if (f.hasPriceCriteria()) {
            // Giá bán cuối phụ thuộc khuyến mãi đang chạy nên lọc/sắp trong bộ nhớ.
            LocalDateTime now = LocalDateTime.now();
            List<Product> all = build(Jdbc.query(SELECT + where + order, ProductDao::row, params.toArray()));
            List<Product> filtered = all.stream()
                    .filter(p -> f.minPrice() == null || p.getFinalPrice(now).compareTo(f.minPrice()) >= 0)
                    .filter(p -> f.maxPrice() == null || p.getFinalPrice(now).compareTo(f.maxPrice()) <= 0)
                    .collect(Collectors.toCollection(ArrayList::new));
            if ("price_asc".equals(f.sort())) {
                filtered.sort(Comparator.comparing(p -> p.getFinalPrice(now)));
            } else if ("price_desc".equals(f.sort())) {
                filtered.sort(Comparator.comparing((Product p) -> p.getFinalPrice(now)).reversed());
            }
            int from = Math.min(filtered.size(), (page - 1) * f.pageSize());
            int to = Math.min(filtered.size(), from + f.pageSize());
            return new Page<>(new ArrayList<>(filtered.subList(from, to)), page, f.pageSize(), filtered.size());
        }
        long total = Jdbc.count("SELECT COUNT(*) FROM products p" + where, params.toArray());
        List<Object> pageParams = new ArrayList<>(params);
        pageParams.add(f.pageSize());
        pageParams.add((page - 1) * f.pageSize());
        List<Product> items = build(Jdbc.query(SELECT + where + order + " LIMIT ? OFFSET ?",
                ProductDao::row, pageParams.toArray()));
        return new Page<>(items, page, f.pageSize(), total);
    }

    public List<Product> findNewest(int limit) {
        return build(Jdbc.query(SELECT + " WHERE p.active = TRUE ORDER BY p.created_at DESC, p.id DESC LIMIT ?",
                ProductDao::row, limit));
    }

    public List<Product> findByPromotion(long promotionId) {
        return build(Jdbc.query(SELECT + " JOIN promotion_products pp ON pp.product_id = p.id "
                + "WHERE pp.promotion_id = ? ORDER BY p.name", ProductDao::row, promotionId));
    }

    public List<Product> findAllForAdmin(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return build(Jdbc.query(SELECT + " ORDER BY p.id DESC", ProductDao::row));
        }
        return build(Jdbc.query(SELECT + " WHERE LOWER(p.name) LIKE ? ORDER BY p.id DESC", ProductDao::row,
                "%" + keyword.trim().toLowerCase() + "%"));
    }

    public List<String> findDistinctSizes() {
        List<String> sizes = Jdbc.query("SELECT DISTINCT v.size FROM product_variants v "
                + "JOIN products p ON p.id = v.product_id WHERE p.active = TRUE", rs -> rs.getString(1));
        List<String> order = List.of("XS", "S", "M", "L", "XL", "XXL", "XXXL");
        sizes.sort(Comparator.comparingInt((String s) -> order.contains(s) ? order.indexOf(s) : 100)
                .thenComparing(Comparator.naturalOrder()));
        return sizes;
    }

    public List<String> findDistinctColors() {
        return Jdbc.query("SELECT DISTINCT v.color FROM product_variants v JOIN products p ON p.id = v.product_id "
                + "WHERE p.active = TRUE ORDER BY v.color", rs -> rs.getString(1));
    }

    public long countActive() {
        return Jdbc.count("SELECT COUNT(*) FROM products WHERE active = TRUE");
    }

    // ------------------------------------------------------------------ ghi

    public void insert(Product p) {
        p.assignId(Jdbc.insert("INSERT INTO products (category_id, name, description, base_price, active, image_url, "
                        + "created_at) VALUES (?, ?, ?, ?, ?, ?, ?)",
                p.getCategory().getId(), p.getName(), p.getDescription(), p.getBasePrice(), p.isActive(),
                p.getImageUrl(), LocalDateTime.now()));
        saveVariants(p);
    }

    public void update(Product p) {
        Jdbc.update("UPDATE products SET category_id = ?, name = ?, description = ?, base_price = ?, active = ?, "
                        + "image_url = ? WHERE id = ?",
                p.getCategory().getId(), p.getName(), p.getDescription(), p.getBasePrice(), p.isActive(),
                p.getImageUrl(), p.getId());
    }

    public void saveVariants(Product p) {
        p.getVariants().forEach(this::saveVariant);
    }

    /**
     * Lưu biến thể. Tồn kho và số đang giữ được ghi bằng chênh lệch so với lúc nạp, kèm điều kiện
     * 0 ≤ giữ ≤ tồn — nên hai đơn đặt cùng lúc không thể bán quá số hàng thật.
     */
    public void saveVariant(ProductVariant v) {
        if (v.isNew()) {
            v.assignId(Jdbc.insert("INSERT INTO product_variants (product_id, size, color, stock_quantity, "
                            + "reserved_quantity) VALUES (?, ?, ?, ?, ?)",
                    v.getProduct().getId(), v.getSize(), v.getColor(), v.getStockQuantity(), v.getReservedQuantity()));
        } else {
            int[] original = Tracker.original(v);
            if (original == null) {
                throw new IllegalStateException("Variant " + v.getId() + " was not loaded in this unit of work");
            }
            int dStock = v.getStockQuantity() - original[0];
            int dReserved = v.getReservedQuantity() - original[1];
            if (dStock == 0 && dReserved == 0) {
                return;
            }
            int updated = Jdbc.update("UPDATE product_variants SET stock_quantity = stock_quantity + ?, "
                            + "reserved_quantity = reserved_quantity + ? WHERE id = ? "
                            + "AND reserved_quantity + ? >= 0 AND reserved_quantity + ? <= stock_quantity + ?",
                    dStock, dReserved, v.getId(), dReserved, dReserved, dStock);
            if (updated == 0) {
                throw new DomainException(v.getProduct().getName() + " (" + v.getLabel()
                        + ") vừa hết hàng, vui lòng thử lại");
            }
        }
        Tracker.remember(v, new int[]{v.getStockQuantity(), v.getReservedQuantity()});
    }
}
