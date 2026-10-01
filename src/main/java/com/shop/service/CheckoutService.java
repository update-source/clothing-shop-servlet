package com.shop.service;

import com.shop.dao.Daos;
import com.shop.model.DomainException;
import com.shop.model.account.Address;
import com.shop.model.account.AddressBook;
import com.shop.model.account.Customer;
import com.shop.model.catalog.Cart;
import com.shop.model.discount.Voucher;
import com.shop.model.order.Order;
import com.shop.model.order.PaymentMethod;
import com.shop.persistence.Tx;
import com.shop.service.AddressService.AddressForm;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Thanh toán giỏ hàng — giống màn hình thanh toán thật: khách chọn cùng lúc địa chỉ giao,
 * phương thức thanh toán và voucher (nếu có) rồi đặt hàng.
 */
public class CheckoutService {

    private final ShippingService shipping = new ShippingService();

    /** Số liệu xem trước ở trang thanh toán. */
    public record Preview(Cart cart, AddressBook book, Address address, BigDecimal subtotal, BigDecimal shippingFee,
                          String voucherCode, Voucher voucher, String voucherError, BigDecimal discount,
                          BigDecimal total, List<Voucher> usableVouchers) {

        public Cart getCart() { return cart; }
        public AddressBook getBook() { return book; }
        public Address getAddress() { return address; }
        public BigDecimal getSubtotal() { return subtotal; }
        public BigDecimal getShippingFee() { return shippingFee; }
        public String getVoucherCode() { return voucherCode; }
        public Voucher getVoucher() { return voucher; }
        public String getVoucherError() { return voucherError; }
        public BigDecimal getDiscount() { return discount; }
        public BigDecimal getTotal() { return total; }
        public List<Voucher> getUsableVouchers() { return usableVouchers; }
    }

    /** Dữ liệu khách gửi khi bấm "Đặt hàng". {@code addressId == null} nghĩa là giao đến địa chỉ mới. */
    public record PlaceOrderForm(Long addressId, AddressForm newAddress, boolean saveNewAddress,
                                 PaymentMethod method, String voucherCode, String note) {
    }

    public Preview preview(Customer customer, Long addressId, String voucherCode) {
        Cart cart = customer.getCart();
        AddressBook book = customer.getAddressBook();
        Address address = addressId == null ? book.getDefault() : book.findById(addressId).orElse(book.getDefault());
        BigDecimal subtotal = cart.getTotal();
        BigDecimal fee = shipping.quote(address == null ? null : address.getProvince());
        LocalDateTime now = LocalDateTime.now();

        Voucher voucher = null;
        String error = null;
        BigDecimal discount = BigDecimal.ZERO;
        String code = voucherCode == null || voucherCode.isBlank() ? null : voucherCode.trim().toUpperCase();
        if (code != null) {
            voucher = Daos.vouchers().findByCode(code).orElse(null);
            if (voucher == null) {
                error = "Mã giảm giá " + code + " không tồn tại";
            } else {
                error = voucher.whyNotUsable(customer, subtotal, now);
                if (error == null) {
                    discount = voucher.calculateDiscount(subtotal).min(subtotal);
                } else {
                    voucher = null;
                }
            }
        }
        List<Voucher> usable = Daos.vouchers().findAll().stream()
                .filter(v -> v.whyNotUsable(customer, subtotal, now) == null).toList();
        BigDecimal total = subtotal.subtract(discount).add(fee);
        return new Preview(cart, book, address, subtotal, fee, code, voucher, error, discount, total, usable);
    }

    /**
     * Đặt hàng: đơn giữ hàng cho từng biến thể, chốt đơn giá, lưu bản địa chỉ giao, ghi nhận lượt voucher;
     * giỏ được làm trống; đơn tạo lần thanh toán đầu tiên theo phương thức đã chọn.
     */
    public Order placeOrder(long customerId, PlaceOrderForm form) {
        return Tx.inTransaction(() -> {
            Customer customer = Daos.users().findCustomer(customerId)
                    .orElseThrow(() -> new DomainException("Không tìm thấy khách hàng"));
            AddressBook book = customer.getAddressBook();
            Address address;
            if (form.addressId() != null) {
                address = book.findById(form.addressId())
                        .orElseThrow(() -> new DomainException("Địa chỉ giao hàng không có trong sổ"));
            } else {
                if (form.newAddress() == null) {
                    throw new DomainException("Vui lòng chọn địa chỉ giao hàng");
                }
                address = form.newAddress().toAddress();
                if (form.saveNewAddress() && !book.getAddresses().contains(address)) {
                    book.add(address);
                    Daos.addresses().saveBook(customerId, book);
                }
            }
            Voucher voucher = null;
            if (form.voucherCode() != null && !form.voucherCode().isBlank()) {
                voucher = Daos.vouchers().findByCode(form.voucherCode())
                        .orElseThrow(() -> new DomainException("Mã giảm giá " + form.voucherCode().trim().toUpperCase()
                                + " không tồn tại"));
            }
            BigDecimal fee = shipping.quote(address.getProvince());
            Order order = customer.checkout(address, form.method(), voucher, fee, form.note());
            order.pay();
            Daos.orders().insert(order);
            Daos.carts().save(customerId, customer.getCart());
            return order;
        });
    }

    /** Đơn của chính khách (dùng cho trang cảm ơn / chi tiết). */
    public Order orderOf(long customerId, long orderId) {
        Order order = Daos.orders().findById(orderId).orElse(null);
        if (order == null || order.getCustomer().getId() != customerId) {
            throw new DomainException("Không tìm thấy đơn hàng");
        }
        return order;
    }

    public ShippingService shipping() {
        return shipping;
    }
}
