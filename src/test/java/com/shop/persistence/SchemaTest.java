package com.shop.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class SchemaTest {

    @BeforeAll
    static void start() {
        TestDatabase.start();
    }

    @AfterAll
    static void stop() {
        TestDatabase.stop();
    }

    @Test
    void schemaCreatesAllTables() {
        long tables = Tx.withConnection(() -> Jdbc.count(
                "SELECT COUNT(*) FROM information_schema.tables WHERE LOWER(table_schema) = 'public' AND LOWER(table_name) IN "
                        + "('users','addresses','address_books','categories','products','product_variants',"
                        + "'cart_items','wishlists','reviews','vouchers','promotions','promotion_products',"
                        + "'orders','order_details','payments')"));
        assertEquals(15, tables);
    }
}
