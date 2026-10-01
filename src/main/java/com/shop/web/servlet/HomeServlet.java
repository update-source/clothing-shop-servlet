package com.shop.web.servlet;

import com.shop.service.CatalogService;
import com.shop.web.BaseServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(urlPatterns = {"", "/home"})
public class HomeServlet extends BaseServlet {

    private final CatalogService catalog = new CatalogService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("featured", catalog.featured(8));
        req.setAttribute("promotions", catalog.ongoingPromotions());
        render(req, resp, "home");
    }
}
