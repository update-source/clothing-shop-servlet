package com.shop.web;

import com.shop.model.account.Customer;
import com.shop.model.account.Employee;
import com.shop.model.account.User;
import java.io.Serializable;

/** Thông tin tối thiểu về người đăng nhập, lưu trong session (không giữ đối tượng nghiệp vụ). */
public final class SessionUser implements Serializable {

    private static final long serialVersionUID = 1L;

    private final long id;
    private final String username;
    private final String fullName;
    private final boolean customer;
    private final boolean admin;

    private SessionUser(long id, String username, String fullName, boolean customer, boolean admin) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.customer = customer;
        this.admin = admin;
    }

    public static SessionUser of(User user) {
        boolean admin = user instanceof Employee e && e.isAdmin();
        return new SessionUser(user.getId(), user.getUsername(), user.getFullName(),
                user instanceof Customer, admin);
    }

    public long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    public boolean isCustomer() {
        return customer;
    }

    public boolean isEmployee() {
        return !customer;
    }

    public boolean isAdmin() {
        return admin;
    }
}
