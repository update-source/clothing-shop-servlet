# Tài liệu dự án — Shoppers (Shop quần áo E-commerce)

Tài liệu mô tả bài toán, thiết kế và cách vận hành ứng dụng. Các sơ đồ viết bằng Mermaid nên xem trực tiếp
được trên GitHub.

| # | Tài liệu | Nội dung |
| --- | --- | --- |
| 1 | [Tổng quan](01-tong-quan.md) | Bài toán, phạm vi, vai trò người dùng, công nghệ, cấu trúc mã nguồn |
| 2 | [Kiến trúc](02-kien-truc.md) | Các tầng, vòng đời một request, transaction, nạp trễ, chống ghi đè đồng thời, bảo mật |
| 3 | [Mô hình lớp](03-mo-hinh-lop.md) | Sơ đồ lớp theo 4 package, ánh xạ lớp → mã nguồn, các điểm bổ sung so với class diagram |
| 4 | [Cơ sở dữ liệu](04-co-so-du-lieu.md) | Sơ đồ ERD, 15 bảng, cách ánh xạ kế thừa / value object / composition |
| 5 | [Chức năng và URL](05-chuc-nang-va-url.md) | Danh sách chức năng theo vai trò, bảng URL, quy tắc phân quyền |
| 6 | [Luồng nghiệp vụ](06-luong-nghiep-vu.md) | Vòng đời đơn hàng, đặt hàng, thanh toán VNPAY, kịch bản A–E |
| 7 | [Cấu hình và triển khai](07-cau-hinh-va-trien-khai.md) | Các khoá cấu hình, đổi cổng, Tomcat, MySQL, VNPAY sandbox |
| 8 | [Kiểm thử](08-kiem-thu.md) | Bộ test tự động, cách chạy, danh sách kiểm thử thủ công |
| 9 | [Hướng dẫn sử dụng](09-huong-dan-su-dung.md) | Thao tác cho khách hàng, nhân viên, quản trị viên |
| 10 | [Quy trình Git](10-quy-trinh-git.md) | Mô hình nhánh, quy ước commit, phiên bản |
| 11 | [Triển khai bằng Docker](11-docker.md) | Docker Compose (H2 / MySQL), biến môi trường, HTTPS qua reverse proxy, deploy miễn phí (Render), sao lưu |

## Tài liệu gốc

- [Đặc tả bài toán](dac-ta/dac-ta-bai-toan.md) — mô hình đối tượng mô tả bằng lời, bảng 28 quan hệ, kịch bản A–E.
- [Class diagram v6](dac-ta/class-diagram-v6.drawio) — mở bằng [diagrams.net](https://app.diagrams.net/)
  (File → Open from → Device).
