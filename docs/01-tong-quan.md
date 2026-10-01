# 1. Tổng quan

## Bài toán

Cửa hàng quần áo bán trực tuyến. Khách hàng xem sản phẩm, chọn **size và màu**, cho vào giỏ, đặt hàng,
thanh toán (COD hoặc VNPAY) và viết đánh giá. Nhân viên xác nhận đơn và giao hàng. Quản trị viên quản lý sản phẩm,
khuyến mãi, voucher và tài khoản. Chi tiết nghiệp vụ: [đặc tả bài toán](dac-ta/dac-ta-bai-toan.md).

**Trong phạm vi:** tài khoản, sổ địa chỉ, danh mục & sản phẩm (biến thể size × màu), giỏ hàng, danh sách yêu thích,
đơn hàng (kể cả giao thất bại và trả hàng), thanh toán COD/VNPAY, voucher, chương trình khuyến mãi, đánh giá.

**Ngoài phạm vi (theo đặc tả):** tích hợp đơn vị vận chuyển (phí ship lấy theo bảng giá tỉnh/thành — xem
`ShippingService`), quản lý nhiều kho.

## Vai trò

| Vai trò | Lớp | Quyền chính |
| --- | --- | --- |
| Khách vãng lai | — | Xem cửa hàng, sản phẩm, khuyến mãi; đăng ký, đăng nhập |
| Khách hàng | `Customer` | Giỏ hàng, yêu thích, sổ địa chỉ, đặt hàng, thanh toán, huỷ đơn chưa giao, đánh giá sản phẩm đã mua |
| Nhân viên | `Employee` (role `STAFF`) | Xem tổng quan, xử lý đơn: xác nhận, giao, hoàn tất, giao thất bại, nhận trả hàng, huỷ |
| Quản trị viên | `Employee` (role `ADMIN`) | Mọi quyền của nhân viên + danh mục, sản phẩm, khuyến mãi, voucher, khoá tài khoản, tạo nhân viên |
| Cổng VNPAY | — (hệ thống ngoài) | Báo kết quả giao dịch về return URL / IPN |

Một người vừa là nhân viên vừa muốn mua hàng cần hai tài khoản (đánh đổi có chủ ý của thiết kế kế thừa
`User` → `Customer` / `Employee`). Tài khoản nhân viên vào khu mua hàng sẽ nhận trang báo lỗi 403.

## Công nghệ

| Thành phần | Lựa chọn | Ghi chú |
| --- | --- | --- |
| Ngôn ngữ | Java 17+ | Đã chạy với JDK 21 |
| Web | Jakarta Servlet 6.0, JSP 3.1, JSTL 3.0 | Không dùng framework MVC; controller là `HttpServlet` |
| Máy chủ | Jetty 12 (`mvn jetty:run`) hoặc Tomcat 10.1+/11 | Đóng gói WAR |
| CSDL | H2 2.2 nhúng (chế độ MySQL) hoặc MySQL 8 | JDBC thuần, pool HikariCP |
| Giao diện | Template *Shoppers* (Colorlib, CC BY 3.0), Bootstrap 4, jQuery | Việt hoá, font Be Vietnam Pro |
| Thanh toán | VNPAY API 2.1.0 | Có cổng giả lập (`vnpay.mode=mock`) để demo |
| Kiểm thử | JUnit 5 | 48 test: domain, DAO trên H2, VNPAY |

## Cấu trúc mã nguồn

```
.
├── pom.xml
├── README.md
├── docs/                                # tài liệu này
└── src
    ├── main
    │   ├── java/com/shop
    │   │   ├── model/                   # domain model — 4 package như class diagram
    │   │   │   ├── account/             #   Tài khoản
    │   │   │   ├── catalog/             #   Sản phẩm & mua sắm
    │   │   │   ├── order/               #   Đơn hàng & thanh toán
    │   │   │   ├── discount/            #   Giảm giá
    │   │   │   └── Entity, Lazy, Check, DomainException
    │   │   ├── dao/                     # JDBC: nạp/lưu đối tượng nghiệp vụ
    │   │   ├── persistence/             # pool, transaction, tạo schema, dữ liệu mẫu
    │   │   ├── service/                 # ca sử dụng (mỗi thao tác = 1 transaction)
    │   │   ├── web/                     # servlet, filter, hàm EL
    │   │   ├── config/AppConfig.java    # đọc app.properties
    │   │   └── util/                    # PasswordHasher, Money
    │   ├── resources
    │   │   ├── app.properties           # cấu hình mặc định
    │   │   └── db/schema.sql            # lược đồ CSDL
    │   └── webapp
    │       ├── css, js, fonts, images   # tài nguyên tĩnh của template
    │       └── WEB-INF
    │           ├── web.xml              # filter, phân quyền, trang lỗi
    │           ├── shop.tld             # hàm EL (f:vnd, f:dateTime...)
    │           ├── tags/                # tag file (thẻ sản phẩm)
    │           └── views/               # JSP theo khu vực
    └── test/java/com/shop               # JUnit 5
```

## Dữ liệu mẫu

Tạo tự động khi CSDL còn trống (`DataSeeder`), bằng chính các hành vi của domain (đăng ký, thêm biến thể,
đặt hàng, xác nhận, giao, đánh giá):

- 3 tài khoản: `admin/admin123` (ADMIN), `staff/staff123` (STAFF), `khachhang/123456` (khách, có 2 địa chỉ).
- 10 danh mục hai cấp: Áo (Áo thun, Áo polo, Áo sơ mi, Áo khoác), Quần (Quần jean, Quần short), Giày dép (Sneaker).
- 10 sản phẩm, 44 biến thể. Tồn kho "Áo thun basic" lấy đúng bảng ví dụ trong đặc tả (Đen – M = 0).
- 2 chương trình khuyến mãi: "Sale tháng 10 — giảm 10%" (tối đa 50.000 ₫), "Flash sale giày — giảm 100.000 ₫".
- 3 voucher: `WELCOME30K`, `SALE10`, `VIP100K` (chỉ hạng Thân thiết/VIP).
- 1 đơn COD đã hoàn tất của `khachhang` kèm 1 đánh giá 5 sao.
