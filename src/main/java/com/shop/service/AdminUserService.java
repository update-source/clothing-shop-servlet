package com.shop.service;

import com.shop.dao.Daos;
import com.shop.model.DomainException;
import com.shop.model.account.Employee;
import com.shop.model.account.EmployeeRole;
import com.shop.model.account.Gender;
import com.shop.model.account.User;
import com.shop.persistence.DataAccessException;
import com.shop.persistence.Tx;
import java.time.LocalDate;
import java.util.List;

/**
 * Quản trị tài khoản. Khoá là trạng thái của chính tài khoản ({@code User.lock/unlock});
 * chỉ quản trị viên được ra lệnh — đó là quy tắc phân quyền ở tầng chức năng này.
 */
public class AdminUserService {

    public List<User> search(String type, String keyword) {
        return Daos.users().search(type, keyword);
    }

    public User setLocked(long adminId, long userId, boolean locked) {
        if (adminId == userId) {
            throw new DomainException("Không thể tự khoá tài khoản của chính mình");
        }
        return Tx.inTransaction(() -> {
            User user = Daos.users().findById(userId).orElseThrow(() -> new DomainException("Không tìm thấy tài khoản"));
            if (locked) {
                user.lock();
            } else {
                user.unlock();
            }
            Daos.users().update(user);
            return user;
        });
    }

    public Employee createEmployee(String username, String password, String fullName, Gender gender, LocalDate dob,
                                   String email, String phone, EmployeeRole role, LocalDate hireDate) {
        try {
            return Tx.inTransaction(() -> {
                Employee employee = Employee.hire(username, password, fullName, gender, dob, email, phone, role, hireDate);
                if (Daos.users().existsUsername(employee.getUsername())) {
                    throw new DomainException("Tên đăng nhập đã được sử dụng");
                }
                if (Daos.users().existsEmail(employee.getEmail(), null)) {
                    throw new DomainException("Email đã được sử dụng");
                }
                Daos.users().insert(employee);
                return employee;
            });
        } catch (DataAccessException e) {
            throw new DomainException("Tên đăng nhập hoặc email đã được sử dụng");
        }
    }
}
