package com.shop.web;

import com.shop.util.Money;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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

    public static String url(String value) {
        return value == null ? "" : URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
