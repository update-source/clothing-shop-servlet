package com.shop.persistence;

import com.shop.config.AppConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/** Khởi động pool kết nối và tạo bảng (CREATE TABLE IF NOT EXISTS) từ {@code db/schema.sql}. */
public final class Database {

    private static HikariDataSource dataSource;

    private Database() {
    }

    public static synchronized void start() {
        if (dataSource != null) {
            return;
        }
        HikariConfig cfg = new HikariConfig();
        cfg.setPoolName("shop-pool");
        cfg.setDriverClassName(AppConfig.get("db.driver"));
        cfg.setJdbcUrl(AppConfig.get("db.url"));
        cfg.setUsername(AppConfig.get("db.username", ""));
        cfg.setPassword(AppConfig.get("db.password", ""));
        cfg.setMaximumPoolSize(AppConfig.getInt("db.poolSize", 10));
        dataSource = new HikariDataSource(cfg);
        Tx.init(dataSource);
        createSchema();
    }

    public static synchronized void stop() {
        if (dataSource != null) {
            dataSource.close();
            dataSource = null;
        }
    }

    private static void createSchema() {
        String script;
        try (InputStream in = Database.class.getResourceAsStream("/db/schema.sql")) {
            if (in == null) {
                throw new IllegalStateException("db/schema.sql not found");
            }
            script = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read schema", e);
        }
        try (Connection c = dataSource.getConnection(); Statement st = c.createStatement()) {
            for (String stmt : stripComments(script).split(";")) {
                if (!stmt.isBlank()) {
                    st.execute(stmt.trim());
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Cannot create schema", e);
        }
    }

    private static String stripComments(String sql) {
        StringBuilder sb = new StringBuilder();
        for (String line : sql.split("\n")) {
            if (!line.trim().startsWith("--")) {
                sb.append(line).append('\n');
            }
        }
        return sb.toString().trim();
    }
}
