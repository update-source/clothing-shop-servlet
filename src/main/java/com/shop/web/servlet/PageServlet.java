package com.shop.web.servlet;

import com.shop.web.BaseServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/** Các trang tĩnh: giới thiệu, liên hệ. */
@WebServlet(urlPatterns = {"/about", "/contact"})
public class PageServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        render(req, resp, req.getServletPath().substring(1));
    }
}
