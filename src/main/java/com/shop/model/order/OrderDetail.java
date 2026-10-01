package com.shop.model.order;

import com.shop.model.Entity;
import com.shop.model.catalog.ProductVariant;
import java.math.BigDecimal;

/**
 * Một dòng đơn: đơn giá đã chốt và số lượng, trỏ đến đúng biến thể khách đã mua.
 * Đơn giá được giữ riêng vì giá sản phẩm có thể đổi sau này.
 */
public class OrderDetail extends Entity {

    private final ProductVariant variant;
    private final BigDecimal unitPrice;
    private final int quantity;

    public OrderDetail(Long id, ProductVariant variant, BigDecimal unitPrice, int quantity) {
        super(id);
        this.variant = variant;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }

    public BigDecimal getSubtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public ProductVariant getVariant() {
        return variant;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }
}
