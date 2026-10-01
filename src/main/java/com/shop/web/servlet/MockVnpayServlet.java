package com.shop.web.servlet;

import com.shop.service.VnpayGateway;
import com.shop.web.BaseServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Cổng VNPAY GIẢ LẬP — chỉ bật khi {@code vnpay.mode=mock}, để demo khi chưa có tài khoản sandbox.
 * Nó đóng vai hệ thống bên ngoài: kiểm chữ ký yêu cầu, cho người dùng chọn kết quả, rồi chuyển về
 * return URL với tham số có chữ ký giống VNPAY thật.
 */
@WebServlet(urlPatterns = {"/payment/mock-vnpay", "/payment/mock-vnpay/complete"})
public class MockVnpayServlet extends BaseServlet {

    private final VnpayGateway gateway = new VnpayGateway();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!gateway.isMock()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        Map<String, String> request = PaymentServlet.vnpParams(req);
        boolean valid = gateway.verify(request);
        if ("/payment/mock-vnpay/complete".equals(req.getServletPath())) {
            if (!valid) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Chữ ký không hợp lệ");
                return;
            }
            resp.sendRedirect(request.get("vnp_ReturnUrl") + "?" + response(request, "success".equals(param(req, "result"))));
            return;
        }
        req.setAttribute("valid", valid);
        req.setAttribute("vnp", request);
        req.setAttribute("query", req.getQueryString());
        req.setAttribute("amount", gateway.parse(request).amount());
        render(req, resp, "payment/mock-gateway");
    }

    /** Tham số kết quả có chữ ký, giống những gì VNPAY gửi về return URL. */
    private String response(Map<String, String> request, boolean success) {
        Map<String, String> p = new TreeMap<>();
        p.put("vnp_Amount", request.get("vnp_Amount"));
        p.put("vnp_BankCode", "NCB");
        p.put("vnp_CardType", "ATM");
        p.put("vnp_OrderInfo", request.get("vnp_OrderInfo"));
        p.put("vnp_PayDate", ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"))
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        p.put("vnp_ResponseCode", success ? "00" : "24");
        p.put("vnp_TmnCode", request.get("vnp_TmnCode"));
        p.put("vnp_TransactionNo", success ? String.valueOf(ThreadLocalRandom.current().nextLong(10_000_000L, 99_999_999L)) : "0");
        p.put("vnp_TransactionStatus", success ? "00" : "02");
        p.put("vnp_TxnRef", request.get("vnp_TxnRef"));
        return gateway.signedQuery(p);
    }
}
