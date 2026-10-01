package com.shop.service;

import com.shop.dao.Daos;
import com.shop.model.DomainException;
import com.shop.model.order.Order;
import com.shop.model.order.OrderStatus;
import com.shop.model.order.Payment;
import com.shop.model.order.PaymentStatus;
import com.shop.persistence.Tx;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Thanh toán đơn hàng qua VNPAY. Đơn tạo các lần thanh toán ({@link Order#pay()}); kết quả từ cổng được
 * kiểm chữ ký và số tiền rồi giao cho chính lần thanh toán đổi trạng thái (markPaid / markFailed).
 */
public class PaymentService {

    private final VnpayGateway gateway = new VnpayGateway();

    /** Kết quả xử lý thông báo từ cổng. */
    public record Outcome(Code code, Long orderId, String message) {

        public enum Code { PAID, FAILED, ALREADY_PROCESSED, INVALID_SIGNATURE, NOT_FOUND, INVALID_AMOUNT }

        public boolean isSuccess() {
            return code == Code.PAID;
        }

        public Code getCode() {
            return code;
        }

        public Long getOrderId() {
            return orderId;
        }

        public String getMessage() {
            return message;
        }
    }

    /**
     * Mở một lần thanh toán VNPAY cho đơn và trả về URL của cổng.
     *
     * @param newAttempt true: tạo lần thanh toán mới (thử lại); false: dùng lần chưa trả hiện có nếu có
     */
    public String startVnpay(long customerId, long orderId, boolean newAttempt, String clientIp, String baseUrl) {
        return Tx.inTransaction(() -> {
            Order order = Daos.orders().findById(orderId).orElse(null);
            if (order == null || order.getCustomer().getId() != customerId) {
                throw new DomainException("Không tìm thấy đơn hàng");
            }
            if (!order.canPayOnline()) {
                throw new DomainException("Đơn #" + orderId + " không ở trạng thái chờ thanh toán online");
            }
            Payment payment = newAttempt ? null : order.findPayment(PaymentStatus.UNPAID).orElse(null);
            if (payment == null) {
                payment = order.pay();
                Daos.orders().update(order);
            }
            return gateway.paymentUrl(payment.getId(), payment.getAmount(),
                    "Thanh toan don hang " + orderId, clientIp, baseUrl);
        });
    }

    /** Xử lý kết quả VNPAY (return URL và IPN dùng chung, xử lý lặp lại không đổi kết quả). */
    public Outcome handleVnpayResult(Map<String, String> params) {
        if (!gateway.verify(params)) {
            return new Outcome(Outcome.Code.INVALID_SIGNATURE, null, "Chữ ký giao dịch không hợp lệ");
        }
        VnpayGateway.Result result = gateway.parse(params);
        Long orderId = Daos.orders().findOrderIdByPayment(result.txnRef()).orElse(null);
        if (orderId == null) {
            return new Outcome(Outcome.Code.NOT_FOUND, null, "Không tìm thấy giao dịch");
        }
        return Tx.inTransaction(() -> {
            Order order = Daos.orders().findById(orderId).orElseThrow();
            Payment payment = order.findPayment(result.txnRef()).orElseThrow();
            if (payment.getAmount().compareTo(result.amount()) != 0) {
                return new Outcome(Outcome.Code.INVALID_AMOUNT, orderId, "Số tiền giao dịch không khớp");
            }
            if (payment.getStatus() != PaymentStatus.UNPAID) {
                boolean paid = payment.getStatus() == PaymentStatus.PAID || payment.getStatus() == PaymentStatus.REFUNDED;
                return new Outcome(Outcome.Code.ALREADY_PROCESSED, orderId,
                        paid ? "Giao dịch đã được ghi nhận trước đó" : "Giao dịch đã được xử lý trước đó");
            }
            if (!result.isSuccess()) {
                payment.markFailed();
                Daos.orders().update(order);
                return new Outcome(Outcome.Code.FAILED, orderId,
                        "Thanh toán không thành công (mã " + result.responseCode() + "). Bạn có thể thanh toán lại.");
            }
            payment.markPaid(result.transactionNo());
            String message = "Thanh toán thành công. Cửa hàng sẽ sớm xác nhận đơn của bạn.";
            if (order.getStatus() != OrderStatus.PENDING) {
                // Tiền về sau khi đơn đã huỷ (ví dụ quá hạn) — hoàn lại ngay cho khách.
                payment.refund();
                message = "Đơn đã bị huỷ trước khi thanh toán xong nên số tiền đã được hoàn lại.";
            }
            Daos.orders().update(order);
            return new Outcome(Outcome.Code.PAID, orderId, message);
        });
    }

    /** Tự huỷ các đơn VNPAY quá hạn thanh toán để nhả hàng đang giữ và trả lượt voucher. */
    public int cancelOverdueVnpayOrders() {
        Duration timeout = Duration.ofMinutes(gateway.timeoutMinutes());
        LocalDateTime now = LocalDateTime.now();
        List<Long> ids = Daos.orders().findOverdueVnpayIds(now.minus(timeout));
        int cancelled = 0;
        for (Long id : ids) {
            try {
                boolean done = Tx.inTransaction(() -> {
                    Order order = Daos.orders().findById(id).orElse(null);
                    if (order == null || !order.isPaymentOverdue(now, timeout)) {
                        return false;
                    }
                    order.cancel();
                    Daos.orders().update(order);
                    return true;
                });
                if (done) {
                    cancelled++;
                }
            } catch (DomainException e) {
                // đơn vừa được người khác xử lý — bỏ qua
            }
        }
        return cancelled;
    }

    public VnpayGateway gateway() {
        return gateway;
    }
}
