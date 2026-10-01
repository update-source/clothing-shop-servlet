package com.shop.model.discount;

import com.shop.model.DomainException;
import com.shop.util.Money;
import java.math.BigDecimal;

/** Giảm một số tiền cố định. */
public final class FixedAmountDiscount implements DiscountPolicy {

    private final BigDecimal amount;

    public FixedAmountDiscount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new DomainException("Số tiền giảm phải lớn hơn 0");
        }
        this.amount = amount;
    }

    @Override
    public BigDecimal calculate(BigDecimal price) {
        if (price == null || price.signum() <= 0) {
            return BigDecimal.ZERO;
        }
        return amount.min(price);
    }

    @Override
    public String describe() {
        return "Giảm " + Money.format(amount);
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
