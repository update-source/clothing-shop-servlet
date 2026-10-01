package com.shop.web;

import com.shop.model.account.Customer;
import com.shop.model.account.Employee;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;

/** Tiện ích chung cho các controller: hiển thị view, redirect, flash, đọc tham số. */
public abstract class BaseServlet extends HttpServlet {

    protected void render(HttpServletRequest req, HttpServletResponse resp, String view)
            throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/views/" + view + ".jsp").forward(req, resp);
    }

    protected void redirect(HttpServletRequest req, HttpServletResponse resp, String path) throws IOException {
        resp.sendRedirect(req.getContextPath() + path);
    }

    protected void flash(HttpServletRequest req, Flash flash) {
        req.getSession().setAttribute(Flash.ATTR, flash);
    }

    protected SessionUser sessionUser(HttpServletRequest req) {
        return Auth.current(req);
    }

    /** Khách hàng đang đăng nhập (do AccessFilter nạp sẵn cho khu vực của khách). */
    protected Customer currentCustomer(HttpServletRequest req) {
        return (Customer) req.getAttribute(AccessAttributes.CURRENT_USER);
    }

    /** Nhân viên đang đăng nhập (do AccessFilter nạp sẵn cho khu vực quản lý). */
    protected Employee currentEmployee(HttpServletRequest req) {
        return (Employee) req.getAttribute(AccessAttributes.CURRENT_USER);
    }

    protected static String param(HttpServletRequest req, String name) {
        String v = req.getParameter(name);
        return v == null || v.isBlank() ? null : v.trim();
    }

    protected static Long longParam(HttpServletRequest req, String name) {
        String v = param(req, name);
        try {
            return v == null ? null : Long.parseLong(v);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    protected static int intParam(HttpServletRequest req, String name, int defaultValue) {
        String v = param(req, name);
        try {
            return v == null ? defaultValue : Integer.parseInt(v);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /** Số tiền nhập tay: bỏ dấu chấm/phẩy ngăn cách nghìn, "199.000" -> 199000. */
    protected static BigDecimal moneyParam(HttpServletRequest req, String name) {
        String v = param(req, name);
        if (v == null) {
            return null;
        }
        String digits = v.replace(".", "").replace(",", "").replace("₫", "").replace(" ", "");
        try {
            return new BigDecimal(digits);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Chỉ cho phép quay lại đường dẫn nội bộ (chống open redirect). */
    protected static String safePath(String next, String fallback) {
        if (next != null && next.startsWith("/") && !next.startsWith("//") && !next.contains("\\")) {
            return next;
        }
        return fallback;
    }
}
