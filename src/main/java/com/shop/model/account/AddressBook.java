package com.shop.model.account;

import com.shop.model.DomainException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Sổ địa chỉ của một khách hàng. Quy tắc "tối đa một địa chỉ mặc định" nằm gọn trong sổ,
 * và địa chỉ mặc định là một liên kết riêng chứ không phải cờ trên từng địa chỉ.
 */
public class AddressBook {

    public static final int MAX_ADDRESSES = 10;

    private final List<Address> addresses;
    private Address defaultAddress;

    public AddressBook() {
        this(new ArrayList<>(), null);
    }

    public AddressBook(List<Address> addresses, Address defaultAddress) {
        this.addresses = new ArrayList<>(addresses);
        this.defaultAddress = defaultAddress;
    }

    public void add(Address address) {
        if (addresses.contains(address)) {
            throw new DomainException("Địa chỉ này đã có trong sổ");
        }
        if (addresses.size() >= MAX_ADDRESSES) {
            throw new DomainException("Sổ địa chỉ chỉ lưu tối đa " + MAX_ADDRESSES + " địa chỉ");
        }
        addresses.add(address);
        if (defaultAddress == null) {
            defaultAddress = address;
        }
    }

    public void remove(Address address) {
        if (!addresses.remove(address)) {
            throw new DomainException("Địa chỉ không có trong sổ");
        }
        if (address.equals(defaultAddress)) {
            defaultAddress = addresses.isEmpty() ? null : addresses.get(0);
        }
    }

    public void setDefault(Address address) {
        int index = addresses.indexOf(address);
        if (index < 0) {
            throw new DomainException("Địa chỉ không có trong sổ");
        }
        defaultAddress = addresses.get(index);
    }

    public Address getDefault() {
        return defaultAddress;
    }

    public boolean isDefault(Address address) {
        return address != null && address.equals(defaultAddress);
    }

    public List<Address> getAddresses() {
        return Collections.unmodifiableList(addresses);
    }

    public Optional<Address> findById(long id) {
        return addresses.stream().filter(a -> a.getId() != null && a.getId() == id).findFirst();
    }

    public boolean isEmpty() {
        return addresses.isEmpty();
    }
}
