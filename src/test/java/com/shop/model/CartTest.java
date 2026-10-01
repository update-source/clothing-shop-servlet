package com.shop.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.shop.model.catalog.Cart;
import com.shop.model.catalog.Product;
import com.shop.model.catalog.ProductVariant;
import com.shop.model.discount.FixedAmountDiscount;
import com.shop.model.discount.Promotion;
import org.junit.jupiter.api.Test;

class CartTest {

    private final Product shirt = Fixtures.product("Áo thun basic", "200000");
    private final ProductVariant blackM = Fixtures.variant(shirt, "M", "Đen", 5);
    private final Cart cart = new Cart();

    @Test
    void addingSameVariantMergesQuantity() {
        cart.addItem(blackM, 1);
        cart.addItem(blackM, 2);
        assertEquals(1, cart.getItems().size());
        assertEquals(3, cart.getItemCount());
    }

    @Test
    void cannotAddMoreThanAvailable() {
        cart.addItem(blackM, 4);
        assertThrows(DomainException.class, () -> cart.addItem(blackM, 2));
        assertEquals(4, cart.getItemCount());
    }

    @Test
    void setQuantityZeroRemovesLine() {
        cart.addItem(blackM, 2);
        cart.setQuantity(blackM, 0);
        assertTrue(cart.isEmpty());
    }

    @Test
    void discontinuedProductCannotBeAdded() {
        shirt.discontinue();
        assertThrows(DomainException.class, () -> cart.addItem(blackM, 1));
    }

    @Test
    void totalUsesCurrentPromotionPrice() {
        cart.addItem(blackM, 2);
        assertEquals(0, cart.getTotal().compareTo(Fixtures.vnd("400000")));
        Promotion sale = Promotion.create("Sale 11.11", new FixedAmountDiscount(Fixtures.vnd("50000")), Fixtures.ongoing());
        sale.addProduct(shirt);
        assertEquals(0, cart.getTotal().compareTo(Fixtures.vnd("300000")));
    }
}
