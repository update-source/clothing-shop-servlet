package com.shop.web.servlet;

import com.shop.service.CatalogService;
import com.shop.web.BaseServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/** Các chương trình khuyến mãi đang diễn ra và sản phẩm của từng chương trình ("Sale 11.11 gồm những gì"). */
@WebServlet("/promotions")
public class PromotionsServlet extends BaseServlet {

    private final CatalogService catalog = new CatalogService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("promotions", catalog.ongoingPromotions());
        render(req, resp, "shop/promotions");
    }
}
