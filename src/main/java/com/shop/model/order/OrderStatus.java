package com.shop.model.order;

public enum OrderStatus {
    PENDING("Chờ xác nhận", "warning"),
    CONFIRMED("Đã xác nhận", "info"),
    SHIPPING("Đang giao", "primary"),
    COMPLETED("Hoàn tất", "success"),
    CANCELLED("Đã huỷ", "secondary"),
    DELIVERY_FAILED("Giao thất bại", "danger"),
    RETURNED("Đã trả hàng", "dark");

    private final String label;
    private final String badge;

    OrderStatus(String label, String badge) {
        this.label = label;
        this.badge = badge;
    }

    public String getLabel() {
        return label;
    }

    /** Lớp màu Bootstrap dùng cho nhãn trạng thái. */
    public String getBadge() {
        return badge;
    }
}
