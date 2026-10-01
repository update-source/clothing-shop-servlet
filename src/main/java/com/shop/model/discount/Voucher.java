package com.shop.model.discount;

import com.shop.model.DomainException;
import com.shop.model.Entity;
import com.shop.model.account.Customer;
import com.shop.model.account.CustomerLevel;
import com.shop.util.Money;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Mã giảm giá khách tự nhập để giảm trên cả đơn hàng. Voucher chứa đúng một cách tính giảm
 * và một thời gian hiệu lực, tự đếm lượt dùng.
 */
public class Voucher extends Entity {

    private static final Pattern CODE = Pattern.compile("^[A-Z0-9_-]{3,30}$");

    private final String code;
    private final Set<CustomerLevel> applicableTiers;
    private final BigDecimal minOrderValue;
    private final int usageLimit;
    private final int perCustomerLimit;
    private int usedCount;
    private boolean active;
    private final DiscountPolicy policy;
    private final Period period;

    public Voucher(Long id, String code, Set<CustomerLevel> applicableTiers, BigDecimal minOrderValue,
                   int usageLimit, int perCustomerLimit, int usedCount, boolean active,
                   DiscountPolicy policy, Period period) {
        super(id);
        this.code = code;
        this.applicableTiers = applicableTiers.isEmpty()
                ? EnumSet.noneOf(CustomerLevel.class) : EnumSet.copyOf(applicableTiers);
        this.minOrderValue = minOrderValue;
        this.usageLimit = usageLimit;
        this.perCustomerLimit = perCustomerLimit;
        this.usedCount = usedCount;
        this.active = active;
        this.policy = policy;
        this.period = period;
    }

    public static Voucher create(String code, Set<CustomerLevel> tiers, BigDecimal minOrderValue,
                                 int usageLimit, int perCustomerLimit,
                                 DiscountPolicy policy, Period period) {
        String normalized = code == null ? "" : code.trim().toUpperCase();
        if (!CODE.matcher(normalized).matches()) {
            throw new DomainException("Mã voucher gồm 3–30 ký tự chữ in hoa, số, '-' hoặc '_'");
        }
        if (tiers == null || tiers.isEmpty()) {
            throw new DomainException("Chọn ít nhất một hạng khách được dùng voucher");
        }
        if (minOrderValue == null || minOrderValue.signum() < 0) {
            throw new DomainException("Giá trị đơn tối thiểu không được âm");
        }
        if (usageLimit < 1 || perCustomerLimit < 1) {
            throw new DomainException("Số lượt dùng phải lớn hơn 0");
        }
        if (policy == null || period == null) {
            throw new DomainException("Voucher cần cách tính giảm và thời gian hiệu lực");
        }
        return new Voucher(null, normalized, tiers, minOrderValue, usageLimit, perCustomerLimit,
                0, true, policy, period);
    }

    /** Còn hiệu lực: đang kích hoạt, trong thời gian hiệu lực, còn lượt. */
    public boolean isValid(LocalDateTime now) {
        return active && period.contains(now) && usedCount < usageLimit;
    }

    /** Áp được cho khách và số tiền: đúng hạng, đủ giá trị tối thiểu, khách chưa dùng quá lượt. */
    public boolean isApplicable(Customer customer, BigDecimal amount) {
        return applicableTiers.contains(customer.getLevel())
                && amount.compareTo(minOrderValue) >= 0
                && customer.countVoucherUsage(this) < perCustomerLimit;
    }

    /** Lý do không dùng được voucher (null nếu dùng được) — để báo cho khách biết vì sao. */
    public String whyNotUsable(Customer customer, BigDecimal amount, LocalDateTime now) {
        if (!active) return "Mã " + code + " đã ngừng áp dụng";
        if (period.isUpcoming(now)) return "Mã " + code + " chưa đến thời gian áp dụng";
        if (period.isOver(now)) return "Mã " + code + " đã hết hạn";
        if (usedCount >= usageLimit) return "Mã " + code + " đã hết lượt sử dụng";
        if (!applicableTiers.contains(customer.getLevel())) {
            return "Mã " + code + " chỉ dành cho hạng " + getTierLabels();
        }
        if (amount.compareTo(minOrderValue) < 0) {
            return "Mã " + code + " áp dụng cho đơn từ " + Money.format(minOrderValue);
        }
        if (customer.countVoucherUsage(this) >= perCustomerLimit) {
            return "Bạn đã dùng hết lượt của mã " + code;
        }
        return null;
    }

    public BigDecimal calculateDiscount(BigDecimal amount) {
        return policy.calculate(amount);
    }

    /** Ghi nhận một lượt dùng khi khách đặt hàng. */
    public void redeem() {
        if (usedCount >= usageLimit) {
            throw new DomainException("Mã " + code + " đã hết lượt sử dụng");
        }
        usedCount++;
    }

    /** Trả lại lượt khi đơn bị huỷ hoặc giao thất bại. */
    public void release() {
        if (usedCount > 0) {
            usedCount--;
        }
    }

    public void activate() {
        active = true;
    }

    public void deactivate() {
        active = false;
    }

    public String getTierLabels() {
        return applicableTiers.stream().map(CustomerLevel::getLabel).collect(Collectors.joining(", "));
    }

    public String getCode() {
        return code;
    }

    public Set<CustomerLevel> getApplicableTiers() {
        return Collections.unmodifiableSet(applicableTiers);
    }

    public BigDecimal getMinOrderValue() {
        return minOrderValue;
    }

    public int getUsageLimit() {
        return usageLimit;
    }

    public int getPerCustomerLimit() {
        return perCustomerLimit;
    }

    public int getUsedCount() {
        return usedCount;
    }

    public boolean isActive() {
        return active;
    }

    public DiscountPolicy getPolicy() {
        return policy;
    }

    public Period getPeriod() {
        return period;
    }
}
