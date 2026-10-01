package com.shop.model.catalog;

import com.shop.model.Check;
import com.shop.model.DomainException;
import com.shop.model.Entity;
import com.shop.model.Lazy;
import com.shop.model.account.Customer;
import com.shop.model.discount.Promotion;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

/**
 * Mẫu sản phẩm: tên, mô tả, giá gốc giống nhau cho mọi size/màu. Sản phẩm chứa các biến thể và đánh giá
 * (composition), thuộc một danh mục và biết các chương trình khuyến mãi của mình để tính giá.
 */
public class Product extends Entity {

    private String name;
    private String description;
    private BigDecimal basePrice;
    private boolean active;
    private Category category;
    /** Ảnh đại diện — thuộc tính trình bày, phục vụ giao diện. */
    private String imageUrl;

    private final Lazy<List<ProductVariant>> variants;
    private final Lazy<List<Review>> reviews;
    private final Lazy<List<Promotion>> promotions;

    public Product(Long id, String name, String description, BigDecimal basePrice, boolean active,
                   Category category, String imageUrl,
                   Lazy<List<ProductVariant>> variants, Lazy<List<Review>> reviews,
                   Lazy<List<Promotion>> promotions) {
        super(id);
        this.name = name;
        this.description = description;
        this.basePrice = basePrice;
        this.active = active;
        this.category = category;
        this.imageUrl = imageUrl;
        this.variants = variants;
        this.reviews = reviews;
        this.promotions = promotions;
    }

    public static Product create(String name, String description, BigDecimal basePrice,
                                 Category category, String imageUrl) {
        if (category == null) {
            throw new DomainException("Vui lòng chọn danh mục");
        }
        return new Product(null, Check.text(name, "Tên sản phẩm", 150),
                Check.optionalText(description, "Mô tả", 2000),
                Check.positive(basePrice, "Giá gốc"), true, category, imageUrl,
                Lazy.of(new ArrayList<>()), Lazy.of(new ArrayList<>()), Lazy.of(new ArrayList<>()));
    }

    // ---------------------------------------------------------------- quản lý mẫu

    /** Tạo biến thể mới khi cửa hàng nhập thêm size hoặc màu. Chỉ sản phẩm mới tạo ra biến thể của nó. */
    public ProductVariant addVariant(String size, String color, int qty) {
        String s = Check.text(size, "Size", 10).toUpperCase();
        String c = Check.text(color, "Màu", 30);
        if (qty < 0) {
            throw new DomainException("Số lượng tồn kho không được âm");
        }
        if (findVariant(s, c).isPresent()) {
            throw new DomainException("Biến thể " + c + " – " + s + " đã tồn tại");
        }
        ProductVariant variant = new ProductVariant(null, this, s, c, qty, 0);
        variants.get().add(variant);
        return variant;
    }

    public void changePrice(BigDecimal newPrice) {
        this.basePrice = Check.positive(newPrice, "Giá gốc");
    }

    /** Ngừng bán. Sản phẩm thật không bị xoá; các đơn cũ vẫn nhắc đến nó. */
    public void discontinue() {
        active = false;
    }

    /** Cập nhật thông tin mô tả (tên, mô tả, danh mục, ảnh) — dành cho quản trị viên. */
    public void updateInfo(String name, String description, Category category, String imageUrl) {
        if (category == null) {
            throw new DomainException("Vui lòng chọn danh mục");
        }
        this.name = Check.text(name, "Tên sản phẩm", 150);
        this.description = Check.optionalText(description, "Mô tả", 2000);
        this.category = category;
        if (imageUrl != null) {
            this.imageUrl = imageUrl;
        }
    }

    // ---------------------------------------------------------------- đánh giá

    public void addReview(Review review) {
        if (hasReviewBy(review.getAuthor())) {
            throw new DomainException("Bạn đã đánh giá sản phẩm này — hãy sửa đánh giá cũ");
        }
        reviews.get().add(review);
    }

    public boolean hasReviewBy(Customer customer) {
        return findReviewBy(customer).isPresent();
    }

    public Optional<Review> findReviewBy(Customer customer) {
        return reviews.get().stream().filter(r -> r.getAuthor().equals(customer)).findFirst();
    }

    public double getAverageRating() {
        return reviews.get().stream().mapToInt(Review::getRating).average().orElse(0);
    }

    // ---------------------------------------------------------------- giá & kho

    /** Giá bán cuối tại một thời điểm: lấy mức giảm cao nhất trong các chương trình đang chạy. */
    public BigDecimal getFinalPrice(LocalDateTime now) {
        BigDecimal best = basePrice;
        for (Promotion promotion : promotions.get()) {
            if (promotion.isOngoing(now)) {
                best = best.min(promotion.apply(basePrice));
            }
        }
        return best;
    }

    public boolean isOnSale(LocalDateTime now) {
        return getFinalPrice(now).compareTo(basePrice) < 0;
    }

    public int getTotalStock() {
        return variants.get().stream().mapToInt(ProductVariant::getStockQuantity).sum();
    }

    public int getTotalAvailable() {
        return variants.get().stream().mapToInt(ProductVariant::getAvailableQuantity).sum();
    }

    public Optional<ProductVariant> findVariant(String size, String color) {
        return variants.get().stream().filter(v -> v.matches(size, color)).findFirst();
    }

    public Optional<ProductVariant> findVariant(long variantId) {
        return variants.get().stream()
                .filter(v -> v.getId() != null && v.getId() == variantId).findFirst();
    }

    public List<String> getSizes() {
        return new ArrayList<>(variants.get().stream().map(ProductVariant::getSize)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new)));
    }

    public List<String> getColors() {
        return new ArrayList<>(variants.get().stream().map(ProductVariant::getColor)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new)));
    }

    // ---------------------------------------------------------------- khuyến mãi (hai chiều)

    /** Do {@link Promotion#addProduct} gọi để giữ quan hệ hai chiều nhất quán. */
    public void joinPromotion(Promotion promotion) {
        if (!promotions.get().contains(promotion)) {
            promotions.get().add(promotion);
        }
    }

    /** Do {@link Promotion#removeProduct} gọi. */
    public void leavePromotion(Promotion promotion) {
        promotions.get().remove(promotion);
    }

    // ---------------------------------------------------------------- getters

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public boolean isActive() {
        return active;
    }

    public Category getCategory() {
        return category;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public List<ProductVariant> getVariants() {
        return Collections.unmodifiableList(variants.get());
    }

    public List<Review> getReviews() {
        return Collections.unmodifiableList(reviews.get());
    }

    public List<Promotion> getPromotions() {
        return Collections.unmodifiableList(promotions.get());
    }
}
