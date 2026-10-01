-- ===========================================================================
-- Shop quần áo E-commerce — lược đồ CSDL (tương thích MySQL 8 và H2 chế độ MySQL)
-- Ánh xạ từ class diagram v6. Các giá trị tính được (tổng tiền, hạng khách,
-- điểm trung bình, số tiền giảm) KHÔNG lưu thành cột.
-- ===========================================================================

-- Package Tài khoản ---------------------------------------------------------
-- User trừu tượng + Customer/Employee: kế thừa một bảng, phân biệt bằng user_type
CREATE TABLE IF NOT EXISTS users (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_type       VARCHAR(10)  NOT NULL,
    username        VARCHAR(30)  NOT NULL UNIQUE,
    password_hash   VARCHAR(200) NOT NULL,
    full_name       VARCHAR(100) NOT NULL,
    gender          VARCHAR(10),
    dob             DATE,
    email           VARCHAR(100) NOT NULL UNIQUE,
    phone           VARCHAR(15),
    active          BOOLEAN      NOT NULL DEFAULT TRUE,
    hire_date       DATE,
    role            VARCHAR(10),
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CHECK (user_type IN ('CUSTOMER', 'EMPLOYEE'))
);

-- Address: value object; mỗi dòng là một địa chỉ đã lưu trong sổ của khách
CREATE TABLE IF NOT EXISTS addresses (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id     BIGINT       NOT NULL,
    recipient_name  VARCHAR(100) NOT NULL,
    phone           VARCHAR(15)  NOT NULL,
    street          VARCHAR(255) NOT NULL,
    ward            VARCHAR(100) NOT NULL,
    province        VARCHAR(100) NOT NULL,
    FOREIGN KEY (customer_id) REFERENCES users (id) ON DELETE CASCADE
);

-- AddressBook: liên kết «mặc định» là một quan hệ riêng, không phải cờ trên địa chỉ
CREATE TABLE IF NOT EXISTS address_books (
    customer_id         BIGINT PRIMARY KEY,
    default_address_id  BIGINT NULL,
    FOREIGN KEY (customer_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (default_address_id) REFERENCES addresses (id) ON DELETE SET NULL
);

-- Package Sản phẩm & mua sắm ------------------------------------------------
CREATE TABLE IF NOT EXISTS categories (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    parent_id   BIGINT NULL,
    FOREIGN KEY (parent_id) REFERENCES categories (id)
);

CREATE TABLE IF NOT EXISTS products (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_id  BIGINT        NOT NULL,
    name         VARCHAR(150)  NOT NULL,
    description  VARCHAR(2000),
    base_price   DECIMAL(15,2) NOT NULL,
    active       BOOLEAN       NOT NULL DEFAULT TRUE,
    image_url    VARCHAR(255),
    created_at   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories (id),
    CHECK (base_price > 0)
);

-- ProductVariant (SKU): 0 <= reserved_quantity <= stock_quantity
CREATE TABLE IF NOT EXISTS product_variants (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id         BIGINT      NOT NULL,
    size               VARCHAR(10) NOT NULL,
    color              VARCHAR(30) NOT NULL,
    stock_quantity     INT         NOT NULL DEFAULT 0,
    reserved_quantity  INT         NOT NULL DEFAULT 0,
    FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE,
    UNIQUE (product_id, size, color),
    CHECK (reserved_quantity >= 0 AND reserved_quantity <= stock_quantity)
);

-- Cart ◆ CartItem: giỏ gắn với khách (1–1) nên định danh bằng customer_id
CREATE TABLE IF NOT EXISTS cart_items (
    customer_id  BIGINT   NOT NULL,
    variant_id   BIGINT   NOT NULL,
    quantity     INT      NOT NULL,
    added_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (customer_id, variant_id),
    FOREIGN KEY (customer_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (variant_id) REFERENCES product_variants (id) ON DELETE CASCADE,
    CHECK (quantity > 0)
);

-- Customer → Product «yêu thích» (nhiều – nhiều)
CREATE TABLE IF NOT EXISTS wishlists (
    customer_id  BIGINT   NOT NULL,
    product_id   BIGINT   NOT NULL,
    added_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (customer_id, product_id),
    FOREIGN KEY (customer_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE
);

-- Product ◆ Review, Review → Customer «viết bởi»; mỗi khách tối đa 1 đánh giá / sản phẩm
CREATE TABLE IF NOT EXISTS reviews (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id   BIGINT   NOT NULL,
    customer_id  BIGINT   NOT NULL,
    rating       INT      NOT NULL,
    comment      VARCHAR(1000),
    created_at   DATETIME NOT NULL,
    updated_at   DATETIME,
    UNIQUE (product_id, customer_id),
    FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE,
    FOREIGN KEY (customer_id) REFERENCES users (id) ON DELETE CASCADE,
    CHECK (rating BETWEEN 1 AND 5)
);

-- Package Giảm giá ----------------------------------------------------------
-- DiscountPolicy & Period là composition 1–1 nên nhúng thành cột:
--   policy_type = PERCENT (policy_value = %, policy_max_discount = trần) | FIXED (policy_value = số tiền)
CREATE TABLE IF NOT EXISTS vouchers (
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    code                 VARCHAR(30)   NOT NULL UNIQUE,
    applicable_tiers     VARCHAR(100)  NOT NULL,
    min_order_value      DECIMAL(15,2) NOT NULL DEFAULT 0,
    usage_limit          INT           NOT NULL,
    per_customer_limit   INT           NOT NULL,
    used_count           INT           NOT NULL DEFAULT 0,
    active               BOOLEAN       NOT NULL DEFAULT TRUE,
    policy_type          VARCHAR(10)   NOT NULL,
    policy_value         DECIMAL(15,2) NOT NULL,
    policy_max_discount  DECIMAL(15,2),
    period_start         DATETIME      NOT NULL,
    period_end           DATETIME      NOT NULL,
    CHECK (used_count >= 0 AND used_count <= usage_limit),
    CHECK (policy_type IN ('PERCENT', 'FIXED'))
);

CREATE TABLE IF NOT EXISTS promotions (
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    name                 VARCHAR(150)  NOT NULL,
    policy_type          VARCHAR(10)   NOT NULL,
    policy_value         DECIMAL(15,2) NOT NULL,
    policy_max_discount  DECIMAL(15,2),
    period_start         DATETIME      NOT NULL,
    period_end           DATETIME      NOT NULL,
    CHECK (policy_type IN ('PERCENT', 'FIXED'))
);

-- Product – Promotion (nhiều – nhiều, hai chiều)
CREATE TABLE IF NOT EXISTS promotion_products (
    promotion_id  BIGINT NOT NULL,
    product_id    BIGINT NOT NULL,
    PRIMARY KEY (promotion_id, product_id),
    FOREIGN KEY (promotion_id) REFERENCES promotions (id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE
);

-- Package Đơn hàng & thanh toán ---------------------------------------------
-- Order ◆ Address «giao đến»: bản địa chỉ riêng của đơn được nhúng thành các cột ship_*
CREATE TABLE IF NOT EXISTS orders (
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id          BIGINT        NOT NULL,
    order_date           DATETIME      NOT NULL,
    note                 VARCHAR(500),
    payment_method       VARCHAR(10)   NOT NULL,
    shipping_fee         DECIMAL(15,2) NOT NULL,
    status               VARCHAR(20)   NOT NULL,
    voucher_id           BIGINT,
    handled_by           BIGINT,
    ship_recipient_name  VARCHAR(100)  NOT NULL,
    ship_phone           VARCHAR(15)   NOT NULL,
    ship_street          VARCHAR(255)  NOT NULL,
    ship_ward            VARCHAR(100)  NOT NULL,
    ship_province        VARCHAR(100)  NOT NULL,
    updated_at           DATETIME,
    FOREIGN KEY (customer_id) REFERENCES users (id),
    FOREIGN KEY (voucher_id) REFERENCES vouchers (id),
    FOREIGN KEY (handled_by) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS order_details (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id    BIGINT        NOT NULL,
    variant_id  BIGINT        NOT NULL,
    unit_price  DECIMAL(15,2) NOT NULL,
    quantity    INT           NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE,
    FOREIGN KEY (variant_id) REFERENCES product_variants (id),
    CHECK (quantity > 0)
);

-- Order → Payment (association): thanh toán được giữ lại kể cả khi đơn bị huỷ
CREATE TABLE IF NOT EXISTS payments (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id        BIGINT        NOT NULL,
    amount          DECIMAL(15,2) NOT NULL,
    status          VARCHAR(10)   NOT NULL,
    paid_at         DATETIME,
    transaction_no  VARCHAR(100),
    created_at      DATETIME      NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders (id)
);
