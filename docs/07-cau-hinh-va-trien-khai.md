# 7. Cấu hình và triển khai

## Yêu cầu

- JDK 17 trở lên (đã chạy với JDK 21), Maven 3.9+.
- Không bắt buộc cài CSDL: mặc định dùng H2 nhúng. Muốn dùng MySQL thì cần MySQL 8.0.16+.
- Máy cần Internet ở lần build đầu (tải thư viện Maven) và để tải font Be Vietnam Pro; không có mạng thì trang dùng
  font hệ thống.

## Chạy ở môi trường phát triển

```bash
mvn jetty:run
```

Mở http://localhost:8080. Dừng bằng Ctrl+C. Jetty đọc JSP/CSS/JS trực tiếp từ `src/main/webapp` nên sửa giao diện
chỉ cần tải lại trang; **sửa code Java phải dừng và chạy lại**.

## Các khoá cấu hình

Cấu hình đọc theo thứ tự ưu tiên tăng dần (sau ghi đè trước):

1. `src/main/resources/app.properties` — giá trị mặc định (có trong repo).
2. `src/main/resources/app-local.properties` — ghi đè trên máy của bạn (đã `.gitignore`, dùng cho mật khẩu CSDL,
   khoá VNPAY…).
3. System property `-Dshop.<khoá>=<giá trị>` khi chạy.

`${user.home}` trong giá trị được thay bằng thư mục người dùng.

| Khoá | Mặc định | Ý nghĩa |
| --- | --- | --- |
| `db.driver` | `org.h2.Driver` | Lớp JDBC driver (`com.mysql.cj.jdbc.Driver` cho MySQL) |
| `db.url` | `jdbc:h2:file:${user.home}/clothing-shop/data/shop;MODE=MySQL;...` | Chuỗi kết nối |
| `db.username` / `db.password` | `sa` / (trống) | Tài khoản CSDL |
| `db.poolSize` | `10` | Số kết nối tối đa của HikariCP |
| `db.seed` | `true` | Tạo dữ liệu mẫu khi bảng `users` trống |
| `upload.dir` | `${user.home}/clothing-shop/uploads` | Thư mục lưu ảnh sản phẩm tải lên (nằm ngoài webapp để không mất khi deploy lại) |
| `upload.maxBytes` | `2097152` | Dung lượng ảnh tối đa (2 MB) |
| `order.vnpayTimeoutMinutes` | `15` | Hạn thanh toán đơn VNPAY trước khi tự huỷ |
| `vnpay.mode` | `mock` | `mock` = cổng giả lập trong ứng dụng; `sandbox` = cổng VNPAY thật |
| `vnpay.payUrl` | `https://sandbox.vnpayment.vn/paymentv2/vpcpay.html` | URL thanh toán VNPAY (chế độ `sandbox`) |
| `vnpay.tmnCode` | `DEMOSHOP` | Mã website (TMN code) VNPAY cấp |
| `vnpay.hashSecret` | `MOCK_SECRET_FOR_LOCAL_DEMO_ONLY` | Chuỗi bí mật ký HMAC-SHA512 — **thay khi dùng VNPAY thật** |
| `vnpay.returnPath` | `/payment/vnpay-return` | Đường dẫn VNPAY chuyển khách về sau thanh toán |

Ví dụ ghi đè khi chạy:

```bash
mvn jetty:run -Dshop.order.vnpayTimeoutMinutes=30
```

> **PowerShell:** đặt mỗi tham số `-D...` trong dấu nháy, ví dụ `mvn jetty:run "-Dshop.order.vnpayTimeoutMinutes=30"`.
> Không có nháy, PowerShell tách tham số ở dấu chấm và Maven báo `Unknown lifecycle phase`.

## Đổi cổng

| Cách | Làm thế nào |
| --- | --- |
| Tạm thời khi chạy | `mvn jetty:run -Djetty.http.port=8081` (PowerShell: `mvn jetty:run "-Djetty.http.port=8081"`) |
| Cố định cho dự án | Thêm `<httpConnector><port>8081</port></httpConnector>` vào `<configuration>` của plugin `jetty-ee10-maven-plugin` trong `pom.xml` |
| Trên Tomcat | Thuộc tính `port` của `<Connector>` trong `conf/server.xml` của Tomcat |

Ứng dụng không cần sửa gì khi đổi cổng: return URL của VNPAY được ghép từ địa chỉ của request hiện tại.

## Dữ liệu cục bộ

| Thư mục | Nội dung |
| --- | --- |
| `~/clothing-shop/data/shop.mv.db` | CSDL H2 |
| `~/clothing-shop/uploads/` | Ảnh sản phẩm tải lên |

Muốn làm lại từ dữ liệu mẫu: dừng server, xoá thư mục `~/clothing-shop`, chạy lại. Hai server không mở chung được
một file H2 — server thứ hai dùng file khác, ví dụ
`-Dshop.db.url=jdbc:h2:file:./data/shop2;MODE=MySQL;DATABASE_TO_LOWER=TRUE` (thư mục `data/` đã `.gitignore`).

## Triển khai lên Tomcat

1. `mvn package` → tạo `target/clothing-shop.war` (đã gồm H2, MySQL driver, HikariCP, JSTL).
2. Chép vào `webapps/` của **Tomcat 10.1+ hoặc 11** (Jakarta EE 10; Tomcat 9 trở xuống không chạy được).
   Muốn ứng dụng ở `/` thì đặt tên `ROOT.war`; giữ tên `clothing-shop.war` thì địa chỉ là `/clothing-shop/`.
3. Tomcat chạy dưới dạng Windows service dùng tài khoản hệ thống nên `${user.home}` khác với tài khoản của bạn —
   nên đặt `db.url` và `upload.dir` thành đường dẫn tuyệt đối: qua `app-local.properties` trước khi build, hoặc thêm
   `-Dshop.db.url=...` / `-Dshop.upload.dir=...` vào Java Options của Tomcat (`bin/setenv.bat` với `CATALINA_OPTS`
   khi chạy bằng `startup.bat`; tab *Java* của Tomcat Monitor khi chạy dạng service).

## Dùng MySQL

```sql
CREATE DATABASE clothing_shop CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

`src/main/resources/app-local.properties`:

```properties
db.driver=com.mysql.cj.jdbc.Driver
db.url=jdbc:mysql://localhost:3306/clothing_shop?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Ho_Chi_Minh
db.username=root
db.password=your-password
```

Khi khởi động, ứng dụng tự tạo bảng (`db/schema.sql`) và dữ liệu mẫu nếu CSDL trống. Bộ test tự động chạy trên H2;
cấu hình MySQL chưa được kiểm thử tự động.

## Dùng cổng VNPAY sandbox thật

1. Đăng ký tài khoản merchant thử nghiệm tại VNPAY để nhận **TMN code** và **hash secret**.
2. Ghi vào `app-local.properties`:

   ```properties
   vnpay.mode=sandbox
   vnpay.tmnCode=<mã TMN>
   vnpay.hashSecret=<chuỗi bí mật>
   ```

3. Khai báo với VNPAY:
   - Return URL: `https://<tên miền>/payment/vnpay-return`
   - IPN URL: `https://<tên miền>/payment/vnpay-ipn` — VNPAY phải gọi tới được từ Internet; khi chạy trên máy cá
     nhân, return URL vẫn đủ để ghi nhận kết quả vì hai đường dùng chung một cách xử lý.

Không commit `hashSecret` thật lên repo.
