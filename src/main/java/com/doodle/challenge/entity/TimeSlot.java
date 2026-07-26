package com.doodle.challenge.entity;

import com.doodle.challenge.event.*;
import com.doodle.challenge.exception.SlotHasMeetingException;
import com.doodle.challenge.exception.SlotNotFreeException;
import com.doodle.challenge.exception.TimeSlotNotFoundException;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "time_slots")
public class TimeSlot extends BaseEntity<TimeSlot> {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Embedded
    private TimeRange range;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TimeSlotStatus status;

    @Column(name = "deleted_on")
    private Instant deletedOn;

    protected TimeSlot() {
        // required by JPA
    }

    private TimeSlot(UUID id, UUID userId, TimeRange range) {
        super(id);
        this.userId = userId;
        this.range = range;
        this.status = TimeSlotStatus.FREE;
    }

    public static TimeSlot create(UUID userId, TimeRange range) {
        TimeSlot slot = new TimeSlot(UUID.randomUUID(), userId, range);
        slot.registerEvent(new TimeSlotCreated(slot.getId(), userId));
        return slot;
    }

    public void reschedule(TimeRange newRange) {
        ensureNotDeleted();
        ensureFree();
        this.range = newRange;
        touch();
        registerEvent(new TimeSlotUpdated(getId(), userId));
    }

    // rejected while BUSY - cancel the meeting first
    public void delete() {
        ensureNotDeleted();
        if (status == TimeSlotStatus.BUSY) {
            throw new SlotHasMeetingException(getId());
        }
        this.deletedOn = Instant.now();
        touch();
        registerEvent(new TimeSlotDeleted(getId(), userId));
    }

    public void markBusy() {
        ensureNotDeleted();
        ensureFree();
        this.status = TimeSlotStatus.BUSY;
        touch();
        registerEvent(new TimeSlotMarkedBusy(getId(), userId));
    }

    public void markFree() {
        ensureNotDeleted();
        this.status = TimeSlotStatus.FREE;
        touch();
        registerEvent(new TimeSlotMarkedFree(getId(), userId));
    }

    public boolean isFree() {
        return status == TimeSlotStatus.FREE;
    }

    public boolean isDeleted() {
        return deletedOn != null;
    }

    public boolean overlaps(TimeRange other) {
        return range.overlaps(other);
    }

    private void ensureFree() {
        if (status != TimeSlotStatus.FREE) {
            throw new SlotNotFreeException(getId());
        }
    }

    private void ensureNotDeleted() {
        if (isDeleted()) {
            throw new TimeSlotNotFoundException(getId());
        }
    }

    public UUID getUserId() {
        return userId;
    }

    public TimeRange getRange() {
        return range;
    }

    public TimeSlotStatus getStatus() {
        return status;
    }

    public Instant getDeletedOn() {
        return deletedOn;
    }
}
