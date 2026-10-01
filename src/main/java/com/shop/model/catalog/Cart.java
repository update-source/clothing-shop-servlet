package com.shop.model.catalog;

import com.shop.model.Check;
import com.shop.model.DomainException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Giỏ hàng của một khách. Nội dung của giỏ chính là các dòng hàng bên trong; giỏ là bên duy nhất
 * được thêm, bớt và sửa số lượng các dòng của mình.
 */
public class Cart {

    public static final int MAX_QTY_PER_LINE = 99;

    private final List<CartItem> items;

    public Cart() {
        this(new ArrayList<>());
    }

    public Cart(List<CartItem> items) {
        this.items = new ArrayList<>(items);
    }

    /** Thêm một biến thể; nếu đã có trong giỏ thì cộng dồn số lượng. Giỏ kiểm tra còn đủ hàng bán. */
    public void addItem(ProductVariant variant, int qty) {
        Check.positive(qty, "Số lượng");
        Optional<CartItem> existing = find(variant);
        int newQty = existing.map(CartItem::getQuantity).orElse(0) + qty;
        requireSellable(variant, newQty);
        if (existing.isPresent()) {
            existing.get().setQuantity(newQty);
        } else {
            items.add(new CartItem(variant, newQty));
        }
    }

    public void removeItem(ProductVariant variant) {
        items.removeIf(i -> i.getVariant().equals(variant));
    }

    /** Đổi số lượng một dòng; số lượng ≤ 0 nghĩa là bỏ dòng đó. */
    public void setQuantity(ProductVariant variant, int qty) {
        CartItem item = find(variant)
                .orElseThrow(() -> new DomainException("Sản phẩm không có trong giỏ"));
        if (qty <= 0) {
            removeItem(variant);
            return;
        }
        requireSellable(variant, qty);
        item.setQuantity(qty);
    }

    private void requireSellable(ProductVariant variant, int qty) {
        if (!variant.getProduct().isActive()) {
            throw new DomainException(variant.getProduct().getName() + " đã ngừng bán");
        }
        if (qty > MAX_QTY_PER_LINE) {
            throw new DomainException("Mỗi sản phẩm chỉ đặt tối đa " + MAX_QTY_PER_LINE + " cái");
        }
        if (!variant.isAvailable(qty)) {
            int left = variant.getAvailableQuantity();
            throw new DomainException(left == 0
                    ? variant.getProduct().getName() + " (" + variant.getLabel() + ") đã hết hàng"
                    : variant.getProduct().getName() + " (" + variant.getLabel() + ") chỉ còn " + left + " sản phẩm");
        }
    }

    /** Tổng tạm tính theo giá hiện tại của sản phẩm. */
    public BigDecimal getTotal() {
        return items.stream().map(CartItem::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void clear() {
        items.clear();
    }

    public Optional<CartItem> find(ProductVariant variant) {
        return items.stream().filter(i -> i.getVariant().equals(variant)).findFirst();
    }

    public List<CartItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    /** Tổng số món trong giỏ (cộng số lượng các dòng). */
    public int getItemCount() {
        return items.stream().mapToInt(CartItem::getQuantity).sum();
    }
}
