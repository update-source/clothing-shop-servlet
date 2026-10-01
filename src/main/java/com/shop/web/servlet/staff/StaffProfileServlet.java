package com.shop.web.servlet.staff;

import com.shop.model.DomainException;
import com.shop.model.account.User;
import com.shop.service.AccountService;
import com.shop.web.Auth;
import com.shop.web.BaseServlet;
import com.shop.web.Flash;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/** Hồ sơ nhân viên: cập nhật họ tên/điện thoại và đổi mật khẩu. */
@WebServlet(urlPatterns = {"/staff/profile", "/staff/password"})
public class StaffProfileServlet extends BaseServlet {

    private final AccountService accounts = new AccountService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("employee", currentEmployee(req));
        render(req, resp, "staff/profile");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        long id = sessionUser(req).getId();
        try {
            if ("/staff/password".equals(req.getServletPath())) {
                accounts.changePassword(id, req.getParameter("oldPassword"), req.getParameter("newPassword"),
                        req.getParameter("confirmPassword"));
                flash(req, Flash.success("Đã đổi mật khẩu."));
            } else {
                User user = accounts.updateProfile(id, param(req, "fullName"), param(req, "phone"));
                Auth.refresh(req, user);
                flash(req, Flash.success("Đã cập nhật hồ sơ."));
            }
        } catch (DomainException e) {
            flash(req, Flash.error(e.getMessage()));
        }
        redirect(req, resp, "/staff/profile");
    }
}
