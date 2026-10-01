package com.shop.web.servlet;

import com.shop.web.Auth;
import com.shop.web.BaseServlet;
import com.shop.web.Flash;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/** Đăng xuất chỉ nhận POST (có CSRF token) để trang khác không thể đăng xuất hộ người dùng. */
@WebServlet("/logout")
public class LogoutServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        redirect(req, resp, "/");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Auth.logout(req);
        flash(req, Flash.info("Bạn đã đăng xuất."));
        redirect(req, resp, "/");
    }
}
