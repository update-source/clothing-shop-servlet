package com.shop.web.servlet;

import com.shop.dao.Daos;
import com.shop.model.account.Customer;
import com.shop.model.catalog.Product;
import com.shop.service.CatalogService;
import com.shop.web.BaseServlet;
import com.shop.web.SessionUser;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/** Chi tiết sản phẩm: chọn biến thể size + màu, giá cuối, tồn kho, đánh giá, sản phẩm liên quan. */
@WebServlet("/product")
public class ProductServlet extends BaseServlet {

    private final CatalogService catalog = new CatalogService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long id = longParam(req, "id");
        Product product = id == null ? null : catalog.product(id).orElse(null);
        if (product == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        req.setAttribute("product", product);
        req.setAttribute("related", catalog.related(product, 4));

        SessionUser user = sessionUser(req);
        if (user != null && user.isCustomer()) {
            Customer customer = Daos.users().findCustomer(user.getId()).orElse(null);
            if (customer != null) {
                req.setAttribute("inWishlist", Daos.wishlists().contains(customer.getId(), product.getId()));
                product.findReviewBy(customer).ifPresentOrElse(
                        r -> req.setAttribute("myReview", r),
                        () -> req.setAttribute("canReview", customer.hasPurchased(product)));
            }
        }
        render(req, resp, "shop/product");
    }
}
