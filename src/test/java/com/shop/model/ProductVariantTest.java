package com.shop.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.shop.model.catalog.Product;
import com.shop.model.catalog.ProductVariant;
import org.junit.jupiter.api.Test;

class ProductVariantTest {

    private final Product shirt = Fixtures.product("Áo thun basic", "199000");

    @Test
    void reserveKeepsStockButReducesAvailable() {
        ProductVariant v = Fixtures.variant(shirt, "M", "Đen", 5);
        v.reserve(2);
        assertEquals(5, v.getStockQuantity());
        assertEquals(2, v.getReservedQuantity());
        assertEquals(3, v.getAvailableQuantity());
    }

    @Test
    void scenarioB_twoCustomersCannotBuyTheLastShirt() {
        ProductVariant whiteL = Fixtures.variant(shirt, "L", "Trắng", 1);
        whiteL.reserve(1);                                   // khách thứ nhất giữ chiếc cuối
        assertEquals(0, whiteL.getAvailableQuantity());
        assertThrows(DomainException.class, () -> whiteL.reserve(1)); // khách thứ hai bị từ chối
        whiteL.releaseReservation(1);                        // khách thứ nhất huỷ trước khi xác nhận
        assertTrue(whiteL.isAvailable(1));
    }

    @Test
    void commitRemovesReservedGoodsFromStock() {
        ProductVariant v = Fixtures.variant(shirt, "S", "Đen", 12);
        v.reserve(2);
        v.commit(2);
        assertEquals(10, v.getStockQuantity());
        assertEquals(0, v.getReservedQuantity());
    }

    @Test
    void restockAddsToStock() {
        ProductVariant v = Fixtures.variant(shirt, "M", "Đen", 0);
        assertFalse(v.isAvailable(1));
        v.restock(4);
        assertEquals(4, v.getAvailableQuantity());
    }

    @Test
    void cannotReleaseOrCommitMoreThanReserved() {
        ProductVariant v = Fixtures.variant(shirt, "M", "Trắng", 3);
        v.reserve(1);
        assertThrows(IllegalStateException.class, () -> v.releaseReservation(2));
        assertThrows(IllegalStateException.class, () -> v.commit(2));
        assertThrows(DomainException.class, () -> v.reserve(0));
    }

    @Test
    void productRejectsDuplicateVariantAndSumsStock() {
        Fixtures.variant(shirt, "S", "Đen", 12);
        Fixtures.variant(shirt, "L", "Đen", 7);
        assertThrows(DomainException.class, () -> shirt.addVariant("s", "đen", 1));
        assertEquals(19, shirt.getTotalStock());
    }
}
