package com.doodle.challenge.dto;

import com.doodle.challenge.entity.TimeRange;
import com.doodle.challenge.entity.TimeSlot;
import java.util.List;

public record AvailabilitySummary(TimeRange window, List<TimeSlot> freeSlots, List<TimeSlot> busySlots) {
}
