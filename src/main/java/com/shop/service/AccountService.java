package com.shop.service;

import com.shop.dao.Daos;
import com.shop.model.DomainException;
import com.shop.model.account.Customer;
import com.shop.model.account.Gender;
import com.shop.model.account.User;
import com.shop.persistence.DataAccessException;
import com.shop.persistence.Tx;
import com.shop.util.PasswordHasher;
import java.time.LocalDate;

/** Đăng ký, đăng nhập và tự quản lý tài khoản (hồ sơ, mật khẩu). */
public class AccountService {

    /** Băm giả để thời gian phản hồi như nhau dù tên đăng nhập có tồn tại hay không. */
    private static final String DUMMY_HASH = PasswordHasher.hash("dummy-password");

    public User login(String username, String password) {
        User user = username == null ? null : Daos.users().findByUsername(username).orElse(null);
        if (user == null) {
            PasswordHasher.matches(password == null ? "" : password, DUMMY_HASH);
            throw new DomainException("Tên đăng nhập hoặc mật khẩu không đúng");
        }
        if (!user.verifyPassword(password)) {
            throw new DomainException("Tên đăng nhập hoặc mật khẩu không đúng");
        }
        if (!user.isActive()) {
            throw new DomainException("Tài khoản đã bị khoá. Vui lòng liên hệ cửa hàng.");
        }
        return user;
    }

    public Customer register(String username, String password, String confirmPassword, String fullName,
                             Gender gender, LocalDate dob, String email, String phone) {
        if (password == null || !password.equals(confirmPassword)) {
            throw new DomainException("Mật khẩu nhập lại không khớp");
        }
        try {
            return Tx.inTransaction(() -> {
                Customer customer = Customer.register(username, password, fullName, gender, dob, email, phone);
                if (Daos.users().existsUsername(customer.getUsername())) {
                    throw new DomainException("Tên đăng nhập đã được sử dụng");
                }
                if (Daos.users().existsEmail(customer.getEmail(), null)) {
                    throw new DomainException("Email đã được sử dụng");
                }
                Daos.users().insert(customer);
                return customer;
            });
        } catch (DataAccessException e) {
            // Hai người đăng ký cùng tên trong cùng một lúc: ràng buộc UNIQUE chặn người sau.
            throw new DomainException("Tên đăng nhập hoặc email đã được sử dụng");
        }
    }

    public User updateProfile(long userId, String fullName, String phone) {
        return Tx.inTransaction(() -> {
            User user = Daos.users().findById(userId).orElseThrow(() -> new DomainException("Không tìm thấy tài khoản"));
            user.updateProfile(fullName, phone);
            Daos.users().update(user);
            return user;
        });
    }

    public void changePassword(long userId, String oldPassword, String newPassword, String confirmPassword) {
        if (newPassword == null || !newPassword.equals(confirmPassword)) {
            throw new DomainException("Mật khẩu mới nhập lại không khớp");
        }
        Tx.inTransaction(() -> {
            User user = Daos.users().findById(userId).orElseThrow(() -> new DomainException("Không tìm thấy tài khoản"));
            user.changePassword(oldPassword, newPassword);
            Daos.users().update(user);
        });
    }
}
