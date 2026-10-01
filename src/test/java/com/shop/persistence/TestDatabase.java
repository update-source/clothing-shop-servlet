package com.shop.persistence;

/** CSDL H2 trong bộ nhớ cho test tích hợp. */
public final class TestDatabase {

    private TestDatabase() {
    }

    public static void start() {
        System.setProperty("shop.db.url",
                "jdbc:h2:mem:shoptest;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;DB_CLOSE_DELAY=-1");
        Database.start();
    }

    public static void stop() {
        Tx.release();
        Database.stop();
    }
}
