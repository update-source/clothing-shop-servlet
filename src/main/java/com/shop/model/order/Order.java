package com.shop.model.order;

import com.shop.model.Check;
import com.shop.model.DomainException;
import com.shop.model.Entity;
import com.shop.model.account.Address;
import com.shop.model.account.Customer;
import com.shop.model.account.Employee;
import com.shop.model.catalog.CartItem;
import com.shop.model.catalog.Product;
import com.shop.model.catalog.ProductVariant;
import com.shop.model.discount.Voucher;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Đơn hàng — ra đời khi khách đặt hàng từ giỏ và từ đó tự quản lý vòng đời của mình.
 * Không ai bên ngoài được sửa thẳng trạng thái; mọi thay đổi đi qua các hành động có tên rõ ràng,
 * mỗi hành động tự kiểm tra trạng thái hiện tại có cho phép không.
 */
public class Order extends Entity {

    private final Customer customer;
    private final LocalDateTime orderDate;
    private final String note;
    private final PaymentMethod paymentMethod;
    private final BigDecimal shippingFee;
    private OrderStatus status;
    private final Address shippingAddress;
    private final List<OrderDetail> details;
    private final List<Payment> payments;
    private Voucher voucher;
    private Employee handledBy;

    /**
     * Tạo đơn từ các dòng giỏ (do {@link Customer#checkout} gọi): chuyển từng dòng giỏ thành dòng đơn,
     * chốt đơn giá và giữ hàng; lưu bản địa chỉ giao riêng; nếu có voucher thì kiểm tra rồi ghi nhận lượt.
     */
    public Order(Customer customer, List<CartItem> items, Address shipTo, PaymentMethod method,
                 Voucher voucher, BigDecimal shippingFee, String note, LocalDateTime orderDate) {
        super(null);
        if (items == null || items.isEmpty()) {
            throw new DomainException("Đơn hàng phải có ít nhất một sản phẩm");
        }
        this.customer = customer;
        this.orderDate = orderDate;
        this.note = Check.optionalText(note, "Ghi chú", 500);
        this.paymentMethod = method;
        this.shippingFee = Check.nonNegative(shippingFee, "Phí vận chuyển");
        this.shippingAddress = shipTo.copy();
        this.details = new ArrayList<>();
        this.payments = new ArrayList<>();
        for (CartItem item : items) {
            addDetail(item);
        }
        if (voucher != null) {
            applyVoucher(voucher);
        }
        this.status = OrderStatus.PENDING;
    }

    /** Khôi phục từ tầng lưu trữ. */
    public Order(Long id, Customer customer, LocalDateTime orderDate, String note, PaymentMethod method,
                 BigDecimal shippingFee, OrderStatus status, Address shippingAddress,
                 List<OrderDetail> details, List<Payment> payments, Voucher voucher, Employee handledBy) {
        super(id);
        this.customer = customer;
        this.orderDate = orderDate;
        this.note = note;
        this.paymentMethod = method;
        this.shippingFee = shippingFee;
        this.status = status;
        this.shippingAddress = shippingAddress;
        this.details = new ArrayList<>(details);
        this.payments = new ArrayList<>(payments);
        this.voucher = voucher;
        this.handledBy = handledBy;
    }

    private void addDetail(CartItem item) {
        ProductVariant variant = item.getVariant();
        if (!variant.getProduct().isActive()) {
            throw new DomainException(variant.getProduct().getName() + " đã ngừng bán");
        }
        variant.reserve(item.getQuantity());
        details.add(new OrderDetail(null, variant,
                variant.getProduct().getFinalPrice(orderDate), item.getQuantity()));
    }

    private void applyVoucher(Voucher v) {
        String reason = v.whyNotUsable(customer, getSubtotal(), orderDate);
        if (reason != null || !v.isValid(orderDate) || !v.isApplicable(customer, getSubtotal())) {
            throw new DomainException(reason != null ? reason : "Không áp dụng được mã " + v.getCode());
        }
        v.redeem();
        this.voucher = v;
    }

    // ---------------------------------------------------------------- thanh toán

    /** Tạo một lần thanh toán theo đúng phương thức đã chọn. */
    public Payment pay() {
        if (isPaid()) {
            throw new DomainException("Đơn hàng đã được thanh toán");
        }
        if (paymentMethod == PaymentMethod.COD) {
            if (!payments.isEmpty()) {
                throw new DomainException("Đơn COD đã có phiếu thu, tiền được thu khi giao hàng");
            }
        } else {
            if (status != OrderStatus.PENDING) {
                throw new DomainException("Chỉ thanh toán online được khi đơn đang chờ xác nhận");
            }
            // Lần thử trước bị bỏ dở được coi là thất bại trước khi mở lần mới.
            payments.stream().filter(p -> p.getStatus() == PaymentStatus.UNPAID)
                    .forEach(Payment::markFailed);
        }
        Payment payment = new Payment(getTotal());
        payments.add(payment);
        return payment;
    }

    public boolean isPaid() {
        return payments.stream().anyMatch(Payment::isSuccess);
    }

    // ---------------------------------------------------------------- vòng đời

    public void confirm(Employee by) {
        requireEmployee(by);
        requireTransition(OrderStatus.CONFIRMED);
        if (paymentMethod == PaymentMethod.VNPAY && !isPaid()) {
            throw new DomainException("Đơn VNPAY phải thanh toán xong mới xác nhận được");
        }
        details.forEach(d -> d.getVariant().commit(d.getQuantity()));
        handledBy = by;
        status = OrderStatus.CONFIRMED;
    }

    public void ship(Employee by) {
        requireEmployee(by);
        requireTransition(OrderStatus.SHIPPING);
        handledBy = by;
        status = OrderStatus.SHIPPING;
    }

    /** Khách nhận hàng. Với COD, lần thanh toán chuyển PAID. */
    public void complete() {
        requireTransition(OrderStatus.COMPLETED);
        if (paymentMethod == PaymentMethod.COD && !isPaid()) {
            Payment cod = findPayment(PaymentStatus.UNPAID).orElseGet(() -> {
                Payment p = new Payment(getTotal());
                payments.add(p);
                return p;
            });
            cod.markPaid("COD-" + (getId() == null ? "NEW" : getId()));
        }
        status = OrderStatus.COMPLETED;
    }

    /** Giao thất bại: nhập lại kho, trả lượt voucher, hoàn tiền nếu đã trả. */
    public void failDelivery() {
        requireTransition(OrderStatus.DELIVERY_FAILED);
        restockAll();
        releaseVoucher();
        refundPaid();
        status = OrderStatus.DELIVERY_FAILED;
    }

    /** Khách trả hàng sau khi đã hoàn tất: nhập lại kho, hoàn tiền. */
    public void returnGoods() {
        requireTransition(OrderStatus.RETURNED);
        restockAll();
        refundPaid();
        status = OrderStatus.RETURNED;
    }

    /**
     * Huỷ đơn khi chưa giao. PENDING: nhả hàng đang giữ; CONFIRMED: nhập lại kho.
     * Cả hai đều trả lượt voucher và hoàn tiền nếu đã trả.
     */
    public void cancel() {
        OrderStatus from = status;
        requireTransition(OrderStatus.CANCELLED);
        if (from == OrderStatus.PENDING) {
            details.forEach(d -> d.getVariant().releaseReservation(d.getQuantity()));
        } else {
            restockAll();
        }
        releaseVoucher();
        refundPaid();
        status = OrderStatus.CANCELLED;
    }

    /** Đơn VNPAY quá hạn thanh toán sẽ tự huỷ để không chiếm hàng giữ và lượt voucher mãi. */
    public boolean isPaymentOverdue(LocalDateTime now, Duration timeout) {
        return paymentMethod == PaymentMethod.VNPAY && status == OrderStatus.PENDING
                && !isPaid() && orderDate.plus(timeout).isBefore(now);
    }

    private boolean canTransitionTo(OrderStatus next) {
        return switch (status) {
            case PENDING -> next == OrderStatus.CONFIRMED || next == OrderStatus.CANCELLED;
            case CONFIRMED -> next == OrderStatus.SHIPPING || next == OrderStatus.CANCELLED;
            case SHIPPING -> next == OrderStatus.COMPLETED || next == OrderStatus.DELIVERY_FAILED;
            case COMPLETED -> next == OrderStatus.RETURNED;
            case CANCELLED, DELIVERY_FAILED, RETURNED -> false;
        };
    }

    private void requireTransition(OrderStatus next) {
        if (!canTransitionTo(next)) {
            throw new DomainException("Không thể chuyển đơn từ \"" + status.getLabel()
                    + "\" sang \"" + next.getLabel() + "\"");
        }
    }

    private static void requireEmployee(Employee by) {
        if (by == null) {
            throw new DomainException("Cần nhân viên xử lý đơn");
        }
    }

    private void restockAll() {
        details.forEach(d -> d.getVariant().restock(d.getQuantity()));
    }

    private void releaseVoucher() {
        if (voucher != null) {
            voucher.release();
        }
    }

    private void refundPaid() {
        payments.stream().filter(Payment::isSuccess).forEach(Payment::refund);
    }

    /** Các hành động hợp lệ ở trạng thái hiện tại — để giao diện chỉ hiện nút bấm được. */
    public boolean canConfirm() {
        return canTransitionTo(OrderStatus.CONFIRMED) && (paymentMethod == PaymentMethod.COD || isPaid());
    }

    public boolean canShip() {
        return canTransitionTo(OrderStatus.SHIPPING);
    }

    public boolean canComplete() {
        return canTransitionTo(OrderStatus.COMPLETED);
    }

    public boolean canFailDelivery() {
        return canTransitionTo(OrderStatus.DELIVERY_FAILED);
    }

    public boolean canReturn() {
        return canTransitionTo(OrderStatus.RETURNED);
    }

    public boolean canCancel() {
        return canTransitionTo(OrderStatus.CANCELLED);
    }

    public boolean canPayOnline() {
        return paymentMethod == PaymentMethod.VNPAY && status == OrderStatus.PENDING && !isPaid();
    }

    // ---------------------------------------------------------------- tiền

    public BigDecimal getSubtotal() {
        return details.stream().map(OrderDetail::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getDiscount() {
        if (voucher == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal subtotal = getSubtotal();
        return voucher.calculateDiscount(subtotal).min(subtotal);
    }

    public BigDecimal getTotal() {
        return getSubtotal().subtract(getDiscount()).add(shippingFee);
    }

    // ---------------------------------------------------------------- truy vấn

    public boolean contains(Product product) {
        return details.stream().anyMatch(d -> d.getVariant().getProduct().equals(product));
    }

    /** Đơn còn giữ một lượt của voucher (lượt chỉ được trả khi huỷ hoặc giao thất bại). */
    public boolean holdsVoucherUsage(Voucher v) {
        return voucher != null && voucher.equals(v)
                && status != OrderStatus.CANCELLED && status != OrderStatus.DELIVERY_FAILED;
    }

    public Optional<Payment> findPayment(PaymentStatus paymentStatus) {
        return payments.stream().filter(p -> p.getStatus() == paymentStatus).findFirst();
    }

    public Optional<Payment> findPayment(long paymentId) {
        return payments.stream().filter(p -> p.getId() != null && p.getId() == paymentId).findFirst();
    }

    public int getItemCount() {
        return details.stream().mapToInt(OrderDetail::getQuantity).sum();
    }

    public Customer getCustomer() {
        return customer;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public String getNote() {
        return note;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public BigDecimal getShippingFee() {
        return shippingFee;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Address getShippingAddress() {
        return shippingAddress;
    }

    public List<OrderDetail> getDetails() {
        return Collections.unmodifiableList(details);
    }

    public List<Payment> getPayments() {
        return Collections.unmodifiableList(payments);
    }

    public Voucher getVoucher() {
        return voucher;
    }

    public Employee getHandledBy() {
        return handledBy;
    }
}
