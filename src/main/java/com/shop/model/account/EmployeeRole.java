package com.shop.model.account;

public enum EmployeeRole {
    STAFF("Nhân viên"),
    ADMIN("Quản trị viên");

    private final String label;

    EmployeeRole(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
