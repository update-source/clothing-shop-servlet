package com.shop.service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Báo giá phí vận chuyển. Tích hợp đơn vị vận chuyển nằm ngoài phạm vi, nên lớp này đóng vai bảng giá
 * của đơn vị vận chuyển theo tỉnh/thành nhận hàng; thay bằng API thật mà không phải sửa đơn hàng.
 */
public class ShippingService {

    private static final Set<String> SPECIAL_CITIES = Set.of("Hà Nội", "TP. Hồ Chí Minh");
    private static final Set<String> MAJOR_CITIES = Set.of("Hải Phòng", "Đà Nẵng", "Huế", "Cần Thơ");

    private static final BigDecimal SPECIAL_FEE = new BigDecimal("25000");
    private static final BigDecimal MAJOR_FEE = new BigDecimal("30000");
    private static final BigDecimal PROVINCE_FEE = new BigDecimal("35000");

    public BigDecimal quote(String province) {
        if (province == null) {
            return PROVINCE_FEE;
        }
        String p = province.trim();
        if (SPECIAL_CITIES.contains(p)) {
            return SPECIAL_FEE;
        }
        return MAJOR_CITIES.contains(p) ? MAJOR_FEE : PROVINCE_FEE;
    }

    /** Bảng giá cho mọi tỉnh/thành — để trang thanh toán hiển thị phí ngay khi khách đổi địa chỉ. */
    public Map<String, BigDecimal> table() {
        Map<String, BigDecimal> table = new LinkedHashMap<>();
        Provinces.ALL.forEach(p -> table.put(p, quote(p)));
        return table;
    }
}
