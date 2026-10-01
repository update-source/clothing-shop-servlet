package com.shop.web.servlet.staff;

import com.shop.dao.Daos;
import com.shop.model.DomainException;
import com.shop.model.order.Order;
import com.shop.model.order.OrderStatus;
import com.shop.service.OrderService;
import com.shop.service.OrderService.StaffAction;
import com.shop.web.BaseServlet;
import com.shop.web.Flash;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/** Nhân viên: tổng quan, danh sách đơn, chi tiết và xử lý đơn theo vòng đời. */
@WebServlet(urlPatterns = {"/staff", "/staff/orders", "/staff/orders/detail", "/staff/orders/action"})
public class StaffOrderServlet extends BaseServlet {

    private final OrderService orders = new OrderService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        switch (req.getServletPath()) {
            case "/staff" -> {
                var counts = orders.countByStatus();
                req.setAttribute("counts", counts);
                req.setAttribute("pendingCount", counts.get(OrderStatus.PENDING));
                req.setAttribute("revenue", orders.revenue());
                req.setAttribute("customerCount", Daos.users().countCustomers());
                req.setAttribute("productCount", Daos.products().countActive());
                req.setAttribute("latest", orders.search(null, null, 1).items());
                render(req, resp, "staff/dashboard");
            }
            case "/staff/orders/detail" -> {
                Long id = longParam(req, "id");
                if (id == null) {
                    redirect(req, resp, "/staff/orders");
                    return;
                }
                try {
                    req.setAttribute("order", orders.find(id));
                } catch (DomainException e) {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
                render(req, resp, "staff/order-detail");
            }
            case "/staff/orders/action" -> redirect(req, resp, "/staff/orders");
            default -> {
                OrderStatus status = status(param(req, "status"));
                String q = param(req, "q");
                req.setAttribute("result", orders.search(status, q, Math.max(1, intParam(req, "page", 1))));
                req.setAttribute("statuses", OrderStatus.values());
                req.setAttribute("counts", orders.countByStatus());
                req.setAttribute("selectedStatus", status);
                req.setAttribute("pageQuery", (status == null ? "" : "status=" + status + "&")
                        + (q == null ? "" : "q=" + URLEncoder.encode(q, StandardCharsets.UTF_8) + "&"));
                render(req, resp, "staff/orders");
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long id = longParam(req, "id");
        if (id == null) {
            redirect(req, resp, "/staff/orders");
            return;
        }
        try {
            StaffAction action = StaffAction.valueOf(param(req, "action"));
            Order order = orders.act(currentEmployee(req).getId(), id, action);
            flash(req, Flash.success(action.getLabel() + ": đơn #" + id + " chuyển sang \""
                    + order.getStatus().getLabel() + "\"."));
        } catch (IllegalArgumentException | NullPointerException e) {
            flash(req, Flash.error("Thao tác không hợp lệ"));
        } catch (DomainException e) {
            flash(req, Flash.error(e.getMessage()));
        }
        redirect(req, resp, "/staff/orders/detail?id=" + id);
    }

    private static OrderStatus status(String value) {
        try {
            return value == null ? null : OrderStatus.valueOf(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
