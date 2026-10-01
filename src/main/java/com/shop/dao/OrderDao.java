package com.shop.dao;

import com.shop.model.DomainException;
import com.shop.model.account.Address;
import com.shop.model.account.Customer;
import com.shop.model.account.Employee;
import com.shop.model.catalog.ProductVariant;
import com.shop.model.discount.Voucher;
import com.shop.model.order.Order;
import com.shop.model.order.OrderDetail;
import com.shop.model.order.OrderStatus;
import com.shop.model.order.Payment;
import com.shop.model.order.PaymentMethod;
import com.shop.model.order.PaymentStatus;
import com.shop.persistence.Jdbc;
import com.shop.persistence.Tracker;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Nạp/lưu đơn hàng cùng các dòng đơn và các lần thanh toán. Trạng thái đơn được ghi kèm điều kiện
 * "trạng thái vẫn như lúc nạp" để hai người không đồng thời chuyển một đơn theo hai hướng.
 */
public class OrderDao {

    private static final String SELECT = "SELECT o.id, o.customer_id, o.order_date, o.note, o.payment_method, "
            + "o.shipping_fee, o.status, o.voucher_id, o.handled_by, o.ship_recipient_name, o.ship_phone, "
            + "o.ship_street, o.ship_ward, o.ship_province FROM orders o";

    private record Row(long id, long customerId, LocalDateTime orderDate, String note, PaymentMethod method,
                       BigDecimal shippingFee, OrderStatus status, Long voucherId, Long handledBy, Address address) {
    }

    private record DetailRow(long id, long orderId, long variantId, BigDecimal unitPrice, int quantity) {
    }

    private static Row row(ResultSet rs) throws SQLException {
        Address address = new Address(rs.getString("ship_recipient_name"), rs.getString("ship_phone"),
                rs.getString("ship_street"), rs.getString("ship_ward"), rs.getString("ship_province"));
        return new Row(rs.getLong("id"), rs.getLong("customer_id"), Jdbc.getDateTime(rs, "order_date"),
                rs.getString("note"), PaymentMethod.valueOf(rs.getString("payment_method")),
                rs.getBigDecimal("shipping_fee"), OrderStatus.valueOf(rs.getString("status")),
                Jdbc.getLong(rs, "voucher_id"), Jdbc.getLong(rs, "handled_by"), address);
    }

    private List<Order> build(List<Row> rows, Function<Long, Customer> customerOf) {
        List<Order> orders = new ArrayList<>();
        if (rows.isEmpty()) {
            return orders;
        }
        List<Object> orderIds = rows.stream().map(r -> (Object) r.id()).collect(Collectors.toList());
        String in = Jdbc.in(orderIds);

        List<DetailRow> detailRows = Jdbc.query("SELECT id, order_id, variant_id, unit_price, quantity "
                        + "FROM order_details WHERE order_id IN (" + in + ") ORDER BY id",
                rs -> new DetailRow(rs.getLong("id"), rs.getLong("order_id"), rs.getLong("variant_id"),
                        rs.getBigDecimal("unit_price"), rs.getInt("quantity")), orderIds.toArray());
        Map<Long, ProductVariant> variants = Daos.products().findVariants(
                detailRows.stream().map(DetailRow::variantId).collect(Collectors.toCollection(LinkedHashSet::new)));

        Map<Long, List<Payment>> payments = new java.util.HashMap<>();
        Jdbc.query("SELECT id, order_id, amount, status, paid_at, transaction_no, created_at FROM payments "
                + "WHERE order_id IN (" + in + ") ORDER BY id", rs -> {
                    Payment p = new Payment(rs.getLong("id"), rs.getBigDecimal("amount"),
                            PaymentStatus.valueOf(rs.getString("status")), Jdbc.getDateTime(rs, "paid_at"),
                            rs.getString("transaction_no"), Jdbc.getDateTime(rs, "created_at"));
                    Tracker.remember(p, p.getStatus());
                    payments.computeIfAbsent(rs.getLong("order_id"), k -> new ArrayList<>()).add(p);
                    return p;
                }, orderIds.toArray());

        Map<Long, Voucher> vouchers = Daos.vouchers().findByIds(rows.stream().map(Row::voucherId)
                .filter(Objects::nonNull).collect(Collectors.toSet()));
        Map<Long, Employee> employees = Daos.users().findEmployees(rows.stream().map(Row::handledBy)
                .filter(Objects::nonNull).collect(Collectors.toSet()));

        for (Row r : rows) {
            List<OrderDetail> details = detailRows.stream().filter(d -> d.orderId() == r.id())
                    .map(d -> new OrderDetail(d.id(), variants.get(d.variantId()), d.unitPrice(), d.quantity()))
                    .collect(Collectors.toList());
            Order order = new Order(r.id(), customerOf.apply(r.customerId()), r.orderDate(), r.note(), r.method(),
                    r.shippingFee(), r.status(), r.address(), details, payments.getOrDefault(r.id(), List.of()),
                    r.voucherId() == null ? null : vouchers.get(r.voucherId()),
                    r.handledBy() == null ? null : employees.get(r.handledBy()));
            Tracker.remember(order, order.getStatus());
            orders.add(order);
        }
        return orders;
    }

    private List<Order> buildWithCustomers(List<Row> rows) {
        Map<Long, Customer> customers = Daos.users().findCustomers(
                rows.stream().map(Row::customerId).collect(Collectors.toSet()));
        return build(rows, customers::get);
    }

    // ------------------------------------------------------------------ truy vấn

    public Optional<Order> findById(long id) {
        return buildWithCustomers(Jdbc.query(SELECT + " WHERE o.id = ?", OrderDao::row, id)).stream().findFirst();
    }

    /** Lịch sử mua của khách (mới nhất trước); các đơn trỏ về chính đối tượng khách đó. */
    public List<Order> findByCustomer(Customer customer) {
        return build(Jdbc.query(SELECT + " WHERE o.customer_id = ? ORDER BY o.order_date DESC, o.id DESC",
                OrderDao::row, customer.getId()), id -> customer);
    }

    /** Danh sách đơn cho nhân viên: lọc trạng thái, tìm theo mã đơn / tên / SĐT người nhận. */
    public Page<Order> search(OrderStatus status, String keyword, int page, int pageSize) {
        StringBuilder where = new StringBuilder(" WHERE 1 = 1");
        List<Object> params = new ArrayList<>();
        if (status != null) {
            where.append(" AND o.status = ?");
            params.add(status);
        }
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.trim().replace("#", "");
            where.append(" AND (o.id = ? OR LOWER(o.ship_recipient_name) LIKE ? OR o.ship_phone LIKE ?)");
            params.add(kw.matches("\\d{1,18}") ? Long.parseLong(kw) : -1L);
            params.add("%" + kw.toLowerCase() + "%");
            params.add("%" + kw + "%");
        }
        long total = Jdbc.count("SELECT COUNT(*) FROM orders o" + where, params.toArray());
        int p = Math.max(1, page);
        List<Object> pageParams = new ArrayList<>(params);
        pageParams.add(pageSize);
        pageParams.add((p - 1) * pageSize);
        List<Row> rows = Jdbc.query(SELECT + where + " ORDER BY o.order_date DESC, o.id DESC LIMIT ? OFFSET ?",
                OrderDao::row, pageParams.toArray());
        return new Page<>(buildWithCustomers(rows), p, pageSize, total);
    }

    public List<Order> findByStatus(OrderStatus status) {
        return buildWithCustomers(Jdbc.query(SELECT + " WHERE o.status = ? ORDER BY o.order_date DESC",
                OrderDao::row, status));
    }

    /** Đơn VNPAY còn chờ, chưa có lần thanh toán thành công và đặt trước thời điểm {@code cutoff}. */
    public List<Long> findOverdueVnpayIds(LocalDateTime cutoff) {
        return Jdbc.query("SELECT o.id FROM orders o WHERE o.payment_method = ? AND o.status = ? AND o.order_date < ? "
                        + "AND NOT EXISTS (SELECT 1 FROM payments p WHERE p.order_id = o.id AND p.status = ?)",
                rs -> rs.getLong(1), PaymentMethod.VNPAY, OrderStatus.PENDING, cutoff, PaymentStatus.PAID);
    }

    public Optional<Long> findOrderIdByPayment(long paymentId) {
        return Jdbc.one("SELECT order_id FROM payments WHERE id = ?", rs -> rs.getLong(1), paymentId);
    }

    public Map<OrderStatus, Long> countByStatus() {
        Map<OrderStatus, Long> counts = new EnumMap<>(OrderStatus.class);
        for (OrderStatus s : OrderStatus.values()) {
            counts.put(s, 0L);
        }
        Jdbc.query("SELECT status, COUNT(*) AS n FROM orders GROUP BY status",
                rs -> counts.put(OrderStatus.valueOf(rs.getString("status")), rs.getLong("n")));
        return counts;
    }

    // ------------------------------------------------------------------ ghi

    /** Lưu đơn mới cùng dòng đơn, lần thanh toán, số hàng đang giữ và lượt voucher. */
    public void insert(Order o) {
        Address a = o.getShippingAddress();
        o.assignId(Jdbc.insert("INSERT INTO orders (customer_id, order_date, note, payment_method, shipping_fee, status, "
                        + "voucher_id, handled_by, ship_recipient_name, ship_phone, ship_street, ship_ward, ship_province, "
                        + "updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                o.getCustomer().getId(), o.getOrderDate(), o.getNote(), o.getPaymentMethod(), o.getShippingFee(),
                o.getStatus(), o.getVoucher() == null ? null : o.getVoucher().getId(),
                o.getHandledBy() == null ? null : o.getHandledBy().getId(), a.getRecipientName(), a.getPhone(),
                a.getStreet(), a.getWard(), a.getProvince(), LocalDateTime.now()));
        for (OrderDetail d : o.getDetails()) {
            d.assignId(Jdbc.insert("INSERT INTO order_details (order_id, variant_id, unit_price, quantity) "
                    + "VALUES (?, ?, ?, ?)", o.getId(), d.getVariant().getId(), d.getUnitPrice(), d.getQuantity()));
        }
        savePayments(o);
        saveRelated(o);
        Tracker.remember(o, o.getStatus());
    }

    /** Lưu thay đổi sau một hành động vòng đời: trạng thái, người xử lý, thanh toán, kho, voucher. */
    public void update(Order o) {
        OrderStatus original = Tracker.original(o);
        int updated = Jdbc.update("UPDATE orders SET status = ?, handled_by = ?, updated_at = ? WHERE id = ? AND status = ?",
                o.getStatus(), o.getHandledBy() == null ? null : o.getHandledBy().getId(), LocalDateTime.now(),
                o.getId(), original == null ? o.getStatus() : original);
        if (updated == 0) {
            throw new DomainException("Đơn hàng #" + o.getId() + " vừa được cập nhật bởi người khác, vui lòng tải lại");
        }
        savePayments(o);
        saveRelated(o);
        Tracker.remember(o, o.getStatus());
    }

    private void savePayments(Order o) {
        for (Payment p : o.getPayments()) {
            if (p.isNew()) {
                p.assignId(Jdbc.insert("INSERT INTO payments (order_id, amount, status, paid_at, transaction_no, created_at) "
                                + "VALUES (?, ?, ?, ?, ?, ?)", o.getId(), p.getAmount(), p.getStatus(), p.getPaidAt(),
                        p.getTransactionNo(), p.getCreatedAt()));
            } else {
                PaymentStatus original = Tracker.original(p);
                if (original != null && original != p.getStatus()) {
                    int updated = Jdbc.update("UPDATE payments SET status = ?, paid_at = ?, transaction_no = ? "
                                    + "WHERE id = ? AND status = ?", p.getStatus(), p.getPaidAt(), p.getTransactionNo(),
                            p.getId(), original);
                    if (updated == 0) {
                        throw new DomainException("Giao dịch #" + p.getId() + " đã được xử lý trước đó");
                    }
                }
            }
            Tracker.remember(p, p.getStatus());
        }
    }

    private void saveRelated(Order o) {
        Set<ProductVariant> variants = o.getDetails().stream().map(OrderDetail::getVariant)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        variants.forEach(Daos.products()::saveVariant);
        if (o.getVoucher() != null) {
            Daos.vouchers().update(o.getVoucher());
        }
    }
}
