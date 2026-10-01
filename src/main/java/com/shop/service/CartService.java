package com.shop.service;

import com.shop.dao.Daos;
import com.shop.model.DomainException;
import com.shop.model.account.Customer;
import com.shop.model.catalog.Cart;
import com.shop.model.catalog.CartItem;
import com.shop.model.catalog.ProductVariant;
import com.shop.persistence.Tx;
import java.util.List;
import java.util.Map;

/** Giỏ hàng của khách: giỏ là bên duy nhất thêm, bớt, sửa số lượng các dòng của mình. */
public class CartService {

    /** Thêm biến thể vào giỏ; trả về biến thể để controller quay lại đúng trang sản phẩm. */
    public ProductVariant add(long customerId, long variantId, int qty) {
        return Tx.inTransaction(() -> {
            Customer customer = customer(customerId);
            ProductVariant variant = Daos.products().findVariants(List.of(variantId)).get(variantId);
            if (variant == null) {
                throw new DomainException("Sản phẩm không tồn tại");
            }
            Cart cart = customer.getCart();
            cart.addItem(variant, qty);
            Daos.carts().save(customerId, cart);
            return variant;
        });
    }

    /** Cập nhật số lượng nhiều dòng một lần; số lượng ≤ 0 nghĩa là bỏ dòng. */
    public void update(long customerId, Map<Long, Integer> quantities) {
        Tx.inTransaction(() -> {
            Customer customer = customer(customerId);
            Cart cart = customer.getCart();
            for (CartItem item : List.copyOf(cart.getItems())) {
                Integer qty = quantities.get(item.getVariant().getId());
                if (qty != null && qty != item.getQuantity()) {
                    cart.setQuantity(item.getVariant(), qty);
                }
            }
            Daos.carts().save(customerId, cart);
        });
    }

    public void remove(long customerId, long variantId) {
        Tx.inTransaction(() -> {
            Cart cart = customer(customerId).getCart();
            cart.getItems().stream().filter(i -> i.getVariant().getId() == variantId).findFirst()
                    .ifPresent(i -> cart.removeItem(i.getVariant()));
            Daos.carts().save(customerId, cart);
        });
    }

    private static Customer customer(long id) {
        return Daos.users().findCustomer(id).orElseThrow(() -> new DomainException("Không tìm thấy khách hàng"));
    }
}
