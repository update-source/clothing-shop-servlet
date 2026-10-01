package com.shop.service;

import com.shop.dao.Daos;
import com.shop.model.DomainException;
import com.shop.model.account.Customer;
import com.shop.model.catalog.Product;
import com.shop.persistence.Tx;

/** Danh sách yêu thích: khách thích mẫu sản phẩm, chưa cần chọn size hay màu. */
public class WishlistService {

    /** Thêm nếu chưa có, bỏ nếu đã có. Trả về true nếu sản phẩm vừa được thêm. */
    public boolean toggle(long customerId, long productId) {
        return Tx.inTransaction(() -> {
            Customer customer = customer(customerId);
            Product product = Daos.products().findById(productId)
                    .orElseThrow(() -> new DomainException("Sản phẩm không tồn tại"));
            boolean adding = !customer.isInWishlist(product);
            if (adding) {
                customer.addToWishlist(product);
            } else {
                customer.removeFromWishlist(product);
            }
            Daos.wishlists().save(customerId, customer.getWishlist());
            return adding;
        });
    }

    public void remove(long customerId, long productId) {
        Tx.inTransaction(() -> {
            Customer customer = customer(customerId);
            customer.getWishlist().stream().filter(p -> p.getId() == productId).findFirst()
                    .ifPresent(customer::removeFromWishlist);
            Daos.wishlists().save(customerId, customer.getWishlist());
        });
    }

    private static Customer customer(long id) {
        return Daos.users().findCustomer(id).orElseThrow(() -> new DomainException("Không tìm thấy khách hàng"));
    }
}
