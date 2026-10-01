package com.shop.model.discount;

import com.shop.model.Check;
import com.shop.model.DomainException;
import com.shop.model.Entity;
import com.shop.model.Lazy;
import com.shop.model.catalog.Product;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Chương trình khuyến mãi cửa hàng tự áp lên giá từng sản phẩm. Quan hệ với sản phẩm là
 * nhiều – nhiều và hai chiều: chương trình biết mình áp cho sản phẩm nào, sản phẩm biết
 * mình nằm trong chương trình nào.
 */
public class Promotion extends Entity {

    private String name;
    private final DiscountPolicy policy;
    private final Period period;
    private final Lazy<List<Product>> products;

    public Promotion(Long id, String name, DiscountPolicy policy, Period period,
                     Lazy<List<Product>> products) {
        super(id);
        this.name = name;
        this.policy = policy;
        this.period = period;
        this.products = products;
    }

    public static Promotion create(String name, DiscountPolicy policy, Period period) {
        if (policy == null || period == null) {
            throw new DomainException("Chương trình cần cách tính giảm và thời gian diễn ra");
        }
        return new Promotion(null, Check.text(name, "Tên chương trình", 150), policy, period,
                Lazy.of(new ArrayList<>()));
    }

    public boolean isOngoing(LocalDateTime now) {
        return period.contains(now);
    }

    /** Giá sau giảm cho một mức giá. */
    public BigDecimal apply(BigDecimal price) {
        return price.subtract(policy.calculate(price)).max(BigDecimal.ZERO);
    }

    public void addProduct(Product product) {
        if (!products.get().contains(product)) {
            products.get().add(product);
        }
        product.joinPromotion(this);
    }

    public void removeProduct(Product product) {
        products.get().remove(product);
        product.leavePromotion(this);
    }

    public boolean includes(Product product) {
        return products.get().contains(product);
    }

    public void rename(String newName) {
        this.name = Check.text(newName, "Tên chương trình", 150);
    }

    public String getName() {
        return name;
    }

    public DiscountPolicy getPolicy() {
        return policy;
    }

    public Period getPeriod() {
        return period;
    }

    public List<Product> getProducts() {
        return Collections.unmodifiableList(products.get());
    }
}
