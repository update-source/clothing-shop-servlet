package com.shop.web.servlet;

import com.shop.model.DomainException;
import com.shop.model.account.AddressBook;
import com.shop.model.account.Customer;
import com.shop.service.AddressService;
import com.shop.service.AddressService.AddressForm;
import com.shop.service.Provinces;
import com.shop.web.BaseServlet;
import com.shop.web.Flash;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/** Sổ địa chỉ: xem, thêm, thay (sửa), xoá, đặt mặc định. */
@WebServlet("/account/addresses")
public class AddressServlet extends BaseServlet {

    private final AddressService addresses = new AddressService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Customer customer = currentCustomer(req);
        AddressBook book = customer.getAddressBook();
        Long editId = longParam(req, "edit");
        if (editId != null) {
            book.findById(editId).ifPresent(a -> req.setAttribute("editing", a));
        }
        req.setAttribute("book", book);
        req.setAttribute("provinces", Provinces.ALL);
        render(req, resp, "account/addresses");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        long customerId = sessionUser(req).getId();
        String action = param(req, "action");
        Long id = longParam(req, "id");
        try {
            switch (action == null ? "" : action) {
                case "add" -> {
                    addresses.add(customerId, form(req), req.getParameter("makeDefault") != null);
                    flash(req, Flash.success("Đã thêm địa chỉ mới."));
                }
                case "replace" -> {
                    addresses.replace(customerId, requireId(id), form(req));
                    flash(req, Flash.success("Đã cập nhật địa chỉ."));
                }
                case "delete" -> {
                    addresses.remove(customerId, requireId(id));
                    flash(req, Flash.success("Đã xoá địa chỉ."));
                }
                case "default" -> {
                    addresses.setDefault(customerId, requireId(id));
                    flash(req, Flash.success("Đã đặt làm địa chỉ mặc định."));
                }
                default -> flash(req, Flash.error("Thao tác không hợp lệ"));
            }
        } catch (DomainException e) {
            flash(req, Flash.error(e.getMessage()));
        }
        redirect(req, resp, safePath(param(req, "next"), "/account/addresses"));
    }

    static AddressForm form(HttpServletRequest req) {
        return new AddressForm(param(req, "recipientName"), param(req, "phone"), param(req, "street"),
                param(req, "ward"), param(req, "province"));
    }

    private static long requireId(Long id) {
        if (id == null) {
            throw new DomainException("Thiếu mã địa chỉ");
        }
        return id;
    }
}
