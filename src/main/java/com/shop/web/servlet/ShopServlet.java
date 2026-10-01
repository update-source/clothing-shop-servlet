package com.shop.web.servlet;

import com.shop.service.CatalogService;
import com.shop.service.CatalogService.ShopQuery;
import com.shop.web.BaseServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/** Trang cửa hàng: lọc theo danh mục (gồm danh mục con), từ khoá, size, màu, khoảng giá; sắp xếp; phân trang. */
@WebServlet("/shop")
public class ShopServlet extends BaseServlet {

    private static final Set<String> SORTS = Set.of("newest", "name_asc", "name_desc", "price_asc", "price_desc");

    private final CatalogService catalog = new CatalogService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long categoryId = longParam(req, "category");
        String sortParam = param(req, "sort");
        String sort = sortParam != null && SORTS.contains(sortParam) ? sortParam : "newest";
        Set<String> sizes = values(req, "size");
        Set<String> colors = values(req, "color");
        BigDecimal minPrice = moneyParam(req, "minPrice");
        BigDecimal maxPrice = moneyParam(req, "maxPrice");
        ShopQuery query = new ShopQuery(categoryId, param(req, "q"), sizes, colors, minPrice, maxPrice, sort,
                Math.max(1, intParam(req, "page", 1)));

        req.setAttribute("result", catalog.search(query));
        catalog.category(categoryId).ifPresent(c -> req.setAttribute("selectedCategory", c));
        req.setAttribute("counts", catalog.productCounts());
        req.setAttribute("allSizes", catalog.sizes());
        req.setAttribute("allColors", catalog.colors());
        req.setAttribute("selectedSizes", sizes);
        req.setAttribute("selectedColors", colors);
        req.setAttribute("sort", sort);
        req.setAttribute("minPrice", minPrice);
        req.setAttribute("maxPrice", maxPrice);
        req.setAttribute("pageQuery", queryWithoutPage(req));
        render(req, resp, "shop/list");
    }

    private static Set<String> values(HttpServletRequest req, String name) {
        String[] raw = req.getParameterValues(name);
        if (raw == null) {
            return new LinkedHashSet<>();
        }
        return Arrays.stream(raw).map(String::trim).filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /** Chuỗi truy vấn hiện tại bỏ tham số page — dùng cho link phân trang. */
    private static String queryWithoutPage(HttpServletRequest req) {
        return req.getParameterMap().entrySet().stream()
                .filter(e -> !"page".equals(e.getKey()))
                .flatMap(e -> List.of(e.getValue()).stream()
                        .filter(v -> v != null && !v.isBlank())
                        .map(v -> enc(e.getKey()) + "=" + enc(v)))
                .collect(Collectors.joining("&"));
    }

    private static String enc(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }
}
