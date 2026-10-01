package com.shop.web.servlet.admin;

import com.shop.model.DomainException;
import com.shop.model.catalog.Product;
import com.shop.service.AdminCatalogService;
import com.shop.service.ImageStorage;
import com.shop.web.BaseServlet;
import com.shop.web.Flash;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;

/** Quản lý sản phẩm: danh sách, tạo mới, sửa thông tin/ảnh, đổi giá, ngừng bán, thêm biến thể, nhập kho. */
@WebServlet(urlPatterns = {"/admin", "/admin/products", "/admin/products/new", "/admin/products/edit",
        "/admin/products/price", "/admin/products/discontinue", "/admin/products/variant", "/admin/products/restock"})
@MultipartConfig(maxFileSize = 5 * 1024 * 1024, maxRequestSize = 6 * 1024 * 1024)
public class AdminProductServlet extends BaseServlet {

    private final AdminCatalogService catalog = new AdminCatalogService();
    private final ImageStorage images = new ImageStorage();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        switch (req.getServletPath()) {
            case "/admin" -> redirect(req, resp, "/staff");
            case "/admin/products/new" -> {
                req.setAttribute("categories", catalog.categories());
                render(req, resp, "admin/product-form");
            }
            case "/admin/products/edit" -> {
                Long id = longParam(req, "id");
                try {
                    req.setAttribute("product", catalog.product(id == null ? -1 : id));
                } catch (DomainException e) {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
                req.setAttribute("categories", catalog.categories());
                render(req, resp, "admin/product-form");
            }
            case "/admin/products" -> {
                req.setAttribute("products", catalog.products(param(req, "q")));
                render(req, resp, "admin/products");
            }
            default -> redirect(req, resp, "/admin/products");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        Long id = longParam(req, "id");
        try {
            switch (path) {
                case "/admin/products/new" -> {
                    Product p = catalog.createProduct(param(req, "name"), param(req, "description"),
                            moneyParam(req, "basePrice"), longParam(req, "categoryId"), uploadedImage(req));
                    flash(req, Flash.success("Đã tạo sản phẩm. Hãy thêm các biến thể size/màu để bắt đầu bán."));
                    redirect(req, resp, "/admin/products/edit?id=" + p.getId());
                    return;
                }
                case "/admin/products/edit" -> {
                    catalog.updateInfo(require(id), param(req, "name"), param(req, "description"),
                            longParam(req, "categoryId"), uploadedImage(req));
                    flash(req, Flash.success("Đã cập nhật thông tin sản phẩm."));
                }
                case "/admin/products/price" -> {
                    catalog.changePrice(require(id), moneyParam(req, "basePrice"));
                    flash(req, Flash.success("Đã đổi giá gốc. Giá trong giỏ của khách cập nhật ngay; đơn cũ giữ giá đã chốt."));
                }
                case "/admin/products/discontinue" -> {
                    catalog.discontinue(require(id));
                    flash(req, Flash.success("Sản phẩm đã ngừng bán. Các đơn cũ vẫn giữ nguyên."));
                }
                case "/admin/products/variant" -> {
                    catalog.addVariant(require(id), param(req, "size"), param(req, "color"), intParam(req, "stock", 0));
                    flash(req, Flash.success("Đã thêm biến thể."));
                }
                case "/admin/products/restock" -> {
                    Long variantId = longParam(req, "variantId");
                    catalog.restock(require(id), variantId == null ? -1 : variantId, intParam(req, "qty", 0));
                    flash(req, Flash.success("Đã nhập thêm hàng vào kho."));
                }
                default -> {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
            }
        } catch (DomainException e) {
            flash(req, Flash.error(e.getMessage()));
            if ("/admin/products/new".equals(path)) {
                redirect(req, resp, "/admin/products/new");
                return;
            }
        }
        redirect(req, resp, id == null ? "/admin/products" : "/admin/products/edit?id=" + id);
    }

    /** Ảnh mới (nếu có chọn tệp), lưu vào thư mục upload. */
    private String uploadedImage(HttpServletRequest req) throws IOException, ServletException {
        String contentType = req.getContentType();
        if (contentType == null || !contentType.toLowerCase().startsWith("multipart/")) {
            return null;
        }
        Part part = req.getPart("image");
        if (part == null || part.getSize() == 0) {
            return null;
        }
        try (InputStream in = part.getInputStream()) {
            return images.save(part.getContentType(), part.getSize(), in);
        }
    }

    private static long require(Long id) {
        if (id == null) {
            throw new DomainException("Thiếu mã sản phẩm");
        }
        return id;
    }
}
