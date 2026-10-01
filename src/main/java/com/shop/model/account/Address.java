package com.shop.model.account;

import com.shop.model.Check;
import java.util.Objects;

/**
 * Địa chỉ nhận hàng — value object bất biến theo mô hình hai cấp tỉnh/thành – phường/xã (từ 07/2025).
 * Hai địa chỉ cùng nội dung được coi là một; muốn sửa thì thay bằng địa chỉ mới.
 * <p>
 * {@code id} chỉ là định danh của bản ghi trong sổ địa chỉ, không tham gia so sánh giá trị.
 */
public final class Address {

    private Long id;
    private final String recipientName;
    private final String phone;
    private final String street;
    private final String ward;
    private final String province;

    public Address(String recipientName, String phone, String street, String ward, String province) {
        this(null, recipientName, phone, street, ward, province);
    }

    public Address(Long id, String recipientName, String phone, String street, String ward, String province) {
        this.id = id;
        this.recipientName = Check.text(recipientName, "Tên người nhận", 100);
        this.phone = Check.phone(phone);
        this.street = Check.text(street, "Số nhà, tên đường", 255);
        this.ward = Check.text(ward, "Phường/xã", 100);
        this.province = Check.text(province, "Tỉnh/thành phố", 100);
    }

    /** Bản sao không mang định danh — dùng khi đơn hàng giữ bản địa chỉ giao của riêng nó. */
    public Address copy() {
        return new Address(null, recipientName, phone, street, ward, province);
    }

    public String getFullAddress() {
        return street + ", " + ward + ", " + province;
    }

    public Long getId() {
        return id;
    }

    public void assignId(long id) {
        if (this.id != null) {
            throw new IllegalStateException("Id already assigned");
        }
        this.id = id;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public String getPhone() {
        return phone;
    }

    public String getStreet() {
        return street;
    }

    public String getWard() {
        return ward;
    }

    public String getProvince() {
        return province;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Address a)) return false;
        return recipientName.equalsIgnoreCase(a.recipientName)
                && phone.equals(a.phone)
                && street.equalsIgnoreCase(a.street)
                && ward.equalsIgnoreCase(a.ward)
                && province.equalsIgnoreCase(a.province);
    }

    @Override
    public int hashCode() {
        return Objects.hash(recipientName.toLowerCase(), phone, street.toLowerCase(),
                ward.toLowerCase(), province.toLowerCase());
    }

    @Override
    public String toString() {
        return recipientName + " (" + phone + "), " + getFullAddress();
    }
}
