package com.shop.web.servlet.admin;

import com.shop.model.DomainException;
import com.shop.service.AdminDiscountService.PolicyForm;
import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

/** Đọc các trường chung của form khuyến mãi/voucher. */
final class DiscountForms {

    private DiscountForms() {
    }

    static PolicyForm policy(HttpServletRequest req) {
        String type = req.getParameter("policyType");
        BigDecimal value = "PERCENT".equals(type) ? percent(req.getParameter("policyValue")) : money(req, "policyValue");
        return new PolicyForm(type, value, "PERCENT".equals(type) ? money(req, "maxDiscount") : null);
    }

    /** Phần trăm cho phép số lẻ, ví dụ "12.5" hoặc "12,5". */
    private static BigDecimal percent(String v) {
        if (v == null || v.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(v.trim().replace(",", ".").replace("%", ""));
        } catch (NumberFormatException e) {
            throw new DomainException("Phần trăm giảm không hợp lệ: " + v);
        }
    }

    static LocalDateTime dateTime(HttpServletRequest req, String name, String label) {
        String v = req.getParameter(name);
        if (v == null || v.isBlank()) {
            throw new DomainException("Vui lòng nhập " + label);
        }
        try {
            return LocalDateTime.parse(v.trim());
        } catch (DateTimeParseException e) {
            throw new DomainException(label + " không hợp lệ");
        }
    }

    static BigDecimal money(HttpServletRequest req, String name) {
        String v = req.getParameter(name);
        if (v == null || v.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(v.trim().replace(".", "").replace(",", "").replace(" ", ""));
        } catch (NumberFormatException e) {
            throw new DomainException("Giá trị số không hợp lệ: " + v);
        }
    }
}
