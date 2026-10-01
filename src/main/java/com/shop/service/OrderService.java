package com.shop.service;

import com.shop.dao.Daos;
import com.shop.model.DomainException;
import com.shop.model.order.Order;
import com.shop.persistence.Tx;

/**
 * Các hành động trên vòng đời đơn hàng. Mỗi hành động nạp đơn trong một transaction, gọi đúng hành vi
 * của Order (đơn tự kiểm tra trạng thái và tự tác động lên kho, voucher, thanh toán) rồi lưu lại.
 */
public class OrderService {

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

    protected static Order load(long orderId) {
        return Daos.orders().findById(orderId).orElseThrow(() -> new DomainException("Không tìm thấy đơn hàng"));
    }
}
