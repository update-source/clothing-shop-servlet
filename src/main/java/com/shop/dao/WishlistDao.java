package com.shop.dao;

import com.shop.model.catalog.Product;
import com.shop.persistence.Jdbc;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Danh sách yêu thích là quan hệ Customer → Product (nhiều – nhiều), không cần lớp riêng. */
public class WishlistDao {

    public List<Product> load(long customerId) {
        List<Long> ids = Jdbc.query("SELECT product_id FROM wishlists WHERE customer_id = ? ORDER BY added_at DESC",
                rs -> rs.getLong("product_id"), customerId);
        return new ArrayList<>(Daos.products().findByIds(ids).values());
    }

    public void save(long customerId, List<Product> wishlist) {
        Set<Long> existing = new HashSet<>(Jdbc.query("SELECT product_id FROM wishlists WHERE customer_id = ?",
                rs -> rs.getLong("product_id"), customerId));
        Set<Long> keep = new HashSet<>();
        for (Product p : wishlist) {
            keep.add(p.getId());
            if (!existing.contains(p.getId())) {
                Jdbc.update("INSERT INTO wishlists (customer_id, product_id, added_at) VALUES (?, ?, ?)",
                        customerId, p.getId(), LocalDateTime.now());
            }
        }
        for (Long productId : existing) {
            if (!keep.contains(productId)) {
                Jdbc.update("DELETE FROM wishlists WHERE customer_id = ? AND product_id = ?", customerId, productId);
            }
        }
    }

    public boolean contains(long customerId, long productId) {
        return Jdbc.count("SELECT COUNT(*) FROM wishlists WHERE customer_id = ? AND product_id = ?",
                customerId, productId) > 0;
    }

    public int count(long customerId) {
        return (int) Jdbc.count("SELECT COUNT(*) FROM wishlists WHERE customer_id = ?", customerId);
    }
}
