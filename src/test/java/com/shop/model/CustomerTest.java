package com.shop.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.shop.model.account.Address;
import com.shop.model.account.AddressBook;
import com.shop.model.account.Customer;
import com.shop.model.account.Gender;
import com.shop.model.catalog.Product;
import com.shop.model.catalog.ProductVariant;
import com.shop.model.catalog.Review;
import com.shop.model.order.Order;
import com.shop.model.order.PaymentMethod;
import org.junit.jupiter.api.Test;

/** Kịch bản E (viết đánh giá) và các hành vi của khách hàng. */
class CustomerTest {

    private final Product shirt = Fixtures.product("Áo thun basic", "200000");
    private final ProductVariant blackM = Fixtures.variant(shirt, "M", "Đen", 10);
    private final Customer customer = Fixtures.customer();

    private void buyAndReceive() {
        customer.getCart().addItem(blackM, 1);
        Order order = customer.checkout(Fixtures.address(), PaymentMethod.COD, null, Fixtures.vnd("30000"), null);
        order.pay();
        order.confirm(Fixtures.staff());
        order.ship(Fixtures.staff());
        order.complete();
    }

    @Test
    void scenarioE_onlyBuyersCanReviewOnce() {
        assertThrows(DomainException.class, () -> customer.writeReview(shirt, 5, "Đẹp"));
        buyAndReceive();

        Review review = customer.writeReview(shirt, 5, "Vải mát, đúng size");
        assertEquals(5.0, shirt.getAverageRating());
        assertTrue(shirt.hasReviewBy(customer));
        assertThrows(DomainException.class, () -> customer.writeReview(shirt, 4, "Lần hai"));

        review.edit(4, "Sau vài lần giặt hơi co");
        assertEquals(4.0, shirt.getAverageRating());
        assertThrows(DomainException.class, () -> review.edit(6, "quá 5 sao"));
    }

    @Test
    void reviewRatingOutOfRangeIsRejected() {
        assertThrows(DomainException.class, () -> new Review(customer, 0, "x"));
    }

    @Test
    void wishlistAddIsIdempotent() {
        customer.addToWishlist(shirt);
        customer.addToWishlist(shirt);
        assertEquals(1, customer.getWishlist().size());
        customer.removeFromWishlist(shirt);
        assertFalse(customer.isInWishlist(shirt));
    }

    @Test
    void passwordCanBeChangedOnlyWithOldPassword() {
        Customer c = Customer.register("newbie", "secret1", "Người Mới", Gender.OTHER, null,
                "newbie@mail.vn", "0909999999");
        assertTrue(c.verifyPassword("secret1"));
        assertThrows(DomainException.class, () -> c.changePassword("wrong", "secret2"));
        c.changePassword("secret1", "secret2");
        assertTrue(c.verifyPassword("secret2"));
        c.lock();
        assertFalse(c.isActive());
    }

    @Test
    void addressBookKeepsAtMostOneDefault() {
        AddressBook book = customer.getAddressBook();
        Address home = Fixtures.address();
        Address office = new Address("Nguyễn Văn A", "0901234567", "1 Võ Văn Ngân",
                "Phường Thủ Đức", "TP. Hồ Chí Minh");
        book.add(home);
        book.add(office);
        assertEquals(home, book.getDefault());
        assertThrows(DomainException.class, () -> book.add(Fixtures.address()));

        book.setDefault(office);
        assertTrue(book.isDefault(office));
        book.remove(office);
        assertEquals(home, book.getDefault());
    }
}
