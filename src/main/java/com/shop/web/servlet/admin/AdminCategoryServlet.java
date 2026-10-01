package com.shop.web.servlet.admin;

import com.shop.dao.Daos;
import com.shop.model.DomainException;
import com.shop.service.AdminCatalogService;
import com.shop.web.BaseServlet;
import com.shop.web.Flash;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/** Quản lý danh mục: thêm, đổi tên/chuyển danh mục cha, xoá danh mục rỗng. */
@WebServlet("/admin/categories")
public class AdminCategoryServlet extends BaseServlet {

    private final AdminCatalogService catalog = new AdminCatalogService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("categories", catalog.categories());
        req.setAttribute("counts", Daos.categories().countActiveProductsRecursive());
        Long editId = longParam(req, "edit");
        if (editId != null) {
            Daos.categories().findById(editId).ifPresent(c -> req.setAttribute("editing", c));
        }
        render(req, resp, "admin/categories");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String action = param(req, "action");
        Long id = longParam(req, "id");
        Long parentId = longParam(req, "parentId");
        try {
            switch (action == null ? "" : action) {
                case "create" -> {
                    catalog.createCategory(param(req, "name"), parentId);
                    flash(req, Flash.success("Đã thêm danh mục."));
                }
                case "update" -> {
                    catalog.updateCategory(require(id), param(req, "name"), parentId);
                    flash(req, Flash.success("Đã cập nhật danh mục."));
                }
                case "delete" -> {
                    catalog.deleteCategory(require(id));
                    flash(req, Flash.success("Đã xoá danh mục."));
                }
                default -> flash(req, Flash.error("Thao tác không hợp lệ"));
            }
        } catch (DomainException e) {
            flash(req, Flash.error(e.getMessage()));
        }
        redirect(req, resp, "/admin/categories");
    }

    private static long require(Long id) {
        if (id == null) {
            throw new DomainException("Thiếu mã danh mục");
        }
        return id;
    }
}
