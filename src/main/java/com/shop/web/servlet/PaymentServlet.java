package com.shop.web.servlet;

import com.shop.model.DomainException;
import com.shop.service.PaymentService;
import com.shop.service.PaymentService.Outcome;
import com.shop.web.BaseServlet;
import com.shop.web.Flash;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Thanh toán VNPAY:
 * /orders/vnpay (sau khi đặt hàng), /orders/pay (thanh toán lại) — khu vực khách hàng;
 * /payment/vnpay-return (trình duyệt quay về), /payment/vnpay-ipn (cổng gọi máy chủ).
 */
@WebServlet(urlPatterns = {"/orders/vnpay", "/orders/pay", "/payment/vnpay-return", "/payment/vnpay-ipn"})
public class PaymentServlet extends BaseServlet {

    private final PaymentService payments = new PaymentService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        switch (req.getServletPath()) {
            case "/orders/vnpay" -> start(req, resp, false);
            case "/payment/vnpay-return" -> {
                Outcome outcome = payments.handleVnpayResult(vnpParams(req));
                req.setAttribute("outcome", outcome);
                render(req, resp, "payment/result");
            }
            case "/payment/vnpay-ipn" -> ipn(req, resp);
            default -> redirect(req, resp, "/orders");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if ("/orders/pay".equals(req.getServletPath())) {
            start(req, resp, true);
        } else {
            resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
    }

    private void start(HttpServletRequest req, HttpServletResponse resp, boolean newAttempt) throws IOException {
        Long orderId = longParam(req, "id");
        if (orderId == null) {
            redirect(req, resp, "/orders");
            return;
        }
        try {
            String url = payments.startVnpay(sessionUser(req).getId(), orderId, newAttempt, clientIp(req), baseUrl(req));
            resp.sendRedirect(url);
        } catch (DomainException e) {
            flash(req, Flash.error(e.getMessage()));
            redirect(req, resp, "/orders/detail?id=" + orderId);
        }
    }

    /** IPN: VNPAY gọi trực tiếp máy chủ, phản hồi JSON theo mã quy định của VNPAY. */
    private void ipn(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Outcome outcome = payments.handleVnpayResult(vnpParams(req));
        String code = switch (outcome.code()) {
            case PAID, FAILED -> "00";
            case ALREADY_PROCESSED -> "02";
            case NOT_FOUND -> "01";
            case INVALID_AMOUNT -> "04";
            case INVALID_SIGNATURE -> "97";
        };
        String message = "00".equals(code) ? "Confirm Success" : outcome.message();
        resp.setContentType("application/json;charset=UTF-8");
        resp.getWriter().write("{\"RspCode\":\"" + code + "\",\"Message\":\"" + message.replace("\"", "'") + "\"}");
    }

    static Map<String, String> vnpParams(HttpServletRequest req) {
        Map<String, String> params = new HashMap<>();
        req.getParameterMap().forEach((k, v) -> {
            if (k.startsWith("vnp_") && v.length > 0) {
                params.put(k, v[0]);
            }
        });
        return params;
    }

    static String baseUrl(HttpServletRequest req) {
        int port = req.getServerPort();
        boolean defaultPort = ("http".equals(req.getScheme()) && port == 80) || ("https".equals(req.getScheme()) && port == 443);
        return req.getScheme() + "://" + req.getServerName() + (defaultPort ? "" : ":" + port) + req.getContextPath();
    }

    private static String clientIp(HttpServletRequest req) {
        String forwarded = req.getHeader("X-Forwarded-For");
        String ip = forwarded != null && !forwarded.isBlank() ? forwarded.split(",")[0].trim() : req.getRemoteAddr();
        return "0:0:0:0:0:0:0:1".equals(ip) ? "127.0.0.1" : ip;
    }
}
