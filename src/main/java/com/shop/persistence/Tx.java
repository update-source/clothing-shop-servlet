package com.shop.persistence;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;
import javax.sql.DataSource;

/**
 * Quản lý kết nối theo luồng: mỗi request dùng một kết nối (mở khi cần, đóng ở cuối request),
 * ngoài transaction thì tự commit. {@link #inTransaction} bọc một đơn vị công việc nghiệp vụ;
 * lời gọi lồng nhau tham gia transaction bên ngoài.
 */
public final class Tx {

    private static volatile DataSource dataSource;
    private static final ThreadLocal<Connection> CURRENT = new ThreadLocal<>();

    private Tx() {
    }

    static void init(DataSource ds) {
        dataSource = ds;
    }

    public static Connection connection() {
        Connection c = CURRENT.get();
        if (c == null) {
            if (dataSource == null) {
                throw new IllegalStateException("Database not started");
            }
            try {
                c = dataSource.getConnection();
                c.setAutoCommit(true);
            } catch (SQLException e) {
                throw new DataAccessException("Cannot open connection", e);
            }
            CURRENT.set(c);
        }
        return c;
    }

    public static <T> T inTransaction(Supplier<T> work) {
        Connection c = connection();
        try {
            if (!c.getAutoCommit()) {
                return work.get();
            }
            c.setAutoCommit(false);
        } catch (SQLException e) {
            throw new DataAccessException("Cannot begin transaction", e);
        }
        try {
            T result = work.get();
            c.commit();
            return result;
        } catch (RuntimeException | Error e) {
            rollbackQuietly(c);
            throw e;
        } catch (SQLException e) {
            rollbackQuietly(c);
            throw new DataAccessException("Commit failed", e);
        } finally {
            try {
                c.setAutoCommit(true);
            } catch (SQLException ignored) {
                // kết nối hỏng sẽ bị pool loại bỏ
            }
        }
    }

    public static void inTransaction(Runnable work) {
        inTransaction(() -> {
            work.run();
            return null;
        });
    }

    /** Chạy công việc ngoài request (luồng nền) rồi trả kết nối. */
    public static <T> T withConnection(Supplier<T> work) {
        try {
            return work.get();
        } finally {
            release();
        }
    }

    /** Trả kết nối của luồng hiện tại về pool; gọi ở cuối mỗi request. */
    public static void release() {
        Connection c = CURRENT.get();
        CURRENT.remove();
        Tracker.clear();
        if (c != null) {
            try {
                if (!c.getAutoCommit()) {
                    c.rollback();
                }
            } catch (SQLException ignored) {
                // bỏ qua
            }
            try {
                c.close();
            } catch (SQLException ignored) {
                // bỏ qua
            }
        }
    }

    private static void rollbackQuietly(Connection c) {
        try {
            c.rollback();
        } catch (SQLException ignored) {
            // giữ lỗi gốc
        }
    }
}
