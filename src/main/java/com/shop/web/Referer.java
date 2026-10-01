package com.shop.web;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;

/** Lấy đường dẫn nội bộ của trang trước (header Referer) để quay lại sau một thao tác POST. */
public final class Referer {

    private Referer() {
    }

    /** Đường dẫn trong ứng dụng (không gồm context path), hoặc {@code fallback} nếu không hợp lệ. */
    public static String path(HttpServletRequest req, String fallback) {
        String header = req.getHeader("Referer");
        if (header == null) {
            return fallback;
        }
        try {
            URI uri = URI.create(header);
            if (uri.getHost() != null && !uri.getHost().equalsIgnoreCase(req.getServerName())) {
                return fallback;
            }
            String path = uri.getRawPath() + (uri.getRawQuery() == null ? "" : "?" + uri.getRawQuery());
            String ctx = req.getContextPath();
            if (!ctx.isEmpty()) {
                if (!path.startsWith(ctx)) {
                    return fallback;
                }
                path = path.substring(ctx.length());
            }
            return path.startsWith("/") && !path.startsWith("//") ? path : fallback;
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }
}
