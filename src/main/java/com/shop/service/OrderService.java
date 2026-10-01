package com.shop.service;

import com.shop.dao.Daos;
import com.shop.dao.Page;
import com.shop.model.DomainException;
import com.shop.model.account.Employee;
import com.shop.model.order.Order;
import com.shop.model.order.OrderStatus;
import com.shop.persistence.Tx;
import java.math.BigDecimal;
import java.util.Map;

/**
 * Các hành động trên vòng đời đơn hàng. Mỗi hành động nạp đơn trong một transaction, gọi đúng hành vi
 * của Order (đơn tự kiểm tra trạng thái và tự tác động lên kho, voucher, thanh toán) rồi lưu lại.
 */
public class OrderService {

    public static final int PAGE_SIZE = 15;

    /** Hành động nhân viên thực hiện trên đơn. */
    public enum StaffAction {
        CONFIRM("Xác nhận đơn"), SHIP("Giao hàng"), COMPLETE("Đã giao thành công"),
        FAIL_DELIVERY("Giao thất bại"), RETURN_GOODS("Nhận trả hàng"), CANCEL("Huỷ đơn");

        private final String label;

        StaffAction(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    /** Khách huỷ đơn của chính mình (chỉ khi chưa giao). */
    public Order cancelByCustomer(long customerId, long orderId) {
        return Tx.inTransaction(() -> {
            Order order = load(orderId);
            if (order.getCustomer().getId() != customerId) {
                throw new DomainException("Không tìm thấy đơn hàng");
            }
            order.cancel();
            Daos.orders().update(order);
            return order;
        });
    }

    /** Nhân viên xử lý đơn: xác nhận và giao được ghi nhận người chịu trách nhiệm. */
    public Order act(long employeeId, long orderId, StaffAction action) {
        return Tx.inTransaction(() -> {
            Employee employee = Daos.users().findEmployee(employeeId)
                    .orElseThrow(() -> new DomainException("Không tìm thấy nhân viên"));
            Order order = load(orderId);
            switch (action) {
                case CONFIRM -> order.confirm(employee);
                case SHIP -> order.ship(employee);
                case COMPLETE -> order.complete();
                case FAIL_DELIVERY -> order.failDelivery();
                case RETURN_GOODS -> order.returnGoods();
                case CANCEL -> order.cancel();
            }
            Daos.orders().update(order);
            return order;
        });
    }

    public Page<Order> search(OrderStatus status, String keyword, int page) {
        return Daos.orders().search(status, keyword, page, PAGE_SIZE);
    }

    public Order find(long orderId) {
        return load(orderId);
    }

    public Map<OrderStatus, Long> countByStatus() {
        return Daos.orders().countByStatus();
    }

    /** Doanh thu = tổng tiền các đơn đã hoàn tất. */
    public BigDecimal revenue() {
        return Daos.orders().findByStatus(OrderStatus.COMPLETED).stream()
                .map(Order::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    protected static Order load(long orderId) {
        return Daos.orders().findById(orderId).orElseThrow(() -> new DomainException("Không tìm thấy đơn hàng"));
    }
}
