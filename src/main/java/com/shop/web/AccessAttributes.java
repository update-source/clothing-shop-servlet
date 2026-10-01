package com.shop.web;

/** Tên các thuộc tính request dùng chung giữa filter và controller. */
public final class AccessAttributes {

    /** Đối tượng User (Customer/Employee) đã nạp từ CSDL cho request hiện tại. */
    public static final String CURRENT_USER = "currentUser";

    private AccessAttributes() {
    }
}
