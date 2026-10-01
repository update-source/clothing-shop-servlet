package com.shop.dao;

import com.shop.model.Lazy;
import com.shop.model.account.Customer;
import com.shop.model.account.Employee;
import com.shop.model.account.EmployeeRole;
import com.shop.model.account.Gender;
import com.shop.model.account.User;
import com.shop.persistence.Jdbc;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Customer và Employee lưu chung bảng {@code users} (kế thừa một bảng, cột user_type).
 * Các quan hệ của khách (sổ địa chỉ, giỏ, yêu thích, đơn hàng) được nạp trễ khi cần.
 */
public class UserDao {

    static final String CUSTOMER = "CUSTOMER";
    static final String EMPLOYEE = "EMPLOYEE";

    /** Danh sách cột người dùng với bí danh bảng, dùng khi JOIN từ bảng khác. */
    static String columns(String alias) {
        String a = alias + ".";
        return a + "id, " + a + "user_type, " + a + "username, " + a + "password_hash, " + a + "full_name, "
                + a + "gender, " + a + "dob, " + a + "email, " + a + "phone, " + a + "active, "
                + a + "hire_date, " + a + "role";
    }

    private static final String SELECT = "SELECT " + columns("u") + " FROM users u";

    static User map(ResultSet rs) throws SQLException {
        return EMPLOYEE.equals(rs.getString("user_type")) ? employee(rs) : customer(rs);
    }

    static Customer customer(ResultSet rs) throws SQLException {
        long id = rs.getLong("id");
        Customer[] self = new Customer[1];
        Customer c = new Customer(id, rs.getString("username"), rs.getString("password_hash"),
                rs.getString("full_name"), gender(rs), Jdbc.getDate(rs, "dob"), rs.getString("email"),
                rs.getString("phone"), rs.getBoolean("active"),
                Lazy.from(() -> Daos.addresses().loadBook(id)),
                Lazy.from(() -> Daos.carts().load(id)),
                Lazy.from(() -> Daos.wishlists().load(id)),
                Lazy.from(() -> Daos.orders().findByCustomer(self[0])));
        self[0] = c;
        return c;
    }

    static Employee employee(ResultSet rs) throws SQLException {
        String role = rs.getString("role");
        return new Employee(rs.getLong("id"), rs.getString("username"), rs.getString("password_hash"),
                rs.getString("full_name"), gender(rs), Jdbc.getDate(rs, "dob"), rs.getString("email"),
                rs.getString("phone"), rs.getBoolean("active"), Jdbc.getDate(rs, "hire_date"),
                role == null ? EmployeeRole.STAFF : EmployeeRole.valueOf(role));
    }

    private static Gender gender(ResultSet rs) throws SQLException {
        String g = rs.getString("gender");
        return g == null ? null : Gender.valueOf(g);
    }

    // ------------------------------------------------------------------ truy vấn

    public Optional<User> findById(long id) {
        return Jdbc.one(SELECT + " WHERE u.id = ?", UserDao::map, id);
    }

    public Optional<User> findByUsername(String username) {
        if (username == null) {
            return Optional.empty();
        }
        return Jdbc.one(SELECT + " WHERE u.username = ?", UserDao::map, username.trim().toLowerCase());
    }

    public Optional<Customer> findCustomer(long id) {
        return Jdbc.one(SELECT + " WHERE u.id = ? AND u.user_type = ?", UserDao::customer, id, CUSTOMER);
    }

    public Optional<Employee> findEmployee(long id) {
        return Jdbc.one(SELECT + " WHERE u.id = ? AND u.user_type = ?", UserDao::employee, id, EMPLOYEE);
    }

    public Map<Long, Customer> findCustomers(Collection<Long> ids) {
        Map<Long, Customer> result = new LinkedHashMap<>();
        if (!ids.isEmpty()) {
            List<Object> params = new ArrayList<>(new LinkedHashSet<>(ids));
            Jdbc.query(SELECT + " WHERE u.id IN (" + Jdbc.in(params) + ")", UserDao::customer, params.toArray())
                    .forEach(c -> result.put(c.getId(), c));
        }
        return result;
    }

    public Map<Long, Employee> findEmployees(Collection<Long> ids) {
        Map<Long, Employee> result = new LinkedHashMap<>();
        if (!ids.isEmpty()) {
            List<Object> params = new ArrayList<>(new LinkedHashSet<>(ids));
            Jdbc.query(SELECT + " WHERE u.id IN (" + Jdbc.in(params) + ")", UserDao::employee, params.toArray())
                    .forEach(e -> result.put(e.getId(), e));
        }
        return result;
    }

    public boolean existsUsername(String username) {
        return Jdbc.count("SELECT COUNT(*) FROM users WHERE username = ?", username.trim().toLowerCase()) > 0;
    }

    public boolean existsEmail(String email, Long exceptUserId) {
        return Jdbc.count("SELECT COUNT(*) FROM users WHERE email = ? AND id <> ?",
                email.trim().toLowerCase(), exceptUserId == null ? -1L : exceptUserId) > 0;
    }

    /** Tìm người dùng cho trang quản trị. {@code type}: CUSTOMER, EMPLOYEE hoặc null (tất cả). */
    public List<User> search(String type, String keyword) {
        StringBuilder sql = new StringBuilder(SELECT + " WHERE 1 = 1");
        List<Object> params = new ArrayList<>();
        if (type != null && !type.isBlank()) {
            sql.append(" AND u.user_type = ?");
            params.add(type);
        }
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (LOWER(u.username) LIKE ? OR LOWER(u.full_name) LIKE ? OR LOWER(u.email) LIKE ?)");
            String kw = "%" + keyword.trim().toLowerCase() + "%";
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }
        sql.append(" ORDER BY u.id DESC");
        return Jdbc.query(sql.toString(), UserDao::map, params.toArray());
    }

    public long countCustomers() {
        return Jdbc.count("SELECT COUNT(*) FROM users WHERE user_type = ?", CUSTOMER);
    }

    // ------------------------------------------------------------------ ghi

    public void insert(User user) {
        boolean employee = user instanceof Employee;
        Employee e = employee ? (Employee) user : null;
        long id = Jdbc.insert("INSERT INTO users (user_type, username, password_hash, full_name, gender, dob, email, "
                        + "phone, active, hire_date, role, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                employee ? EMPLOYEE : CUSTOMER, user.getUsername(), user.getPasswordHash(), user.getFullName(),
                user.getGender(), user.getDob(), user.getEmail(), user.getPhone(), user.isActive(),
                e == null ? null : e.getHireDate(), e == null ? null : e.getRole(), LocalDateTime.now());
        user.assignId(id);
        if (!employee) {
            Jdbc.update("INSERT INTO address_books (customer_id, default_address_id) VALUES (?, NULL)", id);
        }
    }

    /** Lưu các thuộc tính đổi được của tài khoản: họ tên, điện thoại, mật khẩu, trạng thái. */
    public void update(User user) {
        Jdbc.update("UPDATE users SET full_name = ?, phone = ?, password_hash = ?, active = ? WHERE id = ?",
                user.getFullName(), user.getPhone(), user.getPasswordHash(), user.isActive(), user.getId());
    }
}
