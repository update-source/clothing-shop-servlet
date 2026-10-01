package com.shop.model;

import java.math.BigDecimal;
import java.util.regex.Pattern;

/** Các kiểm tra đầu vào dùng chung cho đối tượng nghiệp vụ. */
public final class Check {

    private static final Pattern PHONE = Pattern.compile("^(0|\\+84)\\d{9,10}$");
    private static final Pattern EMAIL = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");

    private Check() {
    }

    public static String text(String value, String label, int maxLength) {
        if (value == null || value.isBlank()) {
            throw new DomainException(label + " không được để trống");
        }
        String trimmed = value.trim();
        if (trimmed.length() > maxLength) {
            throw new DomainException(label + " tối đa " + maxLength + " ký tự");
        }
        return trimmed;
    }

    public static String optionalText(String value, String label, int maxLength) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return text(value, label, maxLength);
    }

    public static String phone(String value) {
        String phone = text(value, "Số điện thoại", 15).replace(" ", "");
        if (!PHONE.matcher(phone).matches()) {
            throw new DomainException("Số điện thoại không hợp lệ");
        }
        return phone;
    }

    public static String email(String value) {
        String email = text(value, "Email", 100).toLowerCase();
        if (!EMAIL.matcher(email).matches()) {
            throw new DomainException("Email không hợp lệ");
        }
        return email;
    }

    public static int positive(int value, String label) {
        if (value <= 0) {
            throw new DomainException(label + " phải lớn hơn 0");
        }
        return value;
    }

    public static BigDecimal positive(BigDecimal value, String label) {
        if (value == null || value.signum() <= 0) {
            throw new DomainException(label + " phải lớn hơn 0");
        }
        return value;
    }

    public static BigDecimal nonNegative(BigDecimal value, String label) {
        if (value == null || value.signum() < 0) {
            throw new DomainException(label + " không được âm");
        }
        return value;
    }
}
