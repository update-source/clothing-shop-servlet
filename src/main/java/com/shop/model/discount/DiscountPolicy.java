package com.shop.model.discount;

import java.math.BigDecimal;

/**
 * Cách tính mức giảm, dùng chung cho Voucher và Promotion.
 * Thêm kiểu giảm mới chỉ cần thêm một lớp cài đặt (Open/Closed).
 */
public interface DiscountPolicy {

    /** Cho một số tiền, trả về số tiền được giảm (không vượt quá chính số tiền đó). */
    BigDecimal calculate(BigDecimal amount);

    /** Mô tả ngắn để hiển thị, ví dụ "Giảm 10% (tối đa 50.000 đ)". */
    String describe();
}
