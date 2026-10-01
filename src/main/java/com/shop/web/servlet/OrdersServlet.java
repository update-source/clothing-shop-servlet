package com.shop.web.servlet;

import com.shop.model.DomainException;
import com.shop.model.account.Customer;
import com.shop.model.order.Order;
import com.shop.model.order.OrderStatus;
import com.shop.service.OrderService;
import com.shop.web.BaseServlet;
import com.shop.web.Flash;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/** Đơn hàng của khách: lịch sử, chi tiết, huỷ đơn. */
@WebServlet(urlPatterns = {"/orders", "/orders/detail", "/orders/cancel"})
public class OrdersServlet extends BaseServlet {

    private final OrderService orders = new OrderService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Customer customer = currentCustomer(req);
        if ("/orders/detail".equals(req.getServletPath())) {
            Long id = longParam(req, "id");
            Order order = id == null ? null : customer.getOrders().stream()
                    .filter(o -> o.getId().equals(id)).findFirst().orElse(null);
            if (order == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            req.setAttribute("order", order);
            render(req, resp, "orders/detail");
            return;
        }
        if ("/orders/cancel".equals(req.getServletPath())) {
            redirect(req, resp, "/orders");
            return;
        }
        OrderStatus status = status(param(req, "status"));
        List<Order> list = customer.getOrders().stream()
                .filter(o -> status == null || o.getStatus() == status).toList();
        req.setAttribute("orders", list);
        req.setAttribute("statuses", OrderStatus.values());
        req.setAttribute("selectedStatus", status);
        render(req, resp, "orders/list");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long id = longParam(req, "id");
        if (id == null) {
            redirect(req, resp, "/orders");
            return;
        }
        try {
            orders.cancelByCustomer(sessionUser(req).getId(), id);
            flash(req, Flash.success("Đã huỷ đơn #" + id + ". Hàng đã được trả lại kho"
                    + " và lượt dùng mã giảm giá (nếu có) đã được hoàn lại."));
        } catch (DomainException e) {
            flash(req, Flash.error(e.getMessage()));
        }
        redirect(req, resp, "/orders/detail?id=" + id);
    }

    static OrderStatus status(String value) {
        try {
            return value == null ? null : OrderStatus.valueOf(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
