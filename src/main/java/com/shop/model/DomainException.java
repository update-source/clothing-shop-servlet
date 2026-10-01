package com.shop.model;

/**
 * Vi phạm một quy tắc nghiệp vụ. Thông điệp được viết để hiển thị trực tiếp cho người dùng.
 */
public class DomainException extends RuntimeException {

    public DomainException(String message) {
        super(message);
    }
}
