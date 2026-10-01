package com.shop.dao;

import com.shop.model.account.Address;
import com.shop.model.account.AddressBook;
import com.shop.persistence.Jdbc;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AddressDao {

    public AddressBook loadBook(long customerId) {
        List<Address> addresses = Jdbc.query("SELECT id, recipient_name, phone, street, ward, province "
                        + "FROM addresses WHERE customer_id = ? ORDER BY id",
                rs -> new Address(rs.getLong("id"), rs.getString("recipient_name"), rs.getString("phone"),
                        rs.getString("street"), rs.getString("ward"), rs.getString("province")),
                customerId);
        Long defaultId = Jdbc.one("SELECT default_address_id FROM address_books WHERE customer_id = ?",
                rs -> Jdbc.getLong(rs, "default_address_id"), customerId).orElse(null);
        Address defaultAddress = addresses.stream()
                .filter(a -> a.getId().equals(defaultId)).findFirst().orElse(null);
        return new AddressBook(addresses, defaultAddress);
    }

    /** Đồng bộ sổ địa chỉ: chèn địa chỉ mới, xoá địa chỉ đã bỏ, cập nhật liên kết mặc định. */
    public void saveBook(long customerId, AddressBook book) {
        Set<Long> existing = new HashSet<>(Jdbc.query("SELECT id FROM addresses WHERE customer_id = ?",
                rs -> rs.getLong("id"), customerId));
        Set<Long> keep = new HashSet<>();
        for (Address a : book.getAddresses()) {
            if (a.getId() == null) {
                a.assignId(Jdbc.insert("INSERT INTO addresses (customer_id, recipient_name, phone, street, ward, "
                                + "province) VALUES (?, ?, ?, ?, ?, ?)", customerId, a.getRecipientName(),
                        a.getPhone(), a.getStreet(), a.getWard(), a.getProvince()));
            }
            keep.add(a.getId());
        }
        Long defaultId = book.getDefault() == null ? null : book.getDefault().getId();
        int updated = Jdbc.update("UPDATE address_books SET default_address_id = ? WHERE customer_id = ?",
                defaultId, customerId);
        if (updated == 0) {
            Jdbc.update("INSERT INTO address_books (customer_id, default_address_id) VALUES (?, ?)",
                    customerId, defaultId);
        }
        for (Long id : existing) {
            if (!keep.contains(id)) {
                Jdbc.update("DELETE FROM addresses WHERE id = ? AND customer_id = ?", id, customerId);
            }
        }
    }
}
