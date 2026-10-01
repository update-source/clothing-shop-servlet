package com.shop.web.servlet;

import com.shop.service.ImageStorage;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Phục vụ ảnh sản phẩm đã tải lên từ thư mục upload (chỉ tên tệp do hệ thống sinh). */
@WebServlet("/uploads/*")
public class UploadServlet extends HttpServlet {

    private final ImageStorage storage = new ImageStorage();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String info = req.getPathInfo();
        Path file = info == null ? null : storage.resolve(info.substring(1));
        if (file == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        String name = file.getFileName().toString();
        String type = name.endsWith(".png") ? "image/png" : name.endsWith(".webp") ? "image/webp"
                : name.endsWith(".gif") ? "image/gif" : "image/jpeg";
        resp.setContentType(type);
        resp.setHeader("Cache-Control", "public, max-age=86400");
        resp.setHeader("X-Content-Type-Options", "nosniff");
        resp.setContentLengthLong(Files.size(file));
        Files.copy(file, resp.getOutputStream());
    }
}
