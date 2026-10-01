# 5. Chức năng và URL

Mọi URL tính từ context path của ứng dụng (mặc định `/`). Form `POST` nào cũng phải kèm tham số `_csrf`.

## Phân quyền theo khu vực

Khai báo trong `WEB-INF/web.xml`, thực thi bởi `AccessFilter`:

| Khu vực | Mẫu URL | Yêu cầu |
| --- | --- | --- |
| Công khai | `/`, `/shop`, `/product`, `/promotions`, `/about`, `/contact`, `/login`, `/register`, `/payment/*`, `/uploads/*` | Không cần đăng nhập |
| Khách hàng | `/account/*`, `/cart`, `/cart/*`, `/wishlist`, `/wishlist/*`, `/checkout`, `/checkout/*`, `/orders`, `/orders/*`, `/reviews/*` | Đăng nhập bằng tài khoản **khách hàng** |
| Nhân viên | `/staff`, `/staff/*` | Tài khoản **nhân viên** (STAFF hoặc ADMIN) |
| Quản trị | `/admin`, `/admin/*` | Tài khoản nhân viên có vai trò **ADMIN** |

Chưa đăng nhập → chuyển tới `/login?next=<trang đang xem>`; sai vai trò → trang lỗi 403; tài khoản bị khoá →
bị đăng xuất ngay ở request kế tiếp.

## Khách vãng lai và khách hàng

| Chức năng | Phương thức & URL | Servlet | Ghi chú |
| --- | --- | --- | --- |
| Trang chủ | `GET /` | `HomeServlet` | Danh mục gốc, 8 sản phẩm mới, khuyến mãi đang chạy |
| Cửa hàng | `GET /shop` | `ShopServlet` | Tham số: `category`, `q`, `size` (nhiều), `color` (nhiều), `minPrice`, `maxPrice`, `sort` (`newest`, `name_asc`, `name_desc`, `price_asc`, `price_desc`), `page` — 9 sản phẩm/trang. Lọc danh mục gồm cả danh mục con; lọc/sắp theo **giá bán cuối** |
| Chi tiết sản phẩm | `GET /product?id=` | `ProductServlet` | Chọn màu → size, hiện số còn bán; đánh giá; sản phẩm liên quan |
| Khuyến mãi | `GET /promotions` | `PromotionsServlet` | Chương trình đang chạy và sản phẩm của từng chương trình |
| Giới thiệu, liên hệ | `GET /about`, `GET /contact` | `PageServlet` | |
| Đăng ký | `GET/POST /register` | `RegisterServlet` | Tự đăng nhập sau khi đăng ký |
| Đăng nhập | `GET/POST /login` | `LoginServlet` | `next` quay lại trang trước; nhân viên vào `/staff/orders`, quản trị vào `/admin` |
| Đăng xuất | `POST /logout` | `LogoutServlet` | Chỉ nhận POST |
| Hồ sơ, hạng thành viên | `GET /account/profile` | `AccountServlet` | Tổng chi tiêu, tiến độ lên hạng |
| Cập nhật hồ sơ | `POST /account/profile` | `AccountServlet` | Họ tên, số điện thoại (`User.updateProfile`) |
| Đổi mật khẩu | `POST /account/password` | `AccountServlet` | Phải nhập mật khẩu cũ |
| Sổ địa chỉ | `GET /account/addresses`, `POST /account/addresses` (`action` = `add` / `replace` / `delete` / `default`) | `AddressServlet` | "Sửa" = thay bằng địa chỉ mới (value object) |
| Giỏ hàng | `GET /cart` | `CartServlet` | Đánh dấu dòng hết hàng / ngừng bán |
| Thêm vào giỏ | `POST /cart/add` (`variantId`, `qty`, tuỳ chọn `buyNow`) | `CartServlet` | Cộng dồn nếu đã có; `buyNow` chuyển thẳng tới thanh toán |
| Sửa số lượng / bỏ dòng | `POST /cart/update` (`qty_<variantId>`), `POST /cart/remove?variantId=` | `CartServlet` | Số lượng 0 = bỏ dòng |
| Yêu thích | `GET /wishlist`, `POST /wishlist/toggle`, `POST /wishlist/remove` (`productId`) | `WishlistServlet` | |
| Thanh toán | `GET /checkout` (`addressId`, `voucher` để xem trước) | `CheckoutServlet` | Hiện phí vận chuyển theo tỉnh, mã khách dùng được |
| Đặt hàng | `POST /checkout` (`addressId` hoặc `new` + các ô địa chỉ, `paymentMethod`, `voucherCode`, `note`, `saveAddress`) | `CheckoutServlet` | COD → `/checkout/success`; VNPAY → cổng thanh toán |
| Trang cảm ơn | `GET /checkout/success?id=` | `CheckoutServlet` | |
| Đơn hàng của tôi | `GET /orders` (`status` để lọc), `GET /orders/detail?id=` | `OrdersServlet` | Chỉ thấy đơn của chính mình |
| Huỷ đơn | `POST /orders/cancel` (`id`) | `OrdersServlet` | Chỉ khi đơn chưa giao |
| Thanh toán VNPAY | `GET /orders/vnpay?id=` (ngay sau đặt hàng), `POST /orders/pay` (`id`, thanh toán lại) | `PaymentServlet` | Chuyển hướng sang cổng VNPAY |
| Đánh giá | `POST /reviews/save` (`productId`, `rating`, `comment`) | `ReviewServlet` | Viết mới hoặc sửa đánh giá cũ |

## Cổng thanh toán

| URL | Ai gọi | Ghi chú |
| --- | --- | --- |
| `GET /payment/vnpay-return` | Trình duyệt khách sau khi thanh toán | Kiểm chữ ký, số tiền; hiển thị kết quả |
| `GET /payment/vnpay-ipn` | Máy chủ VNPAY | Trả JSON `{"RspCode": "...", "Message": "..."}`: `00` đã ghi nhận, `02` đã xử lý trước đó, `01` không tìm thấy, `04` sai số tiền, `97` sai chữ ký |
| `GET /payment/mock-vnpay`, `/payment/mock-vnpay/complete` | Trình duyệt (chỉ khi `vnpay.mode=mock`) | Cổng giả lập để demo; chế độ khác trả 404 |
| `GET /uploads/<tên>` | Trình duyệt | Ảnh sản phẩm đã tải lên |

## Nhân viên (STAFF và ADMIN)

| Chức năng | Phương thức & URL | Servlet |
| --- | --- | --- |
| Tổng quan (doanh thu đơn hoàn tất, đơn chờ xác nhận, số khách, sản phẩm đang bán, đơn theo trạng thái) | `GET /staff` | `StaffOrderServlet` |
| Danh sách đơn (`status`, `q` = mã đơn / tên / SĐT người nhận, `page`) — 15 đơn/trang | `GET /staff/orders` | `StaffOrderServlet` |
| Chi tiết đơn | `GET /staff/orders/detail?id=` | `StaffOrderServlet` |
| Xử lý đơn: `action` = `CONFIRM`, `SHIP`, `COMPLETE`, `FAIL_DELIVERY`, `RETURN_GOODS`, `CANCEL` | `POST /staff/orders/action` | `StaffOrderServlet` |
| Hồ sơ, đổi mật khẩu | `GET/POST /staff/profile`, `POST /staff/password` | `StaffProfileServlet` |

Trang chi tiết chỉ hiện những nút hợp lệ với trạng thái hiện tại (ví dụ đơn VNPAY chưa thanh toán không có nút
"Xác nhận"). Nếu hai người thao tác cùng lúc, người sau nhận thông báo "đơn vừa được người khác cập nhật".

## Quản trị viên (ADMIN)

| Chức năng | Phương thức & URL | Servlet |
| --- | --- | --- |
| Danh mục: thêm, đổi tên / đổi danh mục cha, xoá danh mục rỗng | `GET /admin/categories`, `POST /admin/categories` (`action` = `create` / `update` / `delete`) | `AdminCategoryServlet` |
| Danh sách sản phẩm (tìm theo tên) | `GET /admin/products?q=` | `AdminProductServlet` |
| Tạo sản phẩm (kèm ảnh) | `GET/POST /admin/products/new` (multipart) | `AdminProductServlet` |
| Sửa thông tin / ảnh | `GET/POST /admin/products/edit?id=` (multipart) | `AdminProductServlet` |
| Đổi giá gốc | `POST /admin/products/price` | `AdminProductServlet` |
| Ngừng bán | `POST /admin/products/discontinue` | `AdminProductServlet` |
| Thêm biến thể (màu, size, tồn ban đầu) | `POST /admin/products/variant` | `AdminProductServlet` |
| Nhập kho cho biến thể | `POST /admin/products/restock` | `AdminProductServlet` |
| Khuyến mãi: tạo, đổi tên, thêm/bỏ sản phẩm, xoá | `GET /admin/promotions`, `GET /admin/promotions/edit?id=`, `POST /admin/promotions` (`action` = `create` / `rename` / `addProduct` / `removeProduct` / `delete`) | `AdminPromotionServlet` |
| Voucher: tạo, bật/tắt | `GET /admin/vouchers`, `POST /admin/vouchers` (`action` = `create` / `toggle`) | `AdminVoucherServlet` |
| Tài khoản: xem khách/nhân viên, khoá/mở khoá, tạo nhân viên | `GET /admin/users?type=CUSTOMER\|EMPLOYEE&q=`, `POST /admin/users` (`action` = `lock` / `unlock` / `createEmployee`) | `AdminUserServlet` |

`GET /admin` chuyển về trang tổng quan `/staff`. Quản trị viên không thể tự khoá chính mình.
