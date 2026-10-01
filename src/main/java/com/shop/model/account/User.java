package com.shop.model.account;

import com.shop.model.Check;
import com.shop.model.DomainException;
import com.shop.model.Entity;
import com.shop.util.PasswordHasher;
import java.time.LocalDate;
import java.util.regex.Pattern;

/**
 * Tài khoản người dùng. Không ai chỉ là "User" chung chung — họ hoặc là khách hàng, hoặc là nhân viên.
 */
public abstract class User extends Entity {

    public static final int MIN_PASSWORD_LENGTH = 6;
    private static final Pattern USERNAME = Pattern.compile("^[a-zA-Z0-9_.]{4,30}$");

    private final String username;
    private String password;
    private String fullName;
    private final Gender gender;
    private final LocalDate dob;
    private final String email;
    private String phone;
    private boolean active;

    /** Khôi phục từ tầng lưu trữ; {@code passwordHash} là mật khẩu đã băm. */
    protected User(Long id, String username, String passwordHash, String fullName, Gender gender,
                   LocalDate dob, String email, String phone, boolean active) {
        super(id);
        this.username = username;
        this.password = passwordHash;
        this.fullName = fullName;
        this.gender = gender;
        this.dob = dob;
        this.email = email;
        this.phone = phone;
        this.active = active;
    }

    /** Kiểm tra và chuẩn hoá dữ liệu khi tạo tài khoản mới. */
    protected static String validUsername(String username) {
        String value = Check.text(username, "Tên đăng nhập", 30);
        if (!USERNAME.matcher(value).matches()) {
            throw new DomainException("Tên đăng nhập gồm 4–30 ký tự chữ, số, dấu chấm hoặc gạch dưới");
        }
        return value.toLowerCase();
    }

    protected static String hashNewPassword(String raw) {
        if (raw == null || raw.length() < MIN_PASSWORD_LENGTH) {
            throw new DomainException("Mật khẩu phải có ít nhất " + MIN_PASSWORD_LENGTH + " ký tự");
        }
        return PasswordHasher.hash(raw);
    }

    protected static LocalDate validDob(LocalDate dob) {
        if (dob != null && dob.isAfter(LocalDate.now())) {
            throw new DomainException("Ngày sinh không hợp lệ");
        }
        return dob;
    }

    public boolean verifyPassword(String raw) {
        return PasswordHasher.matches(raw, password);
    }

    public void changePassword(String oldPw, String newPw) {
        if (!verifyPassword(oldPw)) {
            throw new DomainException("Mật khẩu hiện tại không đúng");
        }
        password = hashNewPassword(newPw);
    }

    public void updateProfile(String fullName, String phone) {
        this.fullName = Check.text(fullName, "Họ tên", 100);
        this.phone = Check.phone(phone);
    }

    public boolean isActive() {
        return active;
    }

    /** Khoá tài khoản. Chỉ quản trị viên được ra lệnh — đó là quy tắc phân quyền ở tầng chức năng. */
    public void lock() {
        active = false;
    }

    public void unlock() {
        active = true;
    }

    public String getUsername() {
        return username;
    }

    /** Mật khẩu đã băm — chỉ tầng lưu trữ dùng. */
    public String getPasswordHash() {
        return password;
    }

    public String getFullName() {
        return fullName;
    }

    public Gender getGender() {
        return gender;
    }

    public LocalDate getDob() {
        return dob;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }
}
