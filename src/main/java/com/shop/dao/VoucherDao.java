package com.shop.dao;

import com.shop.model.DomainException;
import com.shop.model.account.CustomerLevel;
import com.shop.model.discount.Voucher;
import com.shop.persistence.Jdbc;
import com.shop.persistence.Tracker;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class VoucherDao {

    private static final String SELECT = "SELECT * FROM vouchers";

    private static Voucher map(ResultSet rs) throws SQLException {
        Set<CustomerLevel> tiers = EnumSet.noneOf(CustomerLevel.class);
        Arrays.stream(rs.getString("applicable_tiers").split(","))
                .map(String::trim).filter(s -> !s.isEmpty()).map(CustomerLevel::valueOf).forEach(tiers::add);
        Voucher v = new Voucher(rs.getLong("id"), rs.getString("code"), tiers, rs.getBigDecimal("min_order_value"),
                rs.getInt("usage_limit"), rs.getInt("per_customer_limit"), rs.getInt("used_count"),
                rs.getBoolean("active"), PolicyColumns.readPolicy(rs), PolicyColumns.readPeriod(rs));
        Tracker.remember(v, v.getUsedCount());
        return v;
    }

    public Optional<Voucher> findById(long id) {
        return Jdbc.one(SELECT + " WHERE id = ?", VoucherDao::map, id);
    }

    public Optional<Voucher> findByCode(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        return Jdbc.one(SELECT + " WHERE code = ?", VoucherDao::map, code.trim().toUpperCase());
    }

    public Map<Long, Voucher> findByIds(Collection<Long> ids) {
        Map<Long, Voucher> result = new LinkedHashMap<>();
        if (!ids.isEmpty()) {
            List<Object> params = new ArrayList<>(new LinkedHashSet<>(ids));
            Jdbc.query(SELECT + " WHERE id IN (" + Jdbc.in(params) + ")", VoucherDao::map, params.toArray())
                    .forEach(v -> result.put(v.getId(), v));
        }
        return result;
    }

    public List<Voucher> findAll() {
        return Jdbc.query(SELECT + " ORDER BY id DESC", VoucherDao::map);
    }

    public boolean existsCode(String code) {
        return Jdbc.count("SELECT COUNT(*) FROM vouchers WHERE code = ?", code.trim().toUpperCase()) > 0;
    }

    public void insert(Voucher v) {
        String tiers = v.getApplicableTiers().stream().map(Enum::name).collect(Collectors.joining(","));
        v.assignId(Jdbc.insert("INSERT INTO vouchers (code, applicable_tiers, min_order_value, usage_limit, "
                        + "per_customer_limit, used_count, active, policy_type, policy_value, policy_max_discount, "
                        + "period_start, period_end) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                v.getCode(), tiers, v.getMinOrderValue(), v.getUsageLimit(), v.getPerCustomerLimit(),
                v.getUsedCount(), v.isActive(), PolicyColumns.type(v.getPolicy()),
                PolicyColumns.value(v.getPolicy()), PolicyColumns.maxDiscount(v.getPolicy()),
                v.getPeriod().getStart(), v.getPeriod().getEnd()));
        Tracker.remember(v, v.getUsedCount());
    }

    /** Lưu trạng thái kích hoạt và số lượt đã dùng (ghi bằng chênh lệch, không vượt tổng lượt). */
    public void update(Voucher v) {
        Integer original = Tracker.original(v);
        int delta = original == null ? 0 : v.getUsedCount() - original;
        int updated = Jdbc.update("UPDATE vouchers SET used_count = used_count + ?, active = ? WHERE id = ? "
                        + "AND used_count + ? >= 0 AND used_count + ? <= usage_limit",
                delta, v.isActive(), v.getId(), delta, delta);
        if (updated == 0) {
            throw new DomainException("Mã " + v.getCode() + " vừa hết lượt sử dụng");
        }
        Tracker.remember(v, v.getUsedCount());
    }
}
