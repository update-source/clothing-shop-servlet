package com.shop.web;

import com.shop.util.Money;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/** Hàm EL dùng trong JSP (khai báo ở WEB-INF/shop.tld, tiền tố "f"). */
public final class ElFunctions {

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter ISO_DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    private ElFunctions() {
    }

    public static String vnd(BigDecimal amount) {
        return Money.format(amount);
    }

    /** Số tiền dạng số thuần (không phần thập phân) để đặt vào ô nhập. */
    public static String plain(BigDecimal amount) {
        return amount == null ? "" : amount.setScale(0, RoundingMode.HALF_UP).toPlainString();
    }

    public static String dateTime(LocalDateTime t) {
        return t == null ? "" : t.format(DATE_TIME);
    }

    public static String date(LocalDate d) {
        return d == null ? "" : d.format(DATE);
    }

    public static String isoDate(LocalDate d) {
        return d == null ? "" : d.toString();
    }

    public static String isoDateTime(LocalDateTime t) {
        return t == null ? "" : t.format(ISO_DATE_TIME);
    }

    /** Chuỗi sao, ví dụ 4.2 -> "★★★★☆". */
    public static String stars(double rating) {
        int full = (int) Math.round(rating);
        return "★".repeat(Math.max(0, Math.min(5, full))) + "☆".repeat(Math.max(0, 5 - full));
    }

    public static String rating(double rating) {
        return String.format("%.1f", rating);
    }

    private static final Map<String, String> COLORS = Map.ofEntries(
            Map.entry("đen", "#222222"), Map.entry("trắng", "#ffffff"), Map.entry("xám", "#9e9e9e"),
            Map.entry("be", "#d9c8a9"), Map.entry("đỏ", "#d32f2f"), Map.entry("hồng", "#f48fb1"),
            Map.entry("vàng", "#fbc02d"), Map.entry("cam", "#fb8c00"), Map.entry("nâu", "#795548"),
            Map.entry("xanh navy", "#1f2a44"), Map.entry("xanh nhạt", "#a6c8ff"), Map.entry("xanh đậm", "#1b3a6b"),
            Map.entry("xanh rêu", "#556b2f"), Map.entry("xanh lá", "#43a047"), Map.entry("xanh dương", "#1e88e5"),
            Map.entry("tím", "#8e24aa"));

    /** Mã màu hiển thị cho tên màu tiếng Việt (mặc định xám nhạt). */
    public static String colorHex(String color) {
        return color == null ? "#cccccc" : COLORS.getOrDefault(color.trim().toLowerCase(), "#cccccc");
    }

    public static String url(String value) {
        return value == null ? "" : URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
