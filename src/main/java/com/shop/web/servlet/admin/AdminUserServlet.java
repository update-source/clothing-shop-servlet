package com.shop.web.servlet.admin;

import com.shop.model.DomainException;
import com.shop.model.account.EmployeeRole;
import com.shop.model.account.Gender;
import com.shop.model.account.User;
import com.shop.service.AdminUserService;
import com.shop.web.BaseServlet;
import com.shop.web.Flash;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/** Quản lý tài khoản: khách hàng và nhân viên — khoá/mở khoá, tạo tài khoản nhân viên. */
@WebServlet("/admin/users")
public class AdminUserServlet extends BaseServlet {

    private final AdminUserService users = new AdminUserService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String type = "EMPLOYEE".equals(param(req, "type")) ? "EMPLOYEE" : "CUSTOMER";
        req.setAttribute("type", type);
        req.setAttribute("users", users.search(type, param(req, "q")));
        req.setAttribute("roles", EmployeeRole.values());
        req.setAttribute("genders", Gender.values());
        render(req, resp, "admin/users");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String action = param(req, "action");
        String back = "/admin/users?type=" + ("createEmployee".equals(action) ? "EMPLOYEE" : param(req, "type"));
        try {
            switch (action == null ? "" : action) {
                case "lock", "unlock" -> {
                    Long id = longParam(req, "id");
                    if (id == null) {
                        throw new DomainException("Thiếu mã tài khoản");
                    }
                    User u = users.setLocked(sessionUser(req).getId(), id, "lock".equals(action));
                    flash(req, Flash.success(("lock".equals(action) ? "Đã khoá" : "Đã mở khoá") + " tài khoản @" + u.getUsername() + "."));
                }
                case "createEmployee" -> {
                    if (!req.getParameter("password").equals(req.getParameter("confirmPassword"))) {
                        throw new DomainException("Mật khẩu nhập lại không khớp");
                    }
                    users.createEmployee(param(req, "username"), req.getParameter("password"), param(req, "fullName"),
                            enumOrNull(Gender.class, param(req, "gender")), date(param(req, "dob")), param(req, "email"),
                            param(req, "phone"), enumOrNull(EmployeeRole.class, param(req, "role")), date(param(req, "hireDate")));
                    flash(req, Flash.success("Đã tạo tài khoản nhân viên."));
                }
                default -> flash(req, Flash.error("Thao tác không hợp lệ"));
            }
        } catch (DomainException e) {
            flash(req, Flash.error(e.getMessage()));
        }
        redirect(req, resp, back);
    }

    private static <E extends Enum<E>> E enumOrNull(Class<E> type, String value) {
        try {
            return value == null ? null : Enum.valueOf(type, value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static LocalDate date(String value) {
        try {
            return value == null ? null : LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            throw new DomainException("Ngày không hợp lệ: " + value);
        }
    }
}
