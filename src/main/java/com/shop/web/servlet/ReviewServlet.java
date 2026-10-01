package com.shop.web.servlet;

import com.shop.model.DomainException;
import com.shop.service.ReviewService;
import com.shop.web.BaseServlet;
import com.shop.web.Flash;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/reviews/save")
public class ReviewServlet extends BaseServlet {

    private final ReviewService reviews = new ReviewService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        redirect(req, resp, "/shop");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long productId = longParam(req, "productId");
        if (productId == null) {
            redirect(req, resp, "/shop");
            return;
        }
        try {
            boolean created = reviews.save(sessionUser(req).getId(), productId, intParam(req, "rating", 0),
                    param(req, "comment"));
            flash(req, Flash.success(created ? "Cảm ơn bạn đã đánh giá sản phẩm!" : "Đã cập nhật đánh giá của bạn."));
        } catch (DomainException e) {
            flash(req, Flash.error(e.getMessage()));
        }
        redirect(req, resp, "/product?id=" + productId + "#reviews");
    }
}
