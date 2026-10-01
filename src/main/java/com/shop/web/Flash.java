package com.shop.web;

import java.io.Serializable;

/** Thông báo hiển thị một lần ở trang kế tiếp (sau redirect). */
public final class Flash implements Serializable {

    private static final long serialVersionUID = 1L;
    static final String ATTR = "flash";

    private final String type;
    private final String message;

    private Flash(String type, String message) {
        this.type = type;
        this.message = message;
    }

    public static Flash success(String message) {
        return new Flash("success", message);
    }

    public static Flash error(String message) {
        return new Flash("danger", message);
    }

    public static Flash info(String message) {
        return new Flash("info", message);
    }

    /** Lớp màu Bootstrap: success | danger | info. */
    public String getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }
}
