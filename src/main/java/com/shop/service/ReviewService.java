package com.shop.service;

import com.shop.dao.Daos;
import com.shop.model.DomainException;
import com.shop.model.account.Customer;
import com.shop.model.catalog.Product;
import com.shop.model.catalog.Review;
import com.shop.persistence.Tx;
import java.util.Optional;

/**
 * Đánh giá sản phẩm: khách tự kiểm tra lịch sử mua ({@code writeReview} → {@code hasPurchased}),
 * sản phẩm từ chối nếu khách đã đánh giá ({@code addReview} → {@code hasReviewBy}). Muốn đổi ý thì sửa đánh giá cũ.
 */
public class ReviewService {

    /** Viết mới, hoặc sửa nếu khách đã có đánh giá cho sản phẩm này. Trả về true nếu là đánh giá mới. */
    public boolean save(long customerId, long productId, int rating, String comment) {
        return Tx.inTransaction(() -> {
            Customer customer = Daos.users().findCustomer(customerId)
                    .orElseThrow(() -> new DomainException("Không tìm thấy khách hàng"));
            Product product = Daos.products().findById(productId)
                    .orElseThrow(() -> new DomainException("Sản phẩm không tồn tại"));
            Optional<Review> existing = product.findReviewBy(customer);
            if (existing.isPresent()) {
                existing.get().edit(rating, comment);
                Daos.reviews().update(existing.get());
                return false;
            }
            Review review = customer.writeReview(product, rating, comment);
            Daos.reviews().insert(review, productId);
            return true;
        });
    }
}
