package com.shop.web;

import com.shop.config.AppConfig;
import com.shop.persistence.DataSeeder;
import com.shop.persistence.Database;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collections;

/** Khởi động/dừng các thành phần nền khi ứng dụng được triển khai/gỡ. */
@WebListener
public class AppContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent event) {
        Database.start();
        if (AppConfig.getBoolean("db.seed", true)) {
            DataSeeder.seedIfEmpty();
        }
        event.getServletContext().log("Clothing shop started with database " + AppConfig.get("db.url"));
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        Database.stop();
        // Gỡ JDBC driver do webapp nạp để tránh rò rỉ bộ nhớ khi redeploy trên Tomcat.
        ClassLoader webappLoader = Thread.currentThread().getContextClassLoader();
        for (Driver driver : Collections.list(DriverManager.getDrivers())) {
            if (driver.getClass().getClassLoader() == webappLoader) {
                try {
                    DriverManager.deregisterDriver(driver);
                } catch (SQLException ignored) {
                    // bỏ qua
                }
            }
        }
    }
}
