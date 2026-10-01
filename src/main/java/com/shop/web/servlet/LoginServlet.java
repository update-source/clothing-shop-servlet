package com.shop.web.servlet;

import com.shop.model.DomainException;
import com.shop.model.account.Employee;
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

@WebServlet("/login")
public class LoginServlet extends BaseServlet {

    private final AccountService accounts = new AccountService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (sessionUser(req) != null) {
            redirect(req, resp, "/");
            return;
        }
        render(req, resp, "auth/login");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            User user = accounts.login(param(req, "username"), req.getParameter("password"));
            Auth.login(req, user);
            flash(req, Flash.success("Xin chào, " + user.getFullName() + "!"));
            String home = user instanceof Employee e ? (e.isAdmin() ? "/admin" : "/staff/orders") : "/";
            redirect(req, resp, safePath(param(req, "next"), home));
        } catch (DomainException e) {
            req.setAttribute("error", e.getMessage());
            render(req, resp, "auth/login");
        }
    }
}
