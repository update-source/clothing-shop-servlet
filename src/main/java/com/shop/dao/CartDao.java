package com.shop.dao;

import com.shop.model.catalog.Cart;
import com.shop.model.catalog.CartItem;
import com.shop.model.catalog.ProductVariant;
import com.shop.persistence.Jdbc;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Giỏ hàng gắn với khách (1–1) nên các dòng giỏ được định danh bằng (customer_id, variant_id). */
public class CartDao {

    public Cart load(long customerId) {
        Map<Long, Integer> rows = new LinkedHashMap<>();
        Jdbc.query("SELECT variant_id, quantity FROM cart_items WHERE customer_id = ? ORDER BY added_at, variant_id",
                rs -> rows.put(rs.getLong("variant_id"), rs.getInt("quantity")), customerId);
        Map<Long, ProductVariant> variants = Daos.products().findVariants(rows.keySet());
        List<CartItem> items = new ArrayList<>();
        rows.forEach((variantId, qty) -> {
            ProductVariant v = variants.get(variantId);
            if (v != null) {
                items.add(new CartItem(v, qty));
            }
        });
        return new Cart(items);
    }

    /** Đồng bộ các dòng giỏ với CSDL (giữ nguyên thời điểm thêm của dòng cũ). */
    public void save(long customerId, Cart cart) {
        Map<Long, Integer> existing = new HashMap<>();
        Jdbc.query("SELECT variant_id, quantity FROM cart_items WHERE customer_id = ?",
                rs -> existing.put(rs.getLong("variant_id"), rs.getInt("quantity")), customerId);
        Set<Long> keep = new HashSet<>();
        LocalDateTime now = LocalDateTime.now();
        for (CartItem item : cart.getItems()) {
            long variantId = item.getVariant().getId();
            keep.add(variantId);
            Integer oldQty = existing.get(variantId);
            if (oldQty == null) {
                Jdbc.update("INSERT INTO cart_items (customer_id, variant_id, quantity, added_at) VALUES (?, ?, ?, ?)",
                        customerId, variantId, item.getQuantity(), now);
            } else if (oldQty != item.getQuantity()) {
                Jdbc.update("UPDATE cart_items SET quantity = ? WHERE customer_id = ? AND variant_id = ?",
                        item.getQuantity(), customerId, variantId);
            }
        }
        for (Long variantId : existing.keySet()) {
            if (!keep.contains(variantId)) {
                Jdbc.update("DELETE FROM cart_items WHERE customer_id = ? AND variant_id = ?", customerId, variantId);
            }
        }
    }

    /** Tổng số món trong giỏ — cho biểu tượng giỏ hàng trên thanh điều hướng. */
    public int countItems(long customerId) {
        return (int) Jdbc.count("SELECT COALESCE(SUM(quantity), 0) FROM cart_items WHERE customer_id = ?", customerId);
    }
}
