# 8. Kiểm thử

## Chạy bộ test tự động

```bash
mvn test
```

52 test JUnit 5, khoảng vài giây. Test tích hợp dùng H2 **trong bộ nhớ** (`TestDatabase`) nên không đụng tới dữ liệu
ở `~/clothing-shop`. `mvn package` cũng chạy toàn bộ test trước khi tạo WAR.

## Danh sách test

### Domain (`src/test/java/com/shop/model`) — không cần CSDL

| Lớp test | Test | Kiểm tra |
| --- | --- | --- |
| `ProductVariantTest` | `reserveKeepsStockButReducesAvailable` | Giữ hàng không trừ tồn kho, chỉ giảm số còn bán |
| | `scenarioB_twoCustomersCannotBuyTheLastShirt` | **Kịch bản B**: người thứ hai bị từ chối; nhả hàng thì bán được lại |
| | `commitRemovesReservedGoodsFromStock` | Xác nhận đơn trừ hẳn hàng đang giữ |
| | `restockAddsToStock` | Nhập kho |
| | `cannotReleaseOrCommitMoreThanReserved` | Không nhả / trừ quá số đang giữ |
| | `productRejectsDuplicateVariantAndSumsStock` | Một cặp size + màu chỉ một biến thể; tổng tồn kho |
| `CartTest` | `addingSameVariantMergesQuantity` | Cộng dồn số lượng cùng biến thể |
| | `cannotAddMoreThanAvailable` | Không thêm quá số còn bán |
| | `setQuantityZeroRemovesLine` | Số lượng 0 = bỏ dòng |
| | `discontinuedProductCannotBeAdded` | Sản phẩm ngừng bán |
| | `totalUsesCurrentPromotionPrice` | Giá trong giỏ theo khuyến mãi hiện tại |
| `DiscountTest` | `percentDiscountIsCappedAtMaxDiscount` | Giảm % có trần |
| | `fixedDiscountNeverExceedsAmount` | Giảm cố định không vượt số tiền |
| | `periodContainsItsBoundaries` | Thời hạn gồm cả hai đầu; kết thúc phải sau bắt đầu |
| | `productTakesHighestOngoingDiscount` | Lấy mức giảm cao nhất trong các chương trình **đang chạy** |
| | `voucherValidityChecksActivePeriodAndUsage` | Voucher: bật/tắt, thời hạn, hết lượt |
| | `voucherApplicabilityChecksTierAndMinimum` | Voucher: hạng khách, giá trị đơn tối thiểu |
| | `customerLevelFollowsSpendingThresholds` | Ngưỡng hạng thành viên |
| | `invalidVoucherCodeRejected` | Định dạng mã |
| `OrderLifecycleTest` | `scenarioA_vnpayWithVoucher` | **Kịch bản A** từ đặt hàng tới hoàn tất, tổng chi tiêu tăng |
| | `scenarioC_cancelConfirmedCodOrder` | **Kịch bản C** |
| | `scenarioD_codCustomerRefusesDelivery` | **Kịch bản D** |
| | `cancelPendingReleasesReservationAndRefundsPaidVnpay` | Huỷ đơn chờ: nhả hàng, hoàn tiền VNPAY đã trả |
| | `codCompletionMarksPaymentPaid` | COD hoàn tất → thanh toán PAID |
| | `returnGoodsRestocksAndRefunds` | Trả hàng: nhập kho, hoàn tiền, không tính vào chi tiêu |
| | `invalidTransitionsAreRejected` | Chuyển trạng thái sai bị từ chối |
| | `retryingVnpayFailsAbandonedAttempt` | Thanh toán lại: lần bỏ dở thành FAILED |
| | `overdueVnpayOrderIsDetected` | Nhận ra đơn VNPAY quá hạn |
| | `perCustomerVoucherLimitIsFreedByCancellation` | Giới hạn lượt/khách; huỷ đơn trả lại lượt |
| | `orderKeepsItsOwnCopyOfShippingAddress` | Đơn giữ bản địa chỉ riêng |
| `CustomerTest` | `scenarioE_onlyBuyersCanReviewOnce` | **Kịch bản E**: chưa mua thì không đánh giá; chỉ một lần; sửa được |
| | `reviewRatingOutOfRangeIsRejected` | Sao ngoài 1–5 |
| | `wishlistAddIsIdempotent` | Thêm yêu thích hai lần vẫn một |
| | `passwordCanBeChangedOnlyWithOldPassword` | Đổi mật khẩu, khoá tài khoản |
| | `addressBookKeepsAtMostOneDefault` | Sổ địa chỉ: trùng nội dung là một, tối đa một mặc định |

### Tầng lưu trữ (`src/test/java/com/shop/persistence`) — H2 trong bộ nhớ

| Lớp test | Test | Kiểm tra |
| --- | --- | --- |
| `SchemaTest` | `schemaCreatesAllTables` | Script tạo đủ 15 bảng |
| `PersistenceIntegrationTest` | `seededCustomerHasHistoryAndReview` | Dữ liệu mẫu nạp lại đúng (lịch sử đơn, đánh giá, tồn kho) |
| | `promotionsAreLoadedForPricing` | Giá cuối theo khuyến mãi sau khi nạp từ CSDL |
| | `searchFiltersByCategoryTreeSizeAndPrice` | Tìm theo cây danh mục, size, giá bán cuối |
| | `employeesAreLoadedWithRoles` | Nhân viên và vai trò |
| | `staleStockSnapshotCannotOversell` | Kịch bản B ở tầng CSDL: ảnh chụp tồn kho cũ không bán quá |
| | `checkoutPersistsReservationVoucherAndClearsCart` | Đặt hàng lưu số đang giữ, lượt voucher, làm trống giỏ; huỷ thì hoàn lại |
| | `concurrentStatusChangeIsRejected` | Hai người cùng đổi trạng thái một đơn → người sau bị từ chối |
| | `newCustomerWithoutDefaultAddressLoadsEmptyBook` | Hồi quy: khách mới chưa có địa chỉ không gây lỗi |

### Dịch vụ (`src/test/java/com/shop/service`)

| Lớp test | Test | Kiểm tra |
| --- | --- | --- |
| `VnpayGatewayTest` | `paymentUrlIsSignedAndVerifiable` | URL thanh toán có chữ ký hợp lệ, số tiền × 100 |
| | `tamperedAmountFailsVerification` | Sửa số tiền / bỏ chữ ký → không qua kiểm tra |
| | `parseReadsResultAndAmount` | Đọc kết quả giao dịch |
| `PaymentExpiryIntegrationTest` | `overdueUnpaidVnpayOrderIsCancelledAndStockReleased` | Job tự huỷ đơn VNPAY quá hạn, nhả hàng |

### Cấu hình (`src/test/java/com/shop/config`)

| Lớp test | Test | Kiểm tra |
| --- | --- | --- |
| `AppConfigTest` | `envNameIsUpperCaseWithUnderscores` | `db.poolSize` → `SHOP_DB_POOLSIZE` |
| | `environmentVariableIsRead` | Đọc được biến môi trường `SHOP_*` (biến thử đặt trong cấu hình surefire của `pom.xml`) |
| | `systemPropertyWinsOverEnvironmentAndFile` | `-Dshop.<khoá>` được ưu tiên nhất |
| | `fileValueAndDefaultsApplyWhenNotOverridden` | Giá trị từ `app.properties` và giá trị mặc định |

## Kiểm thử thủ công (trên giao diện)

Chạy `mvn jetty:run`, dùng các tài khoản mẫu. Nên làm theo thứ tự:

| # | Bước | Kết quả mong đợi |
| --- | --- | --- |
| 1 | Mở "Áo thun basic", chọn màu Đen | Size M bị mờ (hết hàng); chọn L hiện "Còn 7 sản phẩm" |
| 2 | Chưa đăng nhập, bấm "Thêm vào giỏ" | Chuyển tới đăng nhập, xong quay lại trang sản phẩm |
| 3 | Đăng nhập `khachhang`, thêm 2 Đen – L, vào thanh toán, nhập mã `SALE10` | Giảm 10% tối đa 50.000 ₫; mã `VIP100K` báo "chỉ dành cho hạng Thân thiết, VIP" |
| 4 | Đặt hàng bằng VNPAY | Sang cổng giả lập; chọn "Huỷ giao dịch" → kết quả thất bại, trang đơn có nút "Thanh toán VNPAY" |
| 5 | Thanh toán lại, chọn "Xác nhận thanh toán thành công" | Đơn "Đã thanh toán"; lịch sử thanh toán có 1 lần thất bại + 1 lần thành công |
| 6 | Đăng nhập `staff`, mở đơn | Xác nhận → Giao hàng → Đã giao thành công; mỗi bước chỉ hiện nút hợp lệ |
| 7 | Quay lại `khachhang` | Tổng chi tiêu tăng; ở trang sản phẩm có form đánh giá (hoặc form sửa nếu đã đánh giá) |
| 8 | Đặt một đơn COD, nhân viên xác nhận rồi khách bấm "Huỷ đơn" | Kịch bản C: hàng nhập lại kho, lượt voucher được trả |
| 9 | Đặt đơn VNPAY nhưng không thanh toán, chờ quá 15 phút | Đơn tự chuyển "Đã huỷ" |
| 10 | Đăng nhập `admin`: thêm sản phẩm có ảnh, thêm biến thể, tạo khuyến mãi và thêm sản phẩm đó | Giá trên cửa hàng giảm ngay; bỏ khỏi chương trình thì về giá gốc |
| 11 | `admin` khoá tài khoản một khách đang đăng nhập | Khách bị đăng xuất ở thao tác kế tiếp, không đăng nhập lại được |
| 12 | Gửi form mà không có `_csrf` (ví dụ bằng curl) | 403 |
