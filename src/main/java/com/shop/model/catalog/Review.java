package com.shop.model.catalog;

import com.shop.model.Check;
import com.shop.model.DomainException;
import com.shop.model.Entity;
import com.shop.model.account.Customer;
import java.time.LocalDateTime;

/**
 * Đánh giá của một khách cho một sản phẩm. Số sao ngoài khoảng 1–5 bị từ chối ngay lúc tạo,
 * nên không bao giờ tồn tại một đánh giá sai.
 */
public class Review extends Entity {

    private final Customer author;
    private int rating;
    private String comment;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Review(Customer author, int rating, String comment) {
        this(null, author, validRating(rating), validComment(comment), LocalDateTime.now(), null);
    }

    public Review(Long id, Customer author, int rating, String comment,
                  LocalDateTime createdAt, LocalDateTime updatedAt) {
        super(id);
        this.author = author;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /** Người viết sửa lại số sao và nhận xét, cùng ràng buộc 1–5 sao. */
    public void edit(int rating, String comment) {
        this.rating = validRating(rating);
        this.comment = validComment(comment);
        this.updatedAt = LocalDateTime.now();
    }

    private static int validRating(int rating) {
        if (rating < 1 || rating > 5) {
            throw new DomainException("Số sao phải từ 1 đến 5");
        }
        return rating;
    }

    private static String validComment(String comment) {
        return Check.optionalText(comment, "Nhận xét", 1000);
    }

    public Customer getAuthor() {
        return author;
    }

    public int getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
