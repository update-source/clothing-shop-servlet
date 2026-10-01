package com.shop.web.servlet.admin;

import com.shop.model.DomainException;
import com.shop.model.account.CustomerLevel;
import com.shop.service.AdminDiscountService;
import com.shop.web.BaseServlet;
import com.shop.web.Flash;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.EnumSet;
import java.util.Set;

/** Quản lý voucher: tạo mã mới, bật/tắt. Cách tính giảm và thời gian hiệu lực không sửa sau khi tạo. */
@WebServlet("/admin/vouchers")
public class AdminVoucherServlet extends BaseServlet {

    private final AdminDiscountService discounts = new AdminDiscountService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("vouchers", discounts.vouchers());
        req.setAttribute("levels", CustomerLevel.values());
        render(req, resp, "admin/vouchers");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            if ("toggle".equals(param(req, "action"))) {
                Long id = longParam(req, "id");
                if (id == null) {
                    throw new DomainException("Thiếu mã voucher");
                }
                boolean active = discounts.toggleVoucher(id);
                flash(req, Flash.success(active ? "Đã bật voucher." : "Đã tắt voucher."));
            } else {
                Set<CustomerLevel> tiers = EnumSet.noneOf(CustomerLevel.class);
                String[] selected = req.getParameterValues("tiers");
                if (selected != null) {
                    for (String t : selected) {
                        tiers.add(CustomerLevel.valueOf(t));
                    }
                }
                discounts.createVoucher(param(req, "code"), tiers, DiscountForms.money(req, "minOrderValue"),
                        intParam(req, "usageLimit", 0), intParam(req, "perCustomerLimit", 0), DiscountForms.policy(req),
                        DiscountForms.dateTime(req, "start", "thời gian bắt đầu"),
                        DiscountForms.dateTime(req, "end", "thời gian kết thúc"));
                flash(req, Flash.success("Đã tạo voucher."));
            }
        } catch (IllegalArgumentException e) {
            flash(req, Flash.error("Hạng khách không hợp lệ"));
        } catch (DomainException e) {
            flash(req, Flash.error(e.getMessage()));
        }
        redirect(req, resp, "/admin/vouchers");
    }
}
