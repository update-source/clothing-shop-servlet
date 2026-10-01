package com.shop.model.account;

import java.math.BigDecimal;

/**
 * Hạng thành viên là kết quả của việc mua sắm: mỗi hạng mang một ngưỡng chi tiêu tối thiểu.
 * Mức ngưỡng do cửa hàng quyết định.
 */
public enum CustomerLevel {
    CASUAL(new BigDecimal("0"), "Thành viên"),
    REGULAR(new BigDecimal("2000000"), "Thân thiết"),
    LOYAL(new BigDecimal("10000000"), "VIP");

    private final BigDecimal minSpent;
    private final String label;

    CustomerLevel(BigDecimal minSpent, String label) {
        this.minSpent = minSpent;
        this.label = label;
    }

    public BigDecimal getMinSpent() {
        return minSpent;
    }

    public String getLabel() {
        return label;
    }

    /** Hạng cao nhất mà tổng chi tiêu đạt ngưỡng. */
    public static CustomerLevel fromSpent(BigDecimal total) {
        BigDecimal spent = total == null ? BigDecimal.ZERO : total;
        CustomerLevel[] levels = values();
        for (int i = levels.length - 1; i >= 0; i--) {
            if (spent.compareTo(levels[i].minSpent) >= 0) {
                return levels[i];
            }
        }
        return CASUAL;
    }

    /** Hạng kế tiếp (null nếu đã cao nhất) — dùng để hiển thị tiến độ lên hạng. */
    public CustomerLevel next() {
        int i = ordinal() + 1;
        return i < values().length ? values()[i] : null;
    }
}
