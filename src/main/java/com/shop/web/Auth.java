package com.shop.web;

import com.shop.model.account.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/** Đăng nhập/đăng xuất ở tầng web: chỉ lưu {@link SessionUser} trong session. */
public final class Auth {

    public static final String ATTR = "auth";

    private Auth() {
    }

    public static SessionUser current(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        return session == null ? null : (SessionUser) session.getAttribute(ATTR);
    }

    /** Đổi session id khi đăng nhập để chống session fixation. */
    public static void login(HttpServletRequest req, User user) {
        HttpSession old = req.getSession(false);
        Object flash = old == null ? null : old.getAttribute(Flash.ATTR);
        if (old != null) {
            old.invalidate();
        }
        HttpSession session = req.getSession(true);
        session.setAttribute(ATTR, SessionUser.of(user));
        if (flash != null) {
            session.setAttribute(Flash.ATTR, flash);
        }
    }

    public static void refresh(HttpServletRequest req, User user) {
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.setAttribute(ATTR, SessionUser.of(user));
        }
    }

    public static void logout(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }
}
