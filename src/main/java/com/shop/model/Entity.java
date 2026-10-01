package com.shop.model;

/**
 * Gốc chung cho các đối tượng có định danh lưu trữ.
 * Định danh là chi tiết của tầng lưu trữ, không phải thuộc tính nghiệp vụ nên không có trên sơ đồ lớp.
 */
public abstract class Entity {

    private Long id;

    protected Entity(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    /** Tầng lưu trữ gán định danh sau khi chèn bản ghi mới. Chỉ gán được một lần. */
    public void assignId(long id) {
        if (this.id != null) {
            throw new IllegalStateException("Id already assigned: " + this.id);
        }
        this.id = id;
    }

    public boolean isNew() {
        return id == null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Long otherId = ((Entity) o).id;
        return id != null && id.equals(otherId);
    }

    @Override
    public int hashCode() {
        return id != null ? Long.hashCode(id) : System.identityHashCode(this);
    }
}
