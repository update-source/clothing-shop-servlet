package com.shop.model.catalog;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Một dòng trong giỏ, trỏ đến đúng một biến thể. Giá luôn lấy theo giá hiện tại của sản phẩm
 * vì khách chưa trả tiền thì giá chưa được chốt.
 */
public class CartItem {

    private final ProductVariant variant;
    private int quantity;

    public CartItem(ProductVariant variant, int quantity) {
        this.variant = variant;
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return variant.getProduct().getFinalPrice(LocalDateTime.now());
    }

    public BigDecimal getSubtotal() {
        return getUnitPrice().multiply(BigDecimal.valueOf(quantity));
    }

    /** Chỉ giỏ hàng được sửa số lượng dòng của mình. */
    void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public ProductVariant getVariant() {
        return variant;
    }

    public int getQuantity() {
        return quantity;
    }
}
