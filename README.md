# Shoppers — Shop quần áo E-commerce (Java Servlet/JSP)

Cửa hàng quần áo bán trực tuyến xây dựng bằng **Java Servlet + JSP/JSTL + JDBC**, cài đặt theo
"Đặc tả bài toán — Shop quần áo E-commerce" và **class diagram v6**. Giao diện dựa trên template
*Shoppers* (Colorlib, CC BY 3.0), Việt hoá toàn bộ.

Tài liệu chi tiết (kiến trúc, mô hình lớp, CSDL, URL, luồng nghiệp vụ, kiểm thử, hướng dẫn sử dụng):
**[docs/](docs/README.md)** — kèm [đặc tả bài toán](docs/dac-ta/dac-ta-bai-toan.md) và
[class diagram v6](docs/dac-ta/class-diagram-v6.drawio).

## Công nghệ

| Thành phần | Lựa chọn |
| --- | --- |
| Ngôn ngữ / build | Java 17+, Maven (đóng gói WAR) |
| Web | Jakarta Servlet 6, JSP 3.1, JSTL 3 — chạy trên Jetty 12 (`mvn jetty:run`) hoặc Tomcat 10.1+/11 |
| CSDL | H2 nhúng (chế độ MySQL, mặc định) hoặc MySQL 8; JDBC thuần + HikariCP |
| Thanh toán | VNPAY API 2.1.0 (HMAC-SHA512) + cổng giả lập để demo |
| Test | JUnit 5 (domain, DAO trên H2, VNPAY) |

## Chạy nhanh

```bash
mvn jetty:run
```

Mở http://localhost:8080. Lần chạy đầu ứng dụng tự tạo bảng và dữ liệu mẫu
(10 sản phẩm, 2 chương trình khuyến mãi, 3 voucher, 1 đơn đã hoàn tất kèm đánh giá).

| Vai trò | Tên đăng nhập | Mật khẩu |
| --- | --- | --- |
| Quản trị viên (ADMIN) | `admin` | `admin123` |
| Nhân viên (STAFF) | `staff` | `staff123` |
| Khách hàng | `khachhang` | `123456` |

Voucher mẫu: `WELCOME30K` (đơn từ 199.000 ₫), `SALE10` (10%, tối đa 50.000 ₫, đơn từ 300.000 ₫),
`VIP100K` (chỉ hạng Thân thiết/VIP).

Dữ liệu H2 nằm ở `~/clothing-shop/data`, ảnh tải lên ở `~/clothing-shop/uploads`.
Xoá thư mục `~/clothing-shop` để làm lại từ dữ liệu mẫu.

### Đổi cổng (port)

Mặc định Jetty chạy ở cổng 8080. Đổi khi chạy bằng tham số `jetty.http.port`:

```bash
# Git Bash / CMD / Linux / macOS
mvn jetty:run -Djetty.http.port=8081
```

```powershell
# PowerShell: phải đặt tham số -D trong dấu nháy, nếu không PowerShell tách ở dấu chấm
# và Maven báo "Unknown lifecycle phase .http.port=8081"
mvn jetty:run "-Djetty.http.port=8081"
```

Muốn cố định cổng, thêm vào phần `<configuration>` của plugin `jetty-ee10-maven-plugin` trong `pom.xml`:

```xml
<httpConnector>
    <port>8081</port>
</httpConnector>
```

Khi triển khai lên Tomcat, cổng do Tomcat quyết định (thuộc tính `port` của `<Connector>` trong `conf/server.xml`).
Không cần sửa gì trong ứng dụng: return URL của VNPAY được ghép từ địa chỉ của request hiện tại.

### Triển khai lên Tomcat

```bash
mvn package
```

Chép `target/clothing-shop.war` vào `webapps/` của Tomcat 10.1+ (hoặc 11).

### Dùng MySQL thay cho H2

1. Tạo CSDL: `CREATE DATABASE clothing_shop CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;`
2. Tạo `src/main/resources/app-local.properties` (đã được `.gitignore`):

```properties
db.driver=com.mysql.cj.jdbc.Driver
db.url=jdbc:mysql://localhost:3306/clothing_shop?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Ho_Chi_Minh
db.username=root
db.password=your-password
```

Bảng được tạo tự động từ `src/main/resources/db/schema.sql` (cú pháp chung cho MySQL 8 và H2).
Mọi khoá trong `app.properties` cũng có thể ghi đè bằng system property `-Dshop.<khoá>=...`,
ví dụ `mvn jetty:run "-Dshop.order.vnpayTimeoutMinutes=30"` (trong PowerShell nhớ đặt trong dấu nháy).

### VNPAY

Mặc định `vnpay.mode=mock`: khi khách chọn VNPAY, ứng dụng chuyển sang một **cổng giả lập** chạy ngay trong
ứng dụng (`/payment/mock-vnpay`). Yêu cầu và kết quả vẫn được ký/kiểm chữ ký HMAC-SHA512 như VNPAY thật.
Để dùng sandbox VNPAY thật, đăng ký tài khoản merchant thử nghiệm rồi đặt trong `app-local.properties`:

```properties
vnpay.mode=sandbox
vnpay.tmnCode=<mã TMN được cấp>
vnpay.hashSecret=<chuỗi bí mật được cấp>
```

Return URL: `/payment/vnpay-return`; IPN URL: `/payment/vnpay-ipn`. Đơn VNPAY không thanh toán sau
`order.vnpayTimeoutMinutes` (mặc định 15 phút) sẽ tự huỷ để nhả hàng đang giữ và trả lượt voucher.

## Chức năng

**Khách hàng** — đăng ký/đăng nhập, hồ sơ và hạng thành viên (theo tổng chi tiêu), đổi mật khẩu; sổ địa chỉ
(thêm, thay, xoá, đặt mặc định — 34 tỉnh/thành mô hình hai cấp); duyệt cửa hàng (lọc theo danh mục cây, từ khoá,
size, màu, khoảng giá; sắp xếp; phân trang); trang sản phẩm chọn biến thể size + màu với tồn kho từng biến thể;
yêu thích; giỏ hàng; thanh toán chọn cùng lúc địa chỉ, phương thức (COD/VNPAY) và voucher; lịch sử và chi tiết đơn,
huỷ đơn chưa giao, thanh toán lại VNPAY; đánh giá sản phẩm đã mua (1 lần/sản phẩm, sửa được).

**Nhân viên (STAFF)** — tổng quan; danh sách đơn (lọc trạng thái, tìm theo mã/tên/SĐT); xử lý đơn:
xác nhận → giao → hoàn tất / giao thất bại, nhận trả hàng, huỷ.

**Quản trị viên (ADMIN)** — mọi quyền của nhân viên, cộng: danh mục, sản phẩm (ảnh, đổi giá, ngừng bán,
biến thể, nhập kho), chương trình khuyến mãi, voucher, khoá/mở khoá tài khoản, tạo tài khoản nhân viên.

## Kiến trúc

```
src/main/java/com/shop
├── model/            # Domain model — đúng 4 package của class diagram
│   ├── account/      #   User (abstract), Customer, Employee, AddressBook, Address, Gender, EmployeeRole, CustomerLevel
│   ├── catalog/      #   Product, ProductVariant, Category, Cart, CartItem, Review
│   ├── order/        #   Order, OrderDetail, Payment, OrderStatus, PaymentMethod, PaymentStatus
│   └── discount/     #   Voucher, Promotion, DiscountPolicy, PercentDiscount, FixedAmountDiscount, Period
├── dao/              # JDBC: nạp/lưu đối tượng nghiệp vụ (nạp theo lô, quan hệ nạp trễ)
├── persistence/      # Pool kết nối, transaction theo luồng, tạo schema, dữ liệu mẫu
├── service/          # Ca sử dụng: mỗi thao tác = 1 transaction gọi hành vi của domain rồi lưu
└── web/              # Servlet (controller), filter (UTF-8, CSRF, phân quyền), hàm EL
src/main/webapp/WEB-INF/views   # JSP theo khu vực: shop, cart, checkout, orders, account, staff, admin
```

Nguyên tắc chính:

- **Logic nằm trong domain, đúng class diagram.** Ví dụ đặt hàng: `Customer.checkout(...)` tạo `Order`;
  đơn tự tạo `OrderDetail`, gọi `ProductVariant.reserve`, hỏi `Voucher` rồi `redeem`; giỏ được `clear`.
  Vòng đời đơn chỉ đổi qua `confirm/ship/complete/failDelivery/returnGoods/cancel`, mỗi hành động tự kiểm tra
  trạng thái và tự tác động lên kho, voucher, thanh toán theo bảng trong đặc tả.
- **Giá trị tính được không lưu thành cột**: tổng tiền, giảm giá, hạng khách, điểm trung bình, giá bán cuối.
- **Không bán quá số hàng thật**: tồn kho/số đang giữ và lượt voucher được ghi bằng chênh lệch kèm điều kiện
  `0 ≤ giữ ≤ tồn` (kịch bản B — hai khách cùng mua chiếc áo cuối); trạng thái đơn/thanh toán ghi kèm điều kiện
  "trạng thái vẫn như lúc nạp" để hai người không xử lý chồng lên nhau.
- **Bảo mật**: mật khẩu PBKDF2-SHA256 có salt, đổi session id khi đăng nhập, token CSRF cho mọi POST,
  escape đầu ra JSP, phân quyền theo khu vực (`/account`, `/cart`, `/orders`… khách; `/staff/*` nhân viên;
  `/admin/*` quản trị), tài khoản bị khoá bị đăng xuất ở request kế tiếp, ảnh tải lên kiểm tra chữ ký tệp.

## Kiểm thử

```bash
mvn test
```

Gồm test domain cho các kịch bản A–E trong đặc tả (VNPAY + voucher, chiếc áo cuối cùng, huỷ đơn COD đã xác nhận,
khách từ chối nhận hàng, viết đánh giá), test DAO/transaction trên H2, test ký/kiểm chữ ký VNPAY và tự huỷ đơn quá hạn.

## Xử lý sự cố

| Hiện tượng | Nguyên nhân / cách xử lý |
| --- | --- |
| `Unknown lifecycle phase ".http.port=8081"` | Chạy trong PowerShell mà không đặt tham số `-D...` trong dấu nháy — xem mục *Đổi cổng*. |
| `Address already in use` / `Failed to bind to 0.0.0.0:8080` | Cổng đang bị chiếm (thường là một lần `mvn jetty:run` khác chưa tắt). Tìm tiến trình: `netstat -ano \| findstr :8080`, dừng: `taskkill /PID <pid> /F`; hoặc chạy ở cổng khác. |
| Sửa code Java nhưng web không đổi | Jetty chỉ tự nạp lại JSP/CSS/JS; thay đổi Java cần dừng (Ctrl+C) và chạy lại `mvn jetty:run`. |
| Chữ tiếng Việt hiển thị lẫn font / CSS cũ | Trình duyệt còn giữ CSS cũ — nhấn Ctrl+F5. Font Be Vietnam Pro tải từ Google Fonts; khi offline trang dùng font hệ thống (vẫn đủ dấu). |
| Không thấy dữ liệu mẫu / muốn làm lại từ đầu | Dừng server, xoá thư mục `~/clothing-shop` rồi chạy lại — dữ liệu mẫu được tạo khi CSDL trống. |
| `Database may be already in use` (H2) | Hai server cùng mở một file H2. Tắt bớt một server, hoặc cho server thứ hai dùng file khác: `"-Dshop.db.url=jdbc:h2:file:./data/shop2;MODE=MySQL;DATABASE_TO_LOWER=TRUE"`. |

## Quy trình Git

- `main` — bản phát hành; `develop` — nhánh tích hợp.
- Mỗi chức năng phát triển trên một nhánh `feature/*` (domain-model, persistence, web-foundation, auth, address-book,
  catalog, cart, wishlist, checkout, customer-orders, vnpay-payment, reviews, staff-orders, admin-catalog,
  admin-discounts, admin-users), sửa lỗi trên `fix/*`, rồi merge `--no-ff` vào `develop`.
