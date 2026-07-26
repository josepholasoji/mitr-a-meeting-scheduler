package com.doodle.challenge.entity;

import com.doodle.challenge.exception.InvalidTimeRangeException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.Instant;
import java.util.Objects;

@Embeddable
public final class TimeRange {

    @Column(name = "start_time", nullable = false)
    private Instant start;

    @Column(name = "end_time", nullable = false)
    private Instant end;

    protected TimeRange() {
        // required by JPA
    }

    private TimeRange(Instant start, Instant end) {
        this.start = start;
        this.end = end;
    }

    public static TimeRange of(Instant start, Instant end) {
        if (start == null || end == null) {
            throw new InvalidTimeRangeException("Start and end time must both be provided");
        }
        if (!start.isBefore(end)) {
            throw new InvalidTimeRangeException("Start time must be before end time");
        }
        return new TimeRange(start, end);
    }

    // half-open: touching ranges (one ends exactly when the other starts) don't overlap
    public boolean overlaps(TimeRange other) {
        return this.start.isBefore(other.end) && other.start.isBefore(this.end);
    }

    public Instant start() {
        return start;
    }

    public Instant end() {
        return end;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TimeRange other)) {
            return false;
        }
        return Objects.equals(start, other.start) && Objects.equals(end, other.end);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end);
    }

    @Override
    public String toString() {
        return start + " - " + end;
    }
}
