package com.shop.model.order;

public enum PaymentStatus {
    UNPAID("Chưa thanh toán", "warning"),
    PAID("Đã thanh toán", "success"),
    FAILED("Thất bại", "danger"),
    REFUNDED("Đã hoàn tiền", "secondary");

    private final String label;
    private final String badge;

    PaymentStatus(String label, String badge) {
        this.label = label;
        this.badge = badge;
    }

    public String getLabel() {
        return label;
    }

    public String getBadge() {
        return badge;
    }
}
