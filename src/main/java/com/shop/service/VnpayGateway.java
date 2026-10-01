package com.shop.service;

import com.shop.config.AppConfig;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Cổng thanh toán VNPAY (API v2.1.0): tạo URL thanh toán có chữ ký HMAC-SHA512 và kiểm chữ ký kết quả trả về.
 * Chế độ {@code mock} trỏ URL thanh toán về cổng giả lập trong ứng dụng nhưng dùng cùng cách ký/kiểm chữ ký.
 */
public class VnpayGateway {

    public static final String VERSION = "2.1.0";
    private static final ZoneId VN = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter VNP_DATE = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /** Kết quả giao dịch cổng báo về. */
    public record Result(long txnRef, BigDecimal amount, String responseCode, String transactionStatus,
                         String transactionNo, String bankCode) {

        public boolean isSuccess() {
            return "00".equals(responseCode) && "00".equals(transactionStatus);
        }
    }

    public boolean isMock() {
        return "mock".equalsIgnoreCase(AppConfig.get("vnpay.mode", "mock"));
    }

    public String tmnCode() {
        return AppConfig.get("vnpay.tmnCode");
    }

    public int timeoutMinutes() {
        return AppConfig.getInt("order.vnpayTimeoutMinutes", 15);
    }

    /**
     * @param baseUrl gốc ứng dụng, ví dụ http://localhost:8080 (đã gồm context path)
     */
    public String paymentUrl(long txnRef, BigDecimal amount, String orderInfo, String clientIp, String baseUrl) {
        ZonedDateTime now = ZonedDateTime.now(VN);
        Map<String, String> params = new TreeMap<>();
        params.put("vnp_Version", VERSION);
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", tmnCode());
        params.put("vnp_Amount", amount.multiply(BigDecimal.valueOf(100)).toBigInteger().toString());
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", String.valueOf(txnRef));
        params.put("vnp_OrderInfo", orderInfo);
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", baseUrl + AppConfig.get("vnpay.returnPath", "/payment/vnpay-return"));
        params.put("vnp_IpAddr", clientIp);
        params.put("vnp_CreateDate", now.format(VNP_DATE));
        params.put("vnp_ExpireDate", now.plusMinutes(timeoutMinutes()).format(VNP_DATE));
        String query = signedQuery(params);
        String payUrl = isMock() ? baseUrl + "/payment/mock-vnpay" : AppConfig.get("vnpay.payUrl");
        return payUrl + "?" + query;
    }

    /** Chuỗi truy vấn đã sắp xếp theo tên tham số, kèm vnp_SecureHash. */
    public String signedQuery(Map<String, String> params) {
        String data = hashData(params);
        return data + "&vnp_SecureHash=" + hmacSha512(AppConfig.get("vnpay.hashSecret"), data);
    }

    /** Kiểm chữ ký các tham số VNPAY gửi về (return URL hoặc IPN). */
    public boolean verify(Map<String, String> params) {
        String received = params.get("vnp_SecureHash");
        if (received == null) {
            return false;
        }
        Map<String, String> fields = new TreeMap<>(params);
        fields.remove("vnp_SecureHash");
        fields.remove("vnp_SecureHashType");
        String expected = hmacSha512(AppConfig.get("vnpay.hashSecret"), hashData(fields));
        return MessageDigest.isEqual(expected.getBytes(StandardCharsets.US_ASCII),
                received.toLowerCase().getBytes(StandardCharsets.US_ASCII));
    }

    public Result parse(Map<String, String> params) {
        long txnRef;
        BigDecimal amount;
        try {
            txnRef = Long.parseLong(params.getOrDefault("vnp_TxnRef", ""));
            amount = new BigDecimal(params.getOrDefault("vnp_Amount", "0")).divide(BigDecimal.valueOf(100));
        } catch (NumberFormatException e) {
            txnRef = -1;
            amount = BigDecimal.ZERO;
        }
        return new Result(txnRef, amount, params.get("vnp_ResponseCode"), params.get("vnp_TransactionStatus"),
                params.get("vnp_TransactionNo"), params.get("vnp_BankCode"));
    }

    private static String hashData(Map<String, String> params) {
        return new TreeMap<>(params).entrySet().stream()
                .filter(e -> e.getValue() != null && !e.getValue().isEmpty() && e.getKey().startsWith("vnp_"))
                .map(e -> enc(e.getKey()) + "=" + enc(e.getValue()))
                .collect(Collectors.joining("&"));
    }

    private static String enc(String s) {
        return URLEncoder.encode(s, StandardCharsets.US_ASCII);
    }

    static String hmacSha512(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            return HexFormat.of().formatHex(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("HmacSHA512 not available", e);
        }
    }
}
