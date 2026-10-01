package com.shop.model.catalog;

import com.shop.model.Check;
import com.shop.model.DomainException;
import com.shop.model.Entity;

/**
 * Danh mục hàng, có thể nằm trong một danh mục cha (0..1). Danh mục không giữ danh sách sản phẩm;
 * sản phẩm biết danh mục của mình.
 */
public class Category extends Entity {

    private String name;
    private Category parent;

    public Category(Long id, String name, Category parent) {
        super(id);
        this.name = name;
        this.parent = parent;
    }

    public static Category create(String name, Category parent) {
        return new Category(null, Check.text(name, "Tên danh mục", 100), parent);
    }

    public void rename(String newName) {
        this.name = Check.text(newName, "Tên danh mục", 100);
    }

    public void moveTo(Category newParent) {
        for (Category c = newParent; c != null; c = c.parent) {
            if (c == this || (getId() != null && getId().equals(c.getId()))) {
                throw new DomainException("Không thể đặt danh mục làm con của chính nó");
            }
        }
        this.parent = newParent;
    }

    /** Đúng nếu danh mục này là {@code other} hoặc nằm bên trong {@code other}. */
    public boolean isWithin(Category other) {
        for (Category c = this; c != null; c = c.parent) {
            if (c.equals(other)) {
                return true;
            }
        }
        return false;
    }

    /** Tên đầy đủ theo cây, ví dụ "Áo › Áo thun". */
    public String getFullName() {
        return parent == null ? name : parent.getFullName() + " › " + name;
    }

    public String getName() {
        return name;
    }

    public Category getParent() {
        return parent;
    }
}
