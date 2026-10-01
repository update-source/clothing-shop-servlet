# 3. Mô hình lớp

Domain model cài đặt theo [class diagram v6](dac-ta/class-diagram-v6.drawio): 22 lớp/interface và 6 enum, chia đúng
4 package. Các sơ đồ dưới đây vẽ lại từ mã nguồn (chỉ ghi thuộc tính và phương thức có trên class diagram).

## Tổng thể các quan hệ

```mermaid
classDiagram
    direction LR
    User <|-- Customer
    User <|-- Employee
    Customer "1" *-- "1" AddressBook
    AddressBook "1" *-- "0..*" Address
    AddressBook --> "0..1" Address : mặc định
    Customer "1" *-- "1" Cart
    Cart "1" *-- "0..*" CartItem
    CartItem "0..*" --> "1" ProductVariant
    Customer "0..*" --> "0..*" Product : yêu thích
    Product "1" *-- "1..*" ProductVariant
    Product "1" *-- "0..*" Review
    Review "0..*" --> "1" Customer : viết bởi
    Product "0..*" --> "1" Category
    Category "0..*" --> "0..1" Category : cha
    Customer "1" -- "0..*" Order : đặt
    Order "1" *-- "1..*" OrderDetail
    Order "1" *-- "1" Address : giao đến
    OrderDetail "0..*" --> "1" ProductVariant
    Order "0..*" --> "0..1" Employee : xử lý
    Order "1" --> "0..*" Payment
    Order "0..*" --> "0..1" Voucher : áp dụng
    Product "0..*" -- "0..*" Promotion
    Voucher "1" *-- "1" DiscountPolicy
    Voucher "1" *-- "1" Period
    Promotion "1" *-- "1" DiscountPolicy
    Promotion "1" *-- "1" Period
    DiscountPolicy <|.. PercentDiscount
    DiscountPolicy <|.. FixedAmountDiscount
```

## Package Tài khoản — `com.shop.model.account`

```mermaid
classDiagram
    class User {
        <<abstract>>
        -String username
        -String password
        -String fullName
        -Gender gender
        -LocalDate dob
        -String email
        -String phone
        -boolean active
        +verifyPassword(raw) boolean
        +changePassword(oldPw, newPw) void
        +updateProfile(fullName, phone) void
        +isActive() boolean
        +lock() void
        +unlock() void
    }
    class Customer {
        +addToWishlist(product) void
        +removeFromWishlist(product) void
        +checkout(address, method, voucher, shippingFee, note) Order
        +writeReview(product, rating, comment) Review
        +hasPurchased(product) boolean
        +countVoucherUsage(voucher) int
        +getTotalSpent() BigDecimal
        +getLevel() CustomerLevel
    }
    class Employee {
        -LocalDate hireDate
        -EmployeeRole role
        +isAdmin() boolean
    }
    class AddressBook {
        +add(address) void
        +remove(address) void
        +setDefault(address) void
        +getDefault() Address
    }
    class Address {
        <<value object>>
        -String recipientName
        -String phone
        -String street
        -String ward
        -String province
        +getFullAddress() String
    }
    class CustomerLevel {
        <<enumeration>>
        CASUAL
        REGULAR
        LOYAL
        -BigDecimal minSpent
        +fromSpent(total) CustomerLevel
    }
    User <|-- Customer
    User <|-- Employee
    Customer "1" *-- "1" AddressBook
    AddressBook "1" *-- "0..*" Address
    Customer ..> CustomerLevel
```

## Package Sản phẩm & mua sắm — `com.shop.model.catalog`

```mermaid
classDiagram
    class Product {
        -String name
        -String description
        -BigDecimal basePrice
        -boolean active
        +addVariant(size, color, qty) ProductVariant
        +changePrice(newPrice) void
        +discontinue() void
        +addReview(review) void
        +hasReviewBy(customer) boolean
        +getFinalPrice(now) BigDecimal
        +getTotalStock() int
        +getAverageRating() double
    }
    class ProductVariant {
        -String size
        -String color
        -int stockQuantity
        -int reservedQuantity
        +getAvailableQuantity() int
        +isAvailable(qty) boolean
        +reserve(qty) void
        +releaseReservation(qty) void
        +commit(qty) void
        +restock(qty) void
    }
    class Category {
        -String name
    }
    class Cart {
        +addItem(variant, qty) void
        +removeItem(variant) void
        +setQuantity(variant, qty) void
        +getTotal() BigDecimal
        +clear() void
    }
    class CartItem {
        -int quantity
        +getSubtotal() BigDecimal
    }
    class Review {
        -int rating
        -String comment
        +edit(rating, comment) void
    }
    Product "1" *-- "1..*" ProductVariant
    Product "1" *-- "0..*" Review
    Product "0..*" --> "1" Category
    Category "0..*" --> "0..1" Category : cha
    Cart "1" *-- "0..*" CartItem
    CartItem "0..*" --> "1" ProductVariant
```

## Package Đơn hàng & thanh toán — `com.shop.model.order`

```mermaid
classDiagram
    class Order {
        -LocalDateTime orderDate
        -String note
        -PaymentMethod paymentMethod
        -BigDecimal shippingFee
        -OrderStatus status
        +pay() Payment
        +confirm(by) void
        +ship(by) void
        +complete() void
        +failDelivery() void
        +returnGoods() void
        +cancel() void
        +isPaid() boolean
        +getSubtotal() BigDecimal
        +getDiscount() BigDecimal
        +getTotal() BigDecimal
        -addDetail(item) void
        -applyVoucher(voucher) void
        -canTransitionTo(next) boolean
    }
    class OrderDetail {
        -BigDecimal unitPrice
        -int quantity
        +getSubtotal() BigDecimal
    }
    class Payment {
        -String transactionNo
        -BigDecimal amount
        -PaymentStatus status
        -LocalDateTime paidAt
        +markPaid(transactionNo) void
        +markFailed() void
        +refund() void
        +isSuccess() boolean
    }
    class OrderStatus {
        <<enumeration>>
        PENDING
        CONFIRMED
        SHIPPING
        COMPLETED
        CANCELLED
        DELIVERY_FAILED
        RETURNED
    }
    Order "1" *-- "1..*" OrderDetail
    Order "1" --> "0..*" Payment
    Order ..> OrderStatus
```

## Package Giảm giá — `com.shop.model.discount`

```mermaid
classDiagram
    class DiscountPolicy {
        <<interface>>
        +calculate(amount) BigDecimal
    }
    class PercentDiscount {
        -BigDecimal percent
        -BigDecimal maxDiscount
        +calculate(amount) BigDecimal
    }
    class FixedAmountDiscount {
        -BigDecimal amount
        +calculate(amount) BigDecimal
    }
    class Period {
        <<value object>>
        -LocalDateTime start
        -LocalDateTime end
        +contains(time) boolean
    }
    class Voucher {
        -String code
        -Set~CustomerLevel~ applicableTiers
        -BigDecimal minOrderValue
        -int usageLimit
        -int perCustomerLimit
        -int usedCount
        -boolean active
        +isValid(now) boolean
        +isApplicable(customer, amount) boolean
        +calculateDiscount(amount) BigDecimal
        +redeem() void
        +release() void
    }
    class Promotion {
        -String name
        +isOngoing(now) boolean
        +apply(price) BigDecimal
        +addProduct(product) void
        +removeProduct(product) void
    }
    DiscountPolicy <|.. PercentDiscount
    DiscountPolicy <|.. FixedAmountDiscount
    Voucher "1" *-- "1" DiscountPolicy
    Voucher "1" *-- "1" Period
    Promotion "1" *-- "1" DiscountPolicy
    Promotion "1" *-- "1" Period
```

## Ánh xạ lớp → mã nguồn

| Lớp | Tệp | Ghi chú cài đặt |
| --- | --- | --- |
| `User` (abstract) | `model/account/User.java` | Mật khẩu lưu dạng băm; `lock/unlock` đổi trạng thái của chính tài khoản |
| `Customer` | `model/account/Customer.java` | `checkout` tạo `Order` từ giỏ rồi `cart.clear()`; hạng = `CustomerLevel.fromSpent(getTotalSpent())` |
| `Employee` | `model/account/Employee.java` | `Employee.hire(...)` tạo tài khoản nhân viên |
| `AddressBook` | `model/account/AddressBook.java` | Giữ địa chỉ mặc định bằng liên kết riêng; xoá địa chỉ mặc định thì chọn địa chỉ còn lại đầu tiên |
| `Address` | `model/account/Address.java` | Value object: `equals` theo nội dung; `copy()` cho bản địa chỉ riêng của đơn |
| `CustomerLevel` | `model/account/CustomerLevel.java` | Ngưỡng: Thành viên 0 ₫, Thân thiết 2.000.000 ₫, VIP 10.000.000 ₫ |
| `Product` | `model/catalog/Product.java` | Giá cuối = mức giảm cao nhất trong các khuyến mãi đang chạy |
| `ProductVariant` | `model/catalog/ProductVariant.java` | Bất biến `0 ≤ reserved ≤ stock` giữ trong domain và ở CSDL |
| `Category` | `model/catalog/Category.java` | Cây cha – con, chặn vòng lặp khi đổi cha |
| `Cart`, `CartItem` | `model/catalog/Cart.java`, `CartItem.java` | Chỉ `Cart` sửa được số lượng dòng (`CartItem.setQuantity` là package-private) |
| `Review` | `model/catalog/Review.java` | Sao ngoài 1–5 bị từ chối ngay khi tạo/sửa |
| `Order` | `model/order/Order.java` | Bảng chuyển trạng thái trong `canTransitionTo`; tác động kho/voucher/tiền theo đặc tả |
| `OrderDetail` | `model/order/OrderDetail.java` | Giữ đơn giá đã chốt |
| `Payment` | `model/order/Payment.java` | Chỉ `Order.pay()` tạo được (constructor package-private) |
| `DiscountPolicy` & cài đặt | `model/discount/*.java` | Thêm kiểu giảm mới = thêm một lớp cài đặt (Open/Closed) |
| `Voucher`, `Promotion`, `Period` | `model/discount/*.java` | `Promotion` ↔ `Product` hai chiều qua `addProduct/removeProduct` |

## Bổ sung so với class diagram

Các phần dưới đây không có trên sơ đồ, được thêm vì nhu cầu cài đặt; không thay đổi ý nghĩa của mô hình.

| Lớp | Bổ sung | Lý do |
| --- | --- | --- |
| Mọi thực thể | `id` (lớp gốc `Entity`), `Lazy<T>` cho quan hệ | Định danh lưu trữ và nạp trễ — chi tiết của tầng lưu trữ |
| `Customer.checkout` | Thêm tham số `shippingFee`, `note` | `Order` có hai thuộc tính này; phí lấy theo báo giá vận chuyển |
| `Product` | `imageUrl`, `updateInfo(...)`, `isOnSale`, `getTotalAvailable`, `findVariant`, `getSizes/getColors` | Hiển thị ảnh, sửa thông tin sản phẩm, giao diện chọn size/màu |
| `Product` | `joinPromotion/leavePromotion` | Giữ quan hệ hai chiều nhất quán khi `Promotion.addProduct/removeProduct` |
| `Category` | `rename`, `moveTo`, `isWithin`, `getFullName` | Quản trị danh mục, lọc theo cả cây danh mục con |
| `Voucher` | `create(...)`, `whyNotUsable(...)`, `activate/deactivate` | Kiểm tra dữ liệu khi tạo; báo cho khách lý do không dùng được mã; bật/tắt mã |
| `Promotion` | `create(...)`, `rename`, `includes` | Quản trị chương trình |
| `Order` | `canConfirm/canShip/...`, `isPaymentOverdue`, `holdsVoucherUsage`, `contains`, `isRefunded`, `findPayment` | Hiện đúng nút thao tác; tự huỷ đơn VNPAY quá hạn; đếm lượt voucher; kiểm tra đã mua |
| `Payment`, `Review` | `createdAt` (và `updatedAt` cho Review) | Hiển thị lịch sử |
| `AddressBook` | `findById`, `isDefault`, tối đa 10 địa chỉ | Thao tác theo bản ghi trên giao diện |
| `Cart` | `getItemCount`, `find`, tối đa 99 cái/dòng | Biểu tượng giỏ hàng, chặn số lượng bất thường |
| Các enum | `getLabel()` (nhãn tiếng Việt), `OrderStatus/PaymentStatus.getBadge()` | Hiển thị |
