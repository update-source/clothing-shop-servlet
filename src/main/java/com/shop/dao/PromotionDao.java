package com.shop.dao;

import com.shop.model.Lazy;
import com.shop.model.catalog.Product;
import com.shop.model.discount.Promotion;
import com.shop.persistence.Jdbc;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class PromotionDao {

    private static final String COLS =
            "pr.id, pr.name, pr.policy_type, pr.policy_value, pr.policy_max_discount, pr.period_start, pr.period_end";

    private static Promotion map(ResultSet rs) throws SQLException {
        long id = rs.getLong("id");
        return new Promotion(id, rs.getString("name"), PolicyColumns.readPolicy(rs), PolicyColumns.readPeriod(rs),
                Lazy.from(() -> Daos.products().findByPromotion(id)));
    }

    public List<Promotion> findAll() {
        return Jdbc.query("SELECT " + COLS + " FROM promotions pr ORDER BY pr.period_start DESC, pr.id DESC",
                PromotionDao::map);
    }

    public Optional<Promotion> findById(long id) {
        return Jdbc.one("SELECT " + COLS + " FROM promotions pr WHERE pr.id = ?", PromotionDao::map, id);
    }

    public List<Promotion> findOngoing(LocalDateTime now) {
        return Jdbc.query("SELECT " + COLS + " FROM promotions pr WHERE pr.period_start <= ? AND pr.period_end >= ? "
                + "ORDER BY pr.period_end", PromotionDao::map, now, now);
    }

    /** Các chương trình của từng sản phẩm (cùng một chương trình dùng chung một đối tượng). */
    public Map<Long, List<Promotion>> findByProductIds(Collection<Long> productIds) {
        Map<Long, List<Promotion>> result = new HashMap<>();
        if (productIds.isEmpty()) {
            return result;
        }
        List<Object> params = new ArrayList<>(productIds);
        Map<Long, Promotion> shared = new HashMap<>();
        Jdbc.query("SELECT pp.product_id, " + COLS + " FROM promotion_products pp "
                        + "JOIN promotions pr ON pr.id = pp.promotion_id WHERE pp.product_id IN (" + Jdbc.in(params) + ")",
                rs -> {
                    long promoId = rs.getLong("id");
                    Promotion promo = shared.get(promoId);
                    if (promo == null) {
                        promo = map(rs);
                        shared.put(promoId, promo);
                    }
                    result.computeIfAbsent(rs.getLong("product_id"), k -> new ArrayList<>()).add(promo);
                    return promo;
                }, params.toArray());
        return result;
    }

    public void insert(Promotion p) {
        p.assignId(Jdbc.insert("INSERT INTO promotions (name, policy_type, policy_value, policy_max_discount, "
                        + "period_start, period_end) VALUES (?, ?, ?, ?, ?, ?)",
                p.getName(), PolicyColumns.type(p.getPolicy()), PolicyColumns.value(p.getPolicy()),
                PolicyColumns.maxDiscount(p.getPolicy()), p.getPeriod().getStart(), p.getPeriod().getEnd()));
        saveProducts(p);
    }

    public void update(Promotion p) {
        Jdbc.update("UPDATE promotions SET name = ? WHERE id = ?", p.getName(), p.getId());
        saveProducts(p);
    }

    /** Đồng bộ danh sách sản phẩm của chương trình. */
    public void saveProducts(Promotion p) {
        Set<Long> existing = new HashSet<>(Jdbc.query(
                "SELECT product_id FROM promotion_products WHERE promotion_id = ?", rs -> rs.getLong(1), p.getId()));
        Set<Long> keep = new HashSet<>();
        for (Product product : p.getProducts()) {
            keep.add(product.getId());
            if (!existing.contains(product.getId())) {
                Jdbc.update("INSERT INTO promotion_products (promotion_id, product_id) VALUES (?, ?)",
                        p.getId(), product.getId());
            }
        }
        for (Long productId : existing) {
            if (!keep.contains(productId)) {
                Jdbc.update("DELETE FROM promotion_products WHERE promotion_id = ? AND product_id = ?",
                        p.getId(), productId);
            }
        }
    }

    public void delete(long id) {
        Jdbc.update("DELETE FROM promotion_products WHERE promotion_id = ?", id);
        Jdbc.update("DELETE FROM promotions WHERE id = ?", id);
    }
}
