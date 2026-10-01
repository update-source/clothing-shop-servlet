package com.shop.service;

import com.shop.dao.Daos;
import com.shop.model.DomainException;
import com.shop.model.account.Address;
import com.shop.model.account.AddressBook;
import com.shop.model.account.Customer;
import com.shop.persistence.Tx;
import java.util.function.Consumer;

/** Sổ địa chỉ của khách: mọi thay đổi đi qua AddressBook rồi mới lưu. */
public class AddressService {

    /** Dữ liệu nhập của một địa chỉ. */
    public record AddressForm(String recipientName, String phone, String street, String ward, String province) {

        public Address toAddress() {
            if (!Provinces.isValid(province)) {
                throw new DomainException("Vui lòng chọn tỉnh/thành phố trong danh sách");
            }
            return new Address(recipientName, phone, street, ward, province);
        }
    }

    public Address add(long customerId, AddressForm form, boolean makeDefault) {
        Address address = form.toAddress();
        change(customerId, book -> {
            book.add(address);
            if (makeDefault) {
                book.setDefault(address);
            }
        });
        return address;
    }

    /** Địa chỉ là value object bất biến: "sửa" nghĩa là thay địa chỉ cũ bằng một địa chỉ mới. */
    public void replace(long customerId, long addressId, AddressForm form) {
        Address replacement = form.toAddress();
        change(customerId, book -> {
            Address old = find(book, addressId);
            boolean wasDefault = book.isDefault(old);
            book.remove(old);
            book.add(replacement);
            if (wasDefault) {
                book.setDefault(replacement);
            }
        });
    }

    public void remove(long customerId, long addressId) {
        change(customerId, book -> book.remove(find(book, addressId)));
    }

    public void setDefault(long customerId, long addressId) {
        change(customerId, book -> book.setDefault(find(book, addressId)));
    }

    private void change(long customerId, Consumer<AddressBook> action) {
        Tx.inTransaction(() -> {
            Customer customer = Daos.users().findCustomer(customerId)
                    .orElseThrow(() -> new DomainException("Không tìm thấy khách hàng"));
            AddressBook book = customer.getAddressBook();
            action.accept(book);
            Daos.addresses().saveBook(customerId, book);
        });
    }

    private static Address find(AddressBook book, long addressId) {
        return book.findById(addressId).orElseThrow(() -> new DomainException("Địa chỉ không có trong sổ"));
    }
}
