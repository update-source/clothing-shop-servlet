package com.shop.service;

import com.shop.dao.Daos;
import com.shop.model.DomainException;
import com.shop.model.account.CustomerLevel;
import com.shop.model.catalog.Product;
import com.shop.model.discount.DiscountPolicy;
import com.shop.model.discount.FixedAmountDiscount;
import com.shop.model.discount.PercentDiscount;
import com.shop.model.discount.Period;
import com.shop.model.discount.Promotion;
import com.shop.model.discount.Voucher;
import com.shop.persistence.Tx;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/** Quản trị giảm giá (quyền ADMIN): chương trình khuyến mãi và voucher. */
public class AdminDiscountService {

    /** Cách tính giảm do quản trị viên nhập: PERCENT (value = %, max = trần) hoặc FIXED (value = số tiền). */
    public record PolicyForm(String type, BigDecimal value, BigDecimal maxDiscount) {

        public DiscountPolicy toPolicy() {
            if (value == null) {
                throw new DomainException("Vui lòng nhập mức giảm");
            }
            return "PERCENT".equals(type) ? new PercentDiscount(value, maxDiscount) : new FixedAmountDiscount(value);
        }
    }

    // ------------------------------------------------------------------ khuyến mãi

    public List<Promotion> promotions() {
        return Daos.promotions().findAll();
    }

    public Promotion promotion(long id) {
        return Daos.promotions().findById(id).orElseThrow(() -> new DomainException("Chương trình không tồn tại"));
    }

    public Promotion createPromotion(String name, PolicyForm policy, LocalDateTime start, LocalDateTime end) {
        return Tx.inTransaction(() -> {
            Promotion promotion = Promotion.create(name, policy.toPolicy(), new Period(start, end));
            Daos.promotions().insert(promotion);
            return promotion;
        });
    }

    public void renamePromotion(long id, String name) {
        changePromotion(id, p -> p.rename(name));
    }

    public void addProduct(long promotionId, long productId) {
        changePromotion(promotionId, p -> p.addProduct(product(productId)));
    }

    public void removeProduct(long promotionId, long productId) {
        changePromotion(promotionId, p -> p.removeProduct(product(productId)));
    }

    /** Xoá chương trình: an toàn vì đơn hàng đã chốt đơn giá, không tham chiếu khuyến mãi. */
    public void deletePromotion(long id) {
        Tx.inTransaction(() -> {
            promotion(id);
            Daos.promotions().delete(id);
        });
    }

    private void changePromotion(long id, Consumer<Promotion> action) {
        Tx.inTransaction(() -> {
            Promotion promotion = promotion(id);
            action.accept(promotion);
            Daos.promotions().update(promotion);
        });
    }

    private static Product product(long id) {
        return Daos.products().findById(id).orElseThrow(() -> new DomainException("Sản phẩm không tồn tại"));
    }

    // ------------------------------------------------------------------ voucher

    public List<Voucher> vouchers() {
        return Daos.vouchers().findAll();
    }

    public Voucher createVoucher(String code, Set<CustomerLevel> tiers, BigDecimal minOrderValue, int usageLimit,
                                 int perCustomerLimit, PolicyForm policy, LocalDateTime start, LocalDateTime end) {
        return Tx.inTransaction(() -> {
            Voucher voucher = Voucher.create(code, tiers, minOrderValue == null ? BigDecimal.ZERO : minOrderValue,
                    usageLimit, perCustomerLimit, policy.toPolicy(), new Period(start, end));
            if (Daos.vouchers().existsCode(voucher.getCode())) {
                throw new DomainException("Mã " + voucher.getCode() + " đã tồn tại");
            }
            Daos.vouchers().insert(voucher);
            return voucher;
        });
    }

    /** Bật/tắt voucher; trả về trạng thái mới. */
    public boolean toggleVoucher(long id) {
        return Tx.inTransaction(() -> {
            Voucher voucher = Daos.vouchers().findById(id).orElseThrow(() -> new DomainException("Voucher không tồn tại"));
            if (voucher.isActive()) {
                voucher.deactivate();
            } else {
                voucher.activate();
            }
            Daos.vouchers().update(voucher);
            return voucher.isActive();
        });
    }
}
