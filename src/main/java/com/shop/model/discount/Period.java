package com.shop.model.discount;

import com.shop.model.DomainException;
import java.time.LocalDateTime;
import java.util.Objects;

/** Thời gian hiệu lực — value object bất biến gồm thời điểm bắt đầu và kết thúc. */
public final class Period {

    private final LocalDateTime start;
    private final LocalDateTime end;

    public Period(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            throw new DomainException("Thời gian bắt đầu và kết thúc không được để trống");
        }
        if (!end.isAfter(start)) {
            throw new DomainException("Thời gian kết thúc phải sau thời gian bắt đầu");
        }
        this.start = start;
        this.end = end;
    }

    public boolean contains(LocalDateTime time) {
        return !time.isBefore(start) && !time.isAfter(end);
    }

    public boolean isUpcoming(LocalDateTime now) {
        return now.isBefore(start);
    }

    public boolean isOver(LocalDateTime now) {
        return now.isAfter(end);
    }

    public LocalDateTime getStart() {
        return start;
    }

    public LocalDateTime getEnd() {
        return end;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Period p)) return false;
        return start.equals(p.start) && end.equals(p.end);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end);
    }
}
