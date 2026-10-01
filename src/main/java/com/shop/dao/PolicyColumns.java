package com.shop.dao;

import com.shop.model.discount.DiscountPolicy;
import com.shop.model.discount.FixedAmountDiscount;
import com.shop.model.discount.PercentDiscount;
import com.shop.model.discount.Period;
import com.shop.persistence.Jdbc;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Ánh xạ DiscountPolicy và Period (composition 1–1) sang các cột policy_* và period_*. */
final class PolicyColumns {

    static final String PERCENT = "PERCENT";
    static final String FIXED = "FIXED";

    private PolicyColumns() {
    }

    static DiscountPolicy readPolicy(ResultSet rs) throws SQLException {
        String type = rs.getString("policy_type");
        BigDecimal value = rs.getBigDecimal("policy_value");
        BigDecimal max = rs.getBigDecimal("policy_max_discount");
        return PERCENT.equals(type) ? new PercentDiscount(value, max) : new FixedAmountDiscount(value);
    }

    static Period readPeriod(ResultSet rs) throws SQLException {
        return new Period(Jdbc.getDateTime(rs, "period_start"), Jdbc.getDateTime(rs, "period_end"));
    }

    static String type(DiscountPolicy policy) {
        return policy instanceof PercentDiscount ? PERCENT : FIXED;
    }

    static BigDecimal value(DiscountPolicy policy) {
        if (policy instanceof PercentDiscount p) {
            return p.getPercent();
        }
        return ((FixedAmountDiscount) policy).getAmount();
    }

    static BigDecimal maxDiscount(DiscountPolicy policy) {
        return policy instanceof PercentDiscount p ? p.getMaxDiscount() : null;
    }
}
