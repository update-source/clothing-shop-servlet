package com.shop.model.order;

import com.shop.model.DomainException;
import com.shop.model.Entity;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Một lần khách trả tiền cho một đơn. Một đơn có thể có nhiều lần vì giao dịch VNPAY có thể
 * thất bại và khách thử lại. Thanh toán tự đổi trạng thái qua các hành động của chính nó.
 */
public class Payment extends Entity {

    private final BigDecimal amount;
    private PaymentStatus status;
    private LocalDateTime paidAt;
    private String transactionNo;
    private final LocalDateTime createdAt;

    /** Chỉ đơn hàng tạo ra thanh toán ({@link Order#pay()}). */
    Payment(BigDecimal amount) {
        this(null, amount, PaymentStatus.UNPAID, null, null, LocalDateTime.now());
    }

    public Payment(Long id, BigDecimal amount, PaymentStatus status, LocalDateTime paidAt,
                   String transactionNo, LocalDateTime createdAt) {
        super(id);
        this.amount = amount;
        this.status = status;
        this.paidAt = paidAt;
        this.transactionNo = transactionNo;
        this.createdAt = createdAt;
    }

    /** Cổng VNPAY báo thành công, hoặc shipper đã thu tiền COD. */
    public void markPaid(String transactionNo) {
        if (status != PaymentStatus.UNPAID) {
            throw new DomainException("Lần thanh toán này đã được xử lý (" + status.getLabel() + ")");
        }
        this.status = PaymentStatus.PAID;
        this.transactionNo = transactionNo;
        this.paidAt = LocalDateTime.now();
    }

    /** Giao dịch bị từ chối. */
    public void markFailed() {
        if (status != PaymentStatus.UNPAID) {
            throw new DomainException("Lần thanh toán này đã được xử lý (" + status.getLabel() + ")");
        }
        this.status = PaymentStatus.FAILED;
    }

    /** Hoàn tiền — chỉ được khi đang ở trạng thái PAID. */
    public void refund() {
        if (status != PaymentStatus.PAID) {
            throw new DomainException("Chỉ hoàn tiền được cho lần thanh toán đã thành công");
        }
        this.status = PaymentStatus.REFUNDED;
    }

    public boolean isSuccess() {
        return status == PaymentStatus.PAID;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public String getTransactionNo() {
        return transactionNo;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
