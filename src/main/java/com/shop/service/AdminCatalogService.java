package com.shop.service;

import com.shop.dao.Daos;
import com.shop.model.DomainException;
import com.shop.model.catalog.Category;
import com.shop.model.catalog.Product;
import com.shop.model.catalog.ProductVariant;
import com.shop.persistence.Tx;
import java.math.BigDecimal;
import java.util.List;
import java.util.function.Consumer;

/** Quản trị danh mục và sản phẩm (quyền ADMIN). Mọi thay đổi đi qua hành vi của Category/Product/ProductVariant. */
public class AdminCatalogService {

    // ------------------------------------------------------------------ danh mục

    public List<Category> categories() {
        return Daos.categories().findAll();
    }

    public Category createCategory(String name, Long parentId) {
        return Tx.inTransaction(() -> {
            Category category = Category.create(name, parentId == null ? null : category(parentId));
            Daos.categories().save(category);
            return category;
        });
    }

    public void updateCategory(long id, String name, Long parentId) {
        Tx.inTransaction(() -> {
            Category category = category(id);
            category.rename(name);
            category.moveTo(parentId == null ? null : category(parentId));
            Daos.categories().save(category);
        });
    }

    public void deleteCategory(long id) {
        Tx.inTransaction(() -> {
            category(id);
            if (Daos.categories().isInUse(id)) {
                throw new DomainException("Chỉ xoá được danh mục chưa có sản phẩm và danh mục con");
            }
            Daos.categories().delete(id);
        });
    }

    private static Category category(long id) {
        return Daos.categories().findById(id).orElseThrow(() -> new DomainException("Danh mục không tồn tại"));
    }

    // ------------------------------------------------------------------ sản phẩm

    public List<Product> products(String keyword) {
        return Daos.products().findAllForAdmin(keyword);
    }

    public Product product(long id) {
        return Daos.products().findById(id).orElseThrow(() -> new DomainException("Sản phẩm không tồn tại"));
    }

    public Product createProduct(String name, String description, BigDecimal price, Long categoryId, String imageUrl) {
        return Tx.inTransaction(() -> {
            Category category = categoryId == null ? null : category(categoryId);
            Product product = Product.create(name, description, price, category,
                    imageUrl == null ? "images/cloth_1.jpg" : imageUrl);
            Daos.products().insert(product);
            return product;
        });
    }

    public void updateInfo(long id, String name, String description, Long categoryId, String newImageUrl) {
        change(id, p -> p.updateInfo(name, description, categoryId == null ? null : category(categoryId), newImageUrl));
    }

    public void changePrice(long id, BigDecimal newPrice) {
        change(id, p -> p.changePrice(newPrice));
    }

    public void discontinue(long id) {
        change(id, Product::discontinue);
    }

    public ProductVariant addVariant(long productId, String size, String color, int qty) {
        return Tx.inTransaction(() -> {
            Product product = product(productId);
            ProductVariant variant = product.addVariant(size, color, qty);
            Daos.products().saveVariants(product);
            return variant;
        });
    }

    /** Nhập thêm hàng cho một biến thể. */
    public void restock(long productId, long variantId, int qty) {
        Tx.inTransaction(() -> {
            Product product = product(productId);
            ProductVariant variant = product.findVariant(variantId)
                    .orElseThrow(() -> new DomainException("Biến thể không tồn tại"));
            variant.restock(qty);
            Daos.products().saveVariant(variant);
        });
    }

    private void change(long id, Consumer<Product> action) {
        Tx.inTransaction(() -> {
            Product product = product(id);
            action.accept(product);
            Daos.products().update(product);
        });
    }
}
