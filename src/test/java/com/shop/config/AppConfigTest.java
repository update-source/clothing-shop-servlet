package com.shop.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class AppConfigTest {

    @AfterEach
    void clearSystemProperties() {
        System.clearProperty("shop.test.from.env");
        System.clearProperty("shop.vnpay.mode");
    }

    @Test
    void envNameIsUpperCaseWithUnderscores() {
        assertEquals("SHOP_DB_URL", AppConfig.envName("db.url"));
        assertEquals("SHOP_DB_POOLSIZE", AppConfig.envName("db.poolSize"));
        assertEquals("SHOP_VNPAY_HASHSECRET", AppConfig.envName("vnpay.hashSecret"));
    }

    @Test
    void environmentVariableIsRead() {
        // SHOP_TEST_FROM_ENV được đặt trong cấu hình surefire (pom.xml)
        assertEquals("env-value", AppConfig.get("test.from.env"));
    }

    @Test
    void systemPropertyWinsOverEnvironmentAndFile() {
        System.setProperty("shop.test.from.env", "sysprop-value");
        System.setProperty("shop.vnpay.mode", "sandbox");
        assertEquals("sysprop-value", AppConfig.get("test.from.env"));
        assertEquals("sandbox", AppConfig.get("vnpay.mode"));
    }

    @Test
    void fileValueAndDefaultsApplyWhenNotOverridden() {
        assertEquals("mock", AppConfig.get("vnpay.mode"));
        assertNull(AppConfig.get("no.such.key"));
        assertEquals("fallback", AppConfig.get("no.such.key", "fallback"));
        assertEquals(15, AppConfig.getInt("order.vnpayTimeoutMinutes", 0));
    }
}
