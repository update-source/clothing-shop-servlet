package com.shop.service;

import com.shop.config.AppConfig;
import com.shop.model.DomainException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Lưu ảnh sản phẩm tải lên vào thư mục ngoài webapp (cấu hình {@code upload.dir}) để không mất khi deploy lại.
 * Chỉ nhận JPEG/PNG/WEBP/GIF (kiểm tra cả chữ ký tệp), tên tệp sinh ngẫu nhiên.
 */
public class ImageStorage {

    public static final String URL_PREFIX = "uploads/";
    private static final Pattern SAFE_NAME = Pattern.compile("^[a-f0-9\\-]{36}\\.(jpg|png|webp|gif)$");
    private static final Map<String, String> EXTENSIONS = Map.of(
            "image/jpeg", "jpg", "image/png", "png", "image/webp", "webp", "image/gif", "gif");

    public Path directory() {
        return Paths.get(AppConfig.get("upload.dir", System.getProperty("java.io.tmpdir") + "/shop-uploads"));
    }

    /** Lưu ảnh và trả về đường dẫn tương đối dùng cho Product.imageUrl, ví dụ "uploads/xxx.jpg". */
    public String save(String contentType, long size, InputStream content) {
        String ext = contentType == null ? null : EXTENSIONS.get(contentType.toLowerCase());
        if (ext == null) {
            throw new DomainException("Chỉ chấp nhận ảnh JPG, PNG, WEBP hoặc GIF");
        }
        int max = AppConfig.getInt("upload.maxBytes", 2 * 1024 * 1024);
        if (size <= 0 || size > max) {
            throw new DomainException("Ảnh phải nhỏ hơn " + (max / 1024 / 1024) + " MB");
        }
        try {
            byte[] bytes = content.readNBytes(max + 1);
            if (bytes.length > max || !matchesSignature(bytes, ext)) {
                throw new DomainException("Tệp tải lên không phải ảnh hợp lệ");
            }
            Files.createDirectories(directory());
            String name = UUID.randomUUID() + "." + ext;
            Path target = directory().resolve(name);
            Files.write(target, bytes);
            return URL_PREFIX + name;
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot store upload", e);
        }
    }

    /** Đường dẫn tệp trên đĩa cho tên an toàn, hoặc null nếu tên không hợp lệ / không tồn tại. */
    public Path resolve(String name) {
        if (name == null || !SAFE_NAME.matcher(name).matches()) {
            return null;
        }
        Path file = directory().resolve(name);
        return Files.isRegularFile(file) ? file : null;
    }

    private static boolean matchesSignature(byte[] b, String ext) {
        return switch (ext) {
            case "jpg" -> b.length > 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF;
            case "png" -> b.length > 8 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G';
            case "gif" -> b.length > 6 && b[0] == 'G' && b[1] == 'I' && b[2] == 'F';
            case "webp" -> b.length > 12 && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                    && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P';
            default -> false;
        };
    }
}
