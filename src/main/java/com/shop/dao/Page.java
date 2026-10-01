package com.shop.dao;

import java.util.List;

/** Một trang kết quả. {@code page} bắt đầu từ 1. */
public record Page<T>(List<T> items, int page, int pageSize, long total) {

    public int getTotalPages() {
        return (int) Math.max(1, (total + pageSize - 1) / pageSize);
    }

    public boolean isHasPrevious() {
        return page > 1;
    }

    public boolean isHasNext() {
        return page < getTotalPages();
    }

    public List<T> getItems() {
        return items;
    }

    public int getPage() {
        return page;
    }

    public long getTotal() {
        return total;
    }
}
