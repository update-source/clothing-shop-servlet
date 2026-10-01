package com.shop.web.servlet.admin;

import com.shop.dao.Daos;
import com.shop.model.DomainException;
import com.shop.model.discount.Promotion;
import com.shop.service.AdminDiscountService;
import com.shop.web.BaseServlet;
import com.shop.web.Flash;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/** Quản lý chương trình khuyến mãi: tạo, đổi tên, thêm/bớt sản phẩm, xoá. */
@WebServlet(urlPatterns = {"/admin/promotions", "/admin/promotions/edit"})
public class AdminPromotionServlet extends BaseServlet {

    private final AdminDiscountService discounts = new AdminDiscountService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if ("/admin/promotions/edit".equals(req.getServletPath())) {
            Long id = longParam(req, "id");
            try {
                req.setAttribute("promotion", discounts.promotion(id == null ? -1 : id));
            } catch (DomainException e) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            req.setAttribute("allProducts", Daos.products().findAllForAdmin(null));
            render(req, resp, "admin/promotion-edit");
            return;
        }
        req.setAttribute("promotions", discounts.promotions());
        render(req, resp, "admin/promotions");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String action = param(req, "action");
        Long id = longParam(req, "id");
        String back = id == null ? "/admin/promotions" : "/admin/promotions/edit?id=" + id;
        try {
            switch (action == null ? "" : action) {
                case "create" -> {
                    Promotion p = discounts.createPromotion(param(req, "name"), DiscountForms.policy(req),
                            DiscountForms.dateTime(req, "start", "thời gian bắt đầu"),
                            DiscountForms.dateTime(req, "end", "thời gian kết thúc"));
                    flash(req, Flash.success("Đã tạo chương trình. Hãy thêm sản phẩm áp dụng."));
                    back = "/admin/promotions/edit?id=" + p.getId();
                }
                case "rename" -> {
                    discounts.renamePromotion(require(id), param(req, "name"));
                    flash(req, Flash.success("Đã đổi tên chương trình."));
                }
                case "addProduct" -> {
                    discounts.addProduct(require(id), require(longParam(req, "productId")));
                    flash(req, Flash.success("Đã thêm sản phẩm vào chương trình."));
                }
                case "removeProduct" -> {
                    discounts.removeProduct(require(id), require(longParam(req, "productId")));
                    flash(req, Flash.success("Đã bỏ sản phẩm khỏi chương trình."));
                }
                case "delete" -> {
                    discounts.deletePromotion(require(id));
                    flash(req, Flash.success("Đã xoá chương trình."));
                    back = "/admin/promotions";
                }
                default -> flash(req, Flash.error("Thao tác không hợp lệ"));
            }
        } catch (DomainException e) {
            flash(req, Flash.error(e.getMessage()));
        }
        redirect(req, resp, back);
    }

    private static long require(Long id) {
        if (id == null) {
            throw new DomainException("Thiếu mã");
        }
        return id;
    }
}
