package com.shop.web.filter;

import com.shop.dao.Daos;
import com.shop.model.account.Customer;
import com.shop.model.account.Employee;
import com.shop.model.account.User;
import com.shop.web.AccessAttributes;
import com.shop.web.Auth;
import com.shop.web.Flash;
import com.shop.web.SessionUser;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Phân quyền theo khu vực (tham số {@code require} trong web.xml):
 * customer — khu mua hàng; staff — nhân viên xử lý đơn; admin — quản trị sản phẩm, khuyến mãi, tài khoản.
 * Tài khoản bị khoá trong lúc đang đăng nhập sẽ bị đăng xuất ở request kế tiếp.
 */
public class AccessFilter implements Filter {

    private String require;

    @Override
    public void init(FilterConfig config) {
        require = config.getInitParameter("require");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        SessionUser sessionUser = Auth.current(req);
        if (sessionUser == null) {
            String next = req.getRequestURI().substring(req.getContextPath().length());
            if ("GET".equals(req.getMethod()) && req.getQueryString() != null) {
                next += "?" + req.getQueryString();
            }
            req.getSession().setAttribute("flash", Flash.info("Vui lòng đăng nhập để tiếp tục"));
            resp.sendRedirect(req.getContextPath() + "/login?next="
                    + URLEncoder.encode("GET".equals(req.getMethod()) ? next : "/", StandardCharsets.UTF_8));
            return;
        }
        User user = Daos.users().findById(sessionUser.getId()).orElse(null);
        if (user == null || !user.isActive()) {
            Auth.logout(req);
            req.getSession().setAttribute("flash", Flash.error("Tài khoản của bạn đã bị khoá. Vui lòng liên hệ cửa hàng."));
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        boolean allowed = switch (require) {
            case "customer" -> user instanceof Customer;
            case "staff" -> user instanceof Employee;
            case "admin" -> user instanceof Employee e && e.isAdmin();
            default -> false;
        };
        if (!allowed) {
            req.setAttribute("errorMessage", "customer".equals(require)
                    ? "Tài khoản nhân viên không dùng để mua hàng. Vui lòng đăng nhập bằng tài khoản khách hàng."
                    : "Bạn không có quyền truy cập khu vực này.");
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        req.setAttribute(AccessAttributes.CURRENT_USER, user);
        chain.doFilter(request, response);
    }
}
