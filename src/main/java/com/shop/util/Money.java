package com.shop.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

/** Định dạng tiền Việt Nam: 199000 -> "199.000 ₫". */
public final class Money {

    private Money() {
    }

    public static String format(BigDecimal amount) {
        if (amount == null) {
            return "";
        }
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('.');
        DecimalFormat df = new DecimalFormat("#,##0", symbols);
        return df.format(amount.setScale(0, RoundingMode.HALF_UP)) + " ₫";
    }
}
