package com.shop.web;

import com.shop.config.AppConfig;
import com.shop.persistence.DataSeeder;
import com.shop.persistence.Database;
import com.shop.persistence.Tx;
import com.shop.service.PaymentService;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collections;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/** Khởi động/dừng các thành phần nền khi ứng dụng được triển khai/gỡ. */
@WebListener
public class AppContextListener implements ServletContextListener {

    private ScheduledExecutorService scheduler;

    @Override
    public void contextInitialized(ServletContextEvent event) {
        Database.start();
        if (AppConfig.getBoolean("db.seed", true)) {
            DataSeeder.seedIfEmpty();
        }
        event.getServletContext().setAttribute("vnpayTimeoutMinutes", AppConfig.getInt("order.vnpayTimeoutMinutes", 15));
        startPaymentExpiryJob(event);
        event.getServletContext().log("Clothing shop started with database " + AppConfig.get("db.url"));
    }

    /** Mỗi phút huỷ các đơn VNPAY quá hạn thanh toán (nhả hàng đang giữ, trả lượt voucher). */
    private void startPaymentExpiryJob(ServletContextEvent event) {
        PaymentService payments = new PaymentService();
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "vnpay-expiry");
            t.setDaemon(true);
            return t;
        });
        scheduler.scheduleWithFixedDelay(() -> {
            try {
                int cancelled = Tx.withConnection(payments::cancelOverdueVnpayOrders);
                if (cancelled > 0) {
                    event.getServletContext().log("Auto-cancelled " + cancelled + " overdue VNPAY order(s)");
                }
            } catch (RuntimeException e) {
                event.getServletContext().log("VNPAY expiry job failed", e);
            }
        }, 1, 1, TimeUnit.MINUTES);
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
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
