package com.shop.model.order;

public enum PaymentMethod {
    COD("Thanh toán khi nhận hàng (COD)"),
    VNPAY("Thanh toán online qua VNPAY");

    private final String label;

    PaymentMethod(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
