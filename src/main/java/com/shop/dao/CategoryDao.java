package com.shop.dao;

import com.shop.model.catalog.Category;
import com.shop.persistence.Jdbc;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class CategoryDao {

    private record Row(long id, String name, Long parentId) {
    }

    /** Toàn bộ danh mục (bảng nhỏ) với liên kết cha đã dựng sẵn. */
    public Map<Long, Category> findAllMap() {
        Map<Long, Row> rows = new LinkedHashMap<>();
        Jdbc.query("SELECT id, name, parent_id FROM categories ORDER BY name",
                rs -> new Row(rs.getLong("id"), rs.getString("name"), Jdbc.getLong(rs, "parent_id")))
                .forEach(r -> rows.put(r.id(), r));
        Map<Long, Category> built = new LinkedHashMap<>();
        for (Row r : rows.values()) {
            build(r.id(), rows, built, 0);
        }
        return built;
    }

    private Category build(long id, Map<Long, Row> rows, Map<Long, Category> built, int depth) {
        Category existing = built.get(id);
        if (existing != null) {
            return existing;
        }
        Row r = rows.get(id);
        Category parent = r.parentId() == null || depth > 20 ? null : build(r.parentId(), rows, built, depth + 1);
        Category c = new Category(r.id(), r.name(), parent);
        built.put(id, c);
        return c;
    }

    /** Danh sách sắp theo tên đầy đủ (cha › con). */
    public List<Category> findAll() {
        List<Category> list = new ArrayList<>(findAllMap().values());
        list.sort(Comparator.comparing(Category::getFullName, String.CASE_INSENSITIVE_ORDER));
        return list;
    }

    public List<Category> findRoots() {
        return findAll().stream().filter(c -> c.getParent() == null).collect(Collectors.toList());
    }

    public Optional<Category> findById(long id) {
        return Optional.ofNullable(findAllMap().get(id));
    }

    /** Id của danh mục và mọi danh mục con cháu. */
    public Set<Long> selfAndDescendantIds(long id) {
        Map<Long, Category> all = findAllMap();
        Category target = all.get(id);
        if (target == null) {
            return Set.of();
        }
        return all.values().stream().filter(c -> c.isWithin(target))
                .map(Category::getId).collect(Collectors.toSet());
    }

    /** Số sản phẩm đang bán theo danh mục, đã cộng dồn các danh mục con. */
    public Map<Long, Long> countActiveProductsRecursive() {
        Map<Long, Long> direct = new HashMap<>();
        Jdbc.query("SELECT category_id, COUNT(*) AS n FROM products WHERE active = TRUE GROUP BY category_id",
                rs -> new long[]{rs.getLong("category_id"), rs.getLong("n")})
                .forEach(r -> direct.put(r[0], r[1]));
        Map<Long, Category> all = findAllMap();
        Map<Long, Long> total = new HashMap<>();
        for (Category c : all.values()) {
            long n = direct.getOrDefault(c.getId(), 0L);
            for (Category p = c; p != null; p = p.getParent()) {
                total.merge(p.getId(), n, Long::sum);
            }
        }
        return total;
    }

    public void save(Category category) {
        Long parentId = category.getParent() == null ? null : category.getParent().getId();
        if (category.isNew()) {
            category.assignId(Jdbc.insert("INSERT INTO categories (name, parent_id) VALUES (?, ?)",
                    category.getName(), parentId));
        } else {
            Jdbc.update("UPDATE categories SET name = ?, parent_id = ? WHERE id = ?",
                    category.getName(), parentId, category.getId());
        }
    }

    public boolean isInUse(long id) {
        return Jdbc.count("SELECT COUNT(*) FROM products WHERE category_id = ?", id) > 0
                || Jdbc.count("SELECT COUNT(*) FROM categories WHERE parent_id = ?", id) > 0;
    }

    public void delete(long id) {
        Jdbc.update("DELETE FROM categories WHERE id = ?", id);
    }
}
