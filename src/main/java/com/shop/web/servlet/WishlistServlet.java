package com.shop.web.servlet;

import com.shop.model.DomainException;
import com.shop.service.WishlistService;
import com.shop.web.BaseServlet;
import com.shop.web.Flash;
import com.shop.web.Referer;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(urlPatterns = {"/wishlist", "/wishlist/toggle", "/wishlist/remove"})
public class WishlistServlet extends BaseServlet {

    private final WishlistService wishlist = new WishlistService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!"/wishlist".equals(req.getServletPath())) {
            redirect(req, resp, "/wishlist");
            return;
        }
        req.setAttribute("products", currentCustomer(req).getWishlist());
        render(req, resp, "account/wishlist");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        long customerId = sessionUser(req).getId();
        Long productId = longParam(req, "productId");
        try {
            if (productId == null) {
                throw new DomainException("Thiếu mã sản phẩm");
            }
            if ("/wishlist/remove".equals(req.getServletPath())) {
                wishlist.remove(customerId, productId);
                flash(req, Flash.success("Đã bỏ khỏi danh sách yêu thích."));
                redirect(req, resp, "/wishlist");
                return;
            }
            boolean added = wishlist.toggle(customerId, productId);
            flash(req, Flash.success(added ? "Đã thêm vào danh sách yêu thích." : "Đã bỏ khỏi danh sách yêu thích."));
        } catch (DomainException e) {
            flash(req, Flash.error(e.getMessage()));
        }
        redirect(req, resp, Referer.path(req, "/wishlist"));
    }
}
