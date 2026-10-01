package com.shop.web.filter;

import com.shop.model.DomainException;
import com.shop.persistence.Tx;
import com.shop.web.Auth;
import com.shop.web.NavModel;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Bộ lọc đầu tiên của mọi request động: mã hoá UTF-8, dữ liệu dùng chung cho view,
 * và trả kết nối CSDL của request về pool khi xong.
 */
public class RequestContextFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        if (req.getCharacterEncoding() == null) {
            req.setCharacterEncoding("UTF-8");
        }
        if (isStatic(req)) {
            chain.doFilter(request, response);
            return;
        }
        response.setCharacterEncoding("UTF-8");
        try {
            if (req.getAttribute("nav") == null) {
                req.setAttribute("now", LocalDateTime.now());
                req.setAttribute("nav", new NavModel(Auth.current(req)));
                req.setAttribute("auth", Auth.current(req));
            }
            chain.doFilter(request, response);
        } catch (IOException | ServletException | RuntimeException e) {
            if (!(rootCause(e) instanceof DomainException)) {
                req.getServletContext().log("Request failed: " + req.getMethod() + " " + req.getRequestURI(), e);
            }
            throw e;
        } finally {
            Tx.release();
        }
    }

    private static Throwable rootCause(Throwable e) {
        Throwable t = e;
        while (t.getCause() != null && t.getCause() != t) {
            t = t.getCause();
        }
        return t;
    }

    static boolean isStatic(HttpServletRequest req) {
        String path = req.getRequestURI().substring(req.getContextPath().length());
        return path.startsWith("/css/") || path.startsWith("/js/") || path.startsWith("/fonts/")
                || path.startsWith("/images/") || path.startsWith("/uploads/") || path.equals("/favicon.ico");
    }
}
