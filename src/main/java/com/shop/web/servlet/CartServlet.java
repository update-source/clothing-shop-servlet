package com.shop.web.servlet;

import com.shop.model.DomainException;
import com.shop.model.catalog.Cart;
import com.shop.model.catalog.CartItem;
import com.shop.model.catalog.ProductVariant;
import com.shop.service.CartService;
import com.shop.web.BaseServlet;
import com.shop.web.Flash;
import com.shop.web.Referer;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/** Giỏ hàng: xem, thêm, cập nhật số lượng, xoá dòng. */
@WebServlet(urlPatterns = {"/cart", "/cart/add", "/cart/update", "/cart/remove"})
public class CartServlet extends BaseServlet {

    private final CartService carts = new CartService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!"/cart".equals(req.getServletPath())) {
            redirect(req, resp, "/cart");
            return;
        }
        Cart cart = currentCustomer(req).getCart();
        boolean hasIssues = cart.getItems().stream().anyMatch(CartServlet::hasIssue);
        req.setAttribute("cart", cart);
        req.setAttribute("hasIssues", hasIssues);
        render(req, resp, "cart/view");
    }

    /** Dòng giỏ không đặt được nữa: sản phẩm ngừng bán hoặc không còn đủ hàng. */
    static boolean hasIssue(CartItem item) {
        ProductVariant v = item.getVariant();
        return !v.getProduct().isActive() || item.getQuantity() > v.getAvailableQuantity();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        long customerId = sessionUser(req).getId();
        String path = req.getServletPath();
        try {
            switch (path) {
                case "/cart/add" -> {
                    Long variantId = longParam(req, "variantId");
                    if (variantId == null) {
                        throw new DomainException("Vui lòng chọn màu và size");
                    }
                    ProductVariant variant = carts.add(customerId, variantId, intParam(req, "qty", 1));
                    if (req.getParameter("buyNow") != null) {
                        redirect(req, resp, "/checkout");
                        return;
                    }
                    flash(req, Flash.success("Đã thêm " + variant.getProduct().getName() + " ("
                            + variant.getLabel() + ") vào giỏ hàng."));
                    redirect(req, resp, "/product?id=" + variant.getProduct().getId());
                    return;
                }
                case "/cart/update" -> {
                    Map<Long, Integer> quantities = new HashMap<>();
                    req.getParameterMap().forEach((name, values) -> {
                        if (name.startsWith("qty_") && values.length > 0) {
                            try {
                                quantities.put(Long.parseLong(name.substring(4)), Integer.parseInt(values[0].trim()));
                            } catch (NumberFormatException ignored) {
                                // bỏ qua ô nhập sai định dạng
                            }
                        }
                    });
                    carts.update(customerId, quantities);
                    flash(req, Flash.success("Đã cập nhật giỏ hàng."));
                }
                case "/cart/remove" -> {
                    Long variantId = longParam(req, "variantId");
                    if (variantId != null) {
                        carts.remove(customerId, variantId);
                        flash(req, Flash.success("Đã bỏ sản phẩm khỏi giỏ."));
                    }
                }
                default -> resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (DomainException e) {
            flash(req, Flash.error(e.getMessage()));
            if ("/cart/add".equals(path) && req.getHeader("Referer") != null) {
                redirect(req, resp, Referer.path(req, "/cart"));
                return;
            }
        }
        redirect(req, resp, "/cart");
    }
}
