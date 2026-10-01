package com.shop.persistence;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Ghi nhớ trạng thái gốc của đối tượng lúc nạp từ CSDL (theo từng request/luồng).
 * DAO dùng nó để ghi các bộ đếm bằng chênh lệch (tồn kho, lượt voucher) và chặn ghi đè trạng thái
 * đã bị người khác đổi (trạng thái đơn, thanh toán) — tránh mất cập nhật khi hai người thao tác cùng lúc.
 */
public final class Tracker {

    private static final ThreadLocal<Map<Object, Object>> ORIGINALS =
            ThreadLocal.withInitial(IdentityHashMap::new);

    private Tracker() {
    }

    public static void remember(Object entity, Object state) {
        ORIGINALS.get().put(entity, state);
    }

    @SuppressWarnings("unchecked")
    public static <T> T original(Object entity) {
        return (T) ORIGINALS.get().get(entity);
    }

    public static void clear() {
        ORIGINALS.remove();
    }
}
