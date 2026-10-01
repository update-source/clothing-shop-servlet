package com.shop.service;

import java.util.List;

/**
 * 34 đơn vị hành chính cấp tỉnh theo mô hình hai cấp tỉnh – xã áp dụng từ 01/07/2025
 * (6 thành phố trực thuộc trung ương và 28 tỉnh).
 */
public final class Provinces {

    public static final List<String> ALL = List.of(
            "Hà Nội", "TP. Hồ Chí Minh", "Hải Phòng", "Đà Nẵng", "Huế", "Cần Thơ",
            "An Giang", "Bắc Ninh", "Cà Mau", "Cao Bằng", "Đắk Lắk", "Điện Biên", "Đồng Nai",
            "Đồng Tháp", "Gia Lai", "Hà Tĩnh", "Hưng Yên", "Khánh Hòa", "Lai Châu", "Lâm Đồng",
            "Lạng Sơn", "Lào Cai", "Nghệ An", "Ninh Bình", "Phú Thọ", "Quảng Ngãi", "Quảng Ninh",
            "Quảng Trị", "Sơn La", "Tây Ninh", "Thái Nguyên", "Thanh Hóa", "Tuyên Quang", "Vĩnh Long");

    private Provinces() {
    }

    public static boolean isValid(String province) {
        return province != null && ALL.contains(province.trim());
    }
}
