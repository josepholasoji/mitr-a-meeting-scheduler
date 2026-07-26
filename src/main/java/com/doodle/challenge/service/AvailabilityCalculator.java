package com.doodle.challenge.service;

import com.doodle.challenge.dto.AvailabilitySummary;
import com.doodle.challenge.entity.TimeRange;
import com.doodle.challenge.entity.TimeSlot;
import com.doodle.challenge.entity.TimeSlotStatus;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class AvailabilityCalculator {

    public AvailabilitySummary calculate(List<TimeSlot> slots, TimeRange window, TimeSlotStatus statusFilter) {
        List<TimeSlot> inWindow = slots.stream()
                .filter(slot -> !slot.isDeleted())
                .filter(slot -> slot.overlaps(window))
                .filter(slot -> statusFilter == null || slot.getStatus() == statusFilter)
                .sorted(Comparator.comparing(slot -> slot.getRange().start()))
                .toList();

        List<TimeSlot> free = inWindow.stream().filter(TimeSlot::isFree).toList();
        List<TimeSlot> busy = inWindow.stream()
                .filter(slot -> slot.getStatus() == TimeSlotStatus.BUSY)
                .toList();

        return new AvailabilitySummary(window, free, busy);
    }
}
