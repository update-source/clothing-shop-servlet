package com.shop.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Đọc cấu hình từ {@code app.properties}, ghi đè bởi {@code app-local.properties},
 * biến môi trường {@code SHOP_<KEY>} (dùng khi chạy trong Docker) và system property {@code shop.<key>}.
 */
public final class AppConfig {

    private static final Pattern PLACEHOLDER = Pattern.compile("\\$\\{([^}]+)}");
    private static final Properties PROPS = load();

    private AppConfig() {
    }

    private static Properties load() {
        Properties props = new Properties();
        loadInto(props, "/app.properties");
        loadInto(props, "/app-local.properties");
        return props;
    }

    private static void loadInto(Properties props, String resource) {
        try (InputStream in = AppConfig.class.getResourceAsStream(resource)) {
            if (in != null) {
                props.load(new InputStreamReader(in, StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read " + resource, e);
        }
    }

    public static String get(String key) {
        String value = System.getProperty("shop." + key);
        if (value == null) {
            value = System.getenv(envName(key));
        }
        if (value == null) {
            value = PROPS.getProperty(key);
        }
        return value == null ? null : expand(value.trim());
    }

    /** Tên biến môi trường của một khoá: {@code db.url} → {@code SHOP_DB_URL}, {@code db.poolSize} → {@code SHOP_DB_POOLSIZE}. */
    static String envName(String key) {
        return "SHOP_" + key.toUpperCase(Locale.ROOT).replace('.', '_');
    }

    public static String get(String key, String defaultValue) {
        String value = get(key);
        return value == null || value.isEmpty() ? defaultValue : value;
    }

    public static int getInt(String key, int defaultValue) {
        String value = get(key);
        return value == null || value.isEmpty() ? defaultValue : Integer.parseInt(value);
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        String value = get(key);
        return value == null || value.isEmpty() ? defaultValue : Boolean.parseBoolean(value);
    }

    private static String expand(String value) {
        Matcher m = PLACEHOLDER.matcher(value);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            String name = m.group(1);
            String replacement = System.getProperty(name, System.getenv(name));
            String path = replacement == null ? "" : replacement.replace('\\', '/');
            m.appendReplacement(sb, Matcher.quoteReplacement(path));
        }
        m.appendTail(sb);
        return sb.toString();
    }
}
