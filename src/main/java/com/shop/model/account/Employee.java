package com.shop.model.account;

import com.shop.model.Check;
import java.time.LocalDate;

/**
 * Nhân viên cửa hàng. Quyền ADMIN gồm quản lý sản phẩm, khuyến mãi và khoá tài khoản.
 */
public class Employee extends User {

    private final LocalDate hireDate;
    private final EmployeeRole role;

    public Employee(Long id, String username, String passwordHash, String fullName, Gender gender,
                    LocalDate dob, String email, String phone, boolean active,
                    LocalDate hireDate, EmployeeRole role) {
        super(id, username, passwordHash, fullName, gender, dob, email, phone, active);
        this.hireDate = hireDate;
        this.role = role;
    }

    public static Employee hire(String username, String rawPassword, String fullName, Gender gender,
                                LocalDate dob, String email, String phone,
                                EmployeeRole role, LocalDate hireDate) {
        return new Employee(null, validUsername(username), hashNewPassword(rawPassword),
                Check.text(fullName, "Họ tên", 100), gender, validDob(dob), Check.email(email),
                Check.phone(phone), true,
                hireDate == null ? LocalDate.now() : hireDate,
                role == null ? EmployeeRole.STAFF : role);
    }

    public boolean isAdmin() {
        return role == EmployeeRole.ADMIN;
    }

    public LocalDate getHireDate() {
        return hireDate;
    }

    public EmployeeRole getRole() {
        return role;
    }
}
