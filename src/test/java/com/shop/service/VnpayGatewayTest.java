package com.shop.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class VnpayGatewayTest {

    private final VnpayGateway gateway = new VnpayGateway();

    private static Map<String, String> queryOf(String url) {
        Map<String, String> params = new HashMap<>();
        for (String pair : URI.create(url).getRawQuery().split("&")) {
            String[] kv = pair.split("=", 2);
            params.put(URLDecoder.decode(kv[0], StandardCharsets.UTF_8), URLDecoder.decode(kv[1], StandardCharsets.UTF_8));
        }
        return params;
    }

    @Test
    void paymentUrlIsSignedAndVerifiable() {
        String url = gateway.paymentUrl(42, new BigDecimal("513200.00"), "Thanh toan don hang 7",
                "127.0.0.1", "http://localhost:8080");
        Map<String, String> params = queryOf(url);
        assertEquals("51320000", params.get("vnp_Amount"));
        assertEquals("42", params.get("vnp_TxnRef"));
        assertEquals("http://localhost:8080/payment/vnpay-return", params.get("vnp_ReturnUrl"));
        assertTrue(gateway.verify(params));
    }

    @Test
    void tamperedAmountFailsVerification() {
        Map<String, String> params = queryOf(gateway.paymentUrl(42, new BigDecimal("100000"), "x y",
                "127.0.0.1", "http://localhost:8080"));
        params.put("vnp_Amount", "100");
        assertFalse(gateway.verify(params));
        params.remove("vnp_SecureHash");
        assertFalse(gateway.verify(params));
    }

    @Test
    void parseReadsResultAndAmount() {
        VnpayGateway.Result r = gateway.parse(Map.of("vnp_TxnRef", "9", "vnp_Amount", "51320000",
                "vnp_ResponseCode", "00", "vnp_TransactionStatus", "00", "vnp_TransactionNo", "14000000"));
        assertTrue(r.isSuccess());
        assertEquals(9, r.txnRef());
        assertEquals(0, r.amount().compareTo(new BigDecimal("513200")));
    }
}
