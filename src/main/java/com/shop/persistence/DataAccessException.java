package com.shop.persistence;

/** Lỗi truy cập CSDL (bọc SQLException để tầng trên không phụ thuộc JDBC). */
public class DataAccessException extends RuntimeException {

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
