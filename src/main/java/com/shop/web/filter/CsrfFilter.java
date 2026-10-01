package com.shop.web.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Chống CSRF: mọi form POST phải gửi kèm {@code _csrf} trùng với token trong session.
 * JSP lấy token qua {@code ${sessionScope.csrfToken}}.
 */
public class CsrfFilter implements Filter {

    public static final String TOKEN = "csrfToken";
    public static final String PARAM = "_csrf";
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        if (RequestContextFilter.isStatic(req)) {
            chain.doFilter(request, response);
            return;
        }
        HttpSession session = req.getSession();
        String token = (String) session.getAttribute(TOKEN);
        if (token == null) {
            byte[] bytes = new byte[32];
            RANDOM.nextBytes(bytes);
            token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
            session.setAttribute(TOKEN, token);
        }
        if ("POST".equalsIgnoreCase(req.getMethod())) {
            String sent = req.getParameter(PARAM);
            if (sent == null || !MessageDigest.isEqual(token.getBytes(StandardCharsets.UTF_8),
                    sent.getBytes(StandardCharsets.UTF_8))) {
                ((HttpServletResponse) response).sendError(HttpServletResponse.SC_FORBIDDEN,
                        "Phiên làm việc đã hết hạn, vui lòng tải lại trang và thử lại.");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
