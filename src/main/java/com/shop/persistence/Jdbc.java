package com.shop.persistence;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/** Tiện ích JDBC mỏng: truy vấn với tham số, ánh xạ dòng, lấy khoá sinh tự động. */
public final class Jdbc {

    @FunctionalInterface
    public interface RowMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

    private Jdbc() {
    }

    public static <T> List<T> query(String sql, RowMapper<T> mapper, Object... params) {
        try (PreparedStatement ps = Tx.connection().prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                List<T> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(mapper.map(rs));
                }
                return result;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Query failed: " + sql, e);
        }
    }

    public static <T> Optional<T> one(String sql, RowMapper<T> mapper, Object... params) {
        List<T> rows = query(sql, mapper, params);
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    public static long count(String sql, Object... params) {
        return one(sql, rs -> rs.getLong(1), params).orElse(0L);
    }

    public static int update(String sql, Object... params) {
        try (PreparedStatement ps = Tx.connection().prepareStatement(sql)) {
            bind(ps, params);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Update failed: " + sql, e);
        }
    }

    /** Chèn một dòng và trả về khoá chính sinh tự động. */
    public static long insert(String sql, Object... params) {
        try (PreparedStatement ps = Tx.connection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, params);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new DataAccessException("No generated key: " + sql, null);
                }
                return keys.getLong(1);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Insert failed: " + sql, e);
        }
    }

    /** Chuỗi "?,?,?" cho mệnh đề IN. */
    public static String in(Collection<?> values) {
        return String.join(",", Collections.nCopies(Math.max(1, values.size()), "?"));
    }

    public static Long getLong(ResultSet rs, String column) throws SQLException {
        long v = rs.getLong(column);
        return rs.wasNull() ? null : v;
    }

    public static LocalDateTime getDateTime(ResultSet rs, String column) throws SQLException {
        Timestamp ts = rs.getTimestamp(column);
        return ts == null ? null : ts.toLocalDateTime();
    }

    public static LocalDate getDate(ResultSet rs, String column) throws SQLException {
        Date d = rs.getDate(column);
        return d == null ? null : d.toLocalDate();
    }

    private static void bind(PreparedStatement ps, Object[] params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            Object p = params[i];
            int idx = i + 1;
            if (p == null) {
                ps.setObject(idx, null);
            } else if (p instanceof LocalDateTime ldt) {
                ps.setTimestamp(idx, Timestamp.valueOf(ldt));
            } else if (p instanceof LocalDate ld) {
                ps.setDate(idx, Date.valueOf(ld));
            } else if (p instanceof Enum<?> e) {
                ps.setString(idx, e.name());
            } else if (p instanceof Boolean b) {
                ps.setBoolean(idx, b);
            } else if (p instanceof BigDecimal bd) {
                ps.setBigDecimal(idx, bd);
            } else {
                ps.setObject(idx, p);
            }
        }
    }
}
