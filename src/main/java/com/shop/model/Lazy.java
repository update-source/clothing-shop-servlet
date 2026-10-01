package com.shop.model;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Giá trị nạp trễ cho các quan hệ. Tầng lưu trữ cung cấp cách nạp, còn đối tượng nghiệp vụ
 * chỉ thấy giá trị — nhờ đó các phương thức như {@code Customer.getTotalSpent()} không phụ thuộc JDBC.
 */
public final class Lazy<T> implements Supplier<T> {

    private Supplier<? extends T> loader;
    private T value;
    private boolean loaded;

    private Lazy(Supplier<? extends T> loader) {
        this.loader = loader;
    }

    public static <T> Lazy<T> of(T value) {
        Lazy<T> lazy = new Lazy<>(null);
        lazy.value = value;
        lazy.loaded = true;
        return lazy;
    }

    public static <T> Lazy<T> from(Supplier<? extends T> loader) {
        return new Lazy<>(Objects.requireNonNull(loader));
    }

    @Override
    public synchronized T get() {
        if (!loaded) {
            value = loader.get();
            loaded = true;
            loader = null;
        }
        return value;
    }

    public synchronized boolean isLoaded() {
        return loaded;
    }
}
