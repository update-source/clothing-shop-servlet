package com.shop.web.servlet;

import com.shop.model.DomainException;
import com.shop.model.account.Customer;
import com.shop.model.catalog.Cart;
import com.shop.model.order.Order;
import com.shop.model.order.PaymentMethod;
import com.shop.service.AddressService.AddressForm;
import com.shop.service.CheckoutService;
import com.shop.service.CheckoutService.PlaceOrderForm;
import com.shop.service.Provinces;
import com.shop.web.BaseServlet;
import com.shop.web.Flash;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/** Trang thanh toán: chọn địa chỉ, phương thức thanh toán, voucher; đặt hàng; trang cảm ơn. */
@WebServlet(urlPatterns = {"/checkout", "/checkout/success"})
public class CheckoutServlet extends BaseServlet {

    private final CheckoutService checkout = new CheckoutService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Customer customer = currentCustomer(req);
        if ("/checkout/success".equals(req.getServletPath())) {
            Long id = longParam(req, "id");
            if (id == null) {
                redirect(req, resp, "/orders");
                return;
            }
            req.setAttribute("order", checkout.orderOf(customer.getId(), id));
            render(req, resp, "checkout/success");
            return;
        }
        if (!canCheckout(req, resp, customer.getCart())) {
            return;
        }
        showForm(req, resp, customer, longParam(req, "addressId"), param(req, "voucher"));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        long customerId = sessionUser(req).getId();
        String addressChoice = param(req, "addressId");
        Long addressId = addressChoice == null || "new".equals(addressChoice) ? null : longParam(req, "addressId");
        try {
            PaymentMethod method = paymentMethod(param(req, "paymentMethod"));
            PlaceOrderForm form = new PlaceOrderForm(addressId,
                    addressId == null ? AddressServlet.form(req) : null,
                    req.getParameter("saveAddress") != null, method, param(req, "voucherCode"), param(req, "note"));
            Order order = checkout.placeOrder(customerId, form);
            if (method == PaymentMethod.VNPAY) {
                // Hàng đã được giữ; chuyển sang cổng VNPAY để trả trước.
                redirect(req, resp, "/orders/vnpay?id=" + order.getId());
                return;
            }
            flash(req, Flash.success("Đặt hàng thành công! Mã đơn của bạn là #" + order.getId() + "."));
            redirect(req, resp, "/checkout/success?id=" + order.getId());
        } catch (DomainException e) {
            req.setAttribute("error", e.getMessage());
            if (addressId == null) {
                req.setAttribute("addr", submittedAddress(req));
            }
            Customer customer = currentCustomer(req);
            if (!canCheckout(req, resp, customer.getCart())) {
                return;
            }
            showForm(req, resp, customer, addressId, param(req, "voucherCode"));
        }
    }

    private void showForm(HttpServletRequest req, HttpServletResponse resp, Customer customer, Long addressId,
                          String voucher) throws ServletException, IOException {
        req.setAttribute("preview", checkout.preview(customer, addressId, voucher));
        req.setAttribute("provinces", Provinces.ALL);
        req.setAttribute("shippingTable", checkout.shipping().table());
        req.setAttribute("methods", PaymentMethod.values());
        render(req, resp, "checkout/form");
    }

    private boolean canCheckout(HttpServletRequest req, HttpServletResponse resp, Cart cart) throws IOException {
        if (cart.isEmpty()) {
            flash(req, Flash.info("Giỏ hàng đang trống."));
            redirect(req, resp, "/cart");
            return false;
        }
        if (cart.getItems().stream().anyMatch(CartServlet::hasIssue)) {
            flash(req, Flash.error("Một số sản phẩm trong giỏ đã hết hàng hoặc ngừng bán, vui lòng điều chỉnh."));
            redirect(req, resp, "/cart");
            return false;
        }
        return true;
    }

    private static PaymentMethod paymentMethod(String value) {
        try {
            return PaymentMethod.valueOf(value);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new DomainException("Vui lòng chọn phương thức thanh toán");
        }
    }

    /** Giữ lại địa chỉ mới khách vừa nhập khi phải hiển thị lại form. */
    private static Map<String, String> submittedAddress(HttpServletRequest req) {
        Map<String, String> addr = new HashMap<>();
        for (String key : new String[]{"recipientName", "phone", "street", "ward", "province"}) {
            addr.put(key, req.getParameter(key));
        }
        return addr;
    }
}
