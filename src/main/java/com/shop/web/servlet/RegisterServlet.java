package com.shop.web.servlet;

import com.shop.model.DomainException;
import com.shop.model.account.Customer;
import com.shop.model.account.Gender;
import com.shop.service.AccountService;
import com.shop.web.Auth;
import com.shop.web.BaseServlet;
import com.shop.web.Flash;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@WebServlet("/register")
public class RegisterServlet extends BaseServlet {

    private final AccountService accounts = new AccountService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (sessionUser(req) != null) {
            redirect(req, resp, "/");
            return;
        }
        req.setAttribute("genders", Gender.values());
        render(req, resp, "auth/register");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            Customer customer = accounts.register(param(req, "username"), req.getParameter("password"),
                    req.getParameter("confirmPassword"), param(req, "fullName"), gender(param(req, "gender")),
                    date(param(req, "dob")), param(req, "email"), param(req, "phone"));
            Auth.login(req, customer);
            flash(req, Flash.success("Đăng ký thành công. Chào mừng " + customer.getFullName() + " đến với Shoppers!"));
            redirect(req, resp, safePath(param(req, "next"), "/"));
        } catch (DomainException e) {
            req.setAttribute("error", e.getMessage());
            req.setAttribute("genders", Gender.values());
            render(req, resp, "auth/register");
        }
    }

    static Gender gender(String value) {
        try {
            return value == null ? null : Gender.valueOf(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    static LocalDate date(String value) {
        try {
            return value == null ? null : LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            throw new DomainException("Ngày sinh không hợp lệ");
        }
    }
}
