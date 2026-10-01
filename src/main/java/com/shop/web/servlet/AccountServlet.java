package com.shop.web.servlet;

import com.shop.model.DomainException;
import com.shop.model.account.Customer;
import com.shop.model.account.CustomerLevel;
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
import java.math.BigDecimal;

/** Hồ sơ khách hàng: thông tin, hạng thành viên, đổi mật khẩu. */
@WebServlet(urlPatterns = {"/account/profile", "/account/password"})
public class AccountServlet extends BaseServlet {

    private final AccountService accounts = new AccountService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!"/account/profile".equals(req.getServletPath())) {
            redirect(req, resp, "/account/profile");
            return;
        }
        Customer customer = currentCustomer(req);
        BigDecimal spent = customer.getTotalSpent();
        CustomerLevel level = CustomerLevel.fromSpent(spent);
        CustomerLevel next = level.next();
        req.setAttribute("customer", customer);
        req.setAttribute("totalSpent", spent);
        req.setAttribute("level", level);
        req.setAttribute("nextLevel", next);
        if (next != null) {
            req.setAttribute("toNextLevel", next.getMinSpent().subtract(spent));
            req.setAttribute("levelProgress", spent.multiply(BigDecimal.valueOf(100))
                    .divide(next.getMinSpent(), 0, java.math.RoundingMode.DOWN).intValue());
        }
        render(req, resp, "account/profile");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        long userId = sessionUser(req).getId();
        try {
            if ("/account/password".equals(req.getServletPath())) {
                accounts.changePassword(userId, req.getParameter("oldPassword"), req.getParameter("newPassword"),
                        req.getParameter("confirmPassword"));
                flash(req, Flash.success("Đã đổi mật khẩu."));
            } else {
                User user = accounts.updateProfile(userId, param(req, "fullName"), param(req, "phone"));
                Auth.refresh(req, user);
                flash(req, Flash.success("Đã cập nhật thông tin cá nhân."));
            }
        } catch (DomainException e) {
            flash(req, Flash.error(e.getMessage()));
        }
        redirect(req, resp, "/account/profile");
    }
}
