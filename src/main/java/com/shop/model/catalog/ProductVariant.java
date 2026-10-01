package com.shop.model.catalog;

import com.shop.model.Check;
import com.shop.model.DomainException;
import com.shop.model.Entity;

/**
 * Biến thể (SKU) — một cặp size + màu cụ thể của một mẫu sản phẩm, tự quản lý kho của mình.
 * Ràng buộc luôn đúng: 0 ≤ reservedQuantity ≤ stockQuantity.
 */
public class ProductVariant extends Entity {

    private final Product product;
    private final String size;
    private final String color;
    private int stockQuantity;
    private int reservedQuantity;

    public ProductVariant(Long id, Product product, String size, String color,
                          int stockQuantity, int reservedQuantity) {
        super(id);
        if (stockQuantity < 0 || reservedQuantity < 0 || reservedQuantity > stockQuantity) {
            throw new IllegalArgumentException("Invalid stock: " + stockQuantity + "/" + reservedQuantity);
        }
        this.product = product;
        this.size = size;
        this.color = color;
        this.stockQuantity = stockQuantity;
        this.reservedQuantity = reservedQuantity;
    }

    /** Số hàng còn bán được = tồn kho − số đang giữ cho các đơn chưa xác nhận. */
    public int getAvailableQuantity() {
        return stockQuantity - reservedQuantity;
    }

    public boolean isAvailable(int qty) {
        return qty > 0 && qty <= getAvailableQuantity();
    }

    /** Giữ hàng khi khách đặt đơn. Không đủ hàng còn bán thì từ chối ngay. */
    public void reserve(int qty) {
        Check.positive(qty, "Số lượng");
        if (!isAvailable(qty)) {
            int left = getAvailableQuantity();
            throw new DomainException(left == 0
                    ? product.getName() + " (" + getLabel() + ") đã hết hàng"
                    : product.getName() + " (" + getLabel() + ") chỉ còn " + left + " sản phẩm");
        }
        reservedQuantity += qty;
    }

    /** Nhả hàng đang giữ khi đơn bị huỷ trước khi xác nhận. */
    public void releaseReservation(int qty) {
        Check.positive(qty, "Số lượng");
        if (qty > reservedQuantity) {
            throw new IllegalStateException("Release more than reserved: " + qty + " > " + reservedQuantity);
        }
        reservedQuantity -= qty;
    }

    /** Nhân viên xác nhận đơn: hàng đang giữ được trừ hẳn khỏi kho. */
    public void commit(int qty) {
        Check.positive(qty, "Số lượng");
        if (qty > reservedQuantity) {
            throw new IllegalStateException("Commit more than reserved: " + qty + " > " + reservedQuantity);
        }
        reservedQuantity -= qty;
        stockQuantity -= qty;
    }

    /** Nhập hàng vào kho: nhập mới, hoặc nhập lại khi đơn đã xác nhận bị huỷ / giao thất bại / bị trả. */
    public void restock(int qty) {
        Check.positive(qty, "Số lượng nhập");
        stockQuantity += qty;
    }

    public String getLabel() {
        return color + " – " + size;
    }

    public boolean matches(String size, String color) {
        return this.size.equalsIgnoreCase(size.trim()) && this.color.equalsIgnoreCase(color.trim());
    }

    public Product getProduct() {
        return product;
    }

    public String getSize() {
        return size;
    }

    public String getColor() {
        return color;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public int getReservedQuantity() {
        return reservedQuantity;
    }
}
