package com.shop.dao;

import com.shop.model.catalog.Review;
import com.shop.persistence.Jdbc;
import java.util.List;

public class ReviewDao {

    /** Đánh giá của một sản phẩm, kèm tác giả, mới nhất trước. */
    public List<Review> findByProduct(long productId) {
        return Jdbc.query("SELECT r.id AS review_id, r.rating, r.comment, r.created_at AS review_created, "
                        + "r.updated_at AS review_updated, " + UserDao.columns("u")
                        + " FROM reviews r JOIN users u ON u.id = r.customer_id "
                        + "WHERE r.product_id = ? ORDER BY r.created_at DESC, r.id DESC",
                rs -> new Review(rs.getLong("review_id"), UserDao.customer(rs), rs.getInt("rating"),
                        rs.getString("comment"), Jdbc.getDateTime(rs, "review_created"),
                        Jdbc.getDateTime(rs, "review_updated")),
                productId);
    }

    public void insert(Review review, long productId) {
        review.assignId(Jdbc.insert("INSERT INTO reviews (product_id, customer_id, rating, comment, created_at) "
                        + "VALUES (?, ?, ?, ?, ?)", productId, review.getAuthor().getId(), review.getRating(),
                review.getComment(), review.getCreatedAt()));
    }

    public void update(Review review) {
        Jdbc.update("UPDATE reviews SET rating = ?, comment = ?, updated_at = ? WHERE id = ?",
                review.getRating(), review.getComment(), review.getUpdatedAt(), review.getId());
    }
}
