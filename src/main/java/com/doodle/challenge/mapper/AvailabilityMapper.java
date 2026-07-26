package com.doodle.challenge.mapper;

import com.doodle.challenge.dto.AvailabilityResponse;
import com.doodle.challenge.dto.AvailabilitySummary;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AvailabilityMapper {

    private final TimeSlotMapper timeSlotMapper;

    public AvailabilityMapper(TimeSlotMapper timeSlotMapper) {
        this.timeSlotMapper = timeSlotMapper;
    }

    public AvailabilityResponse toResponse(UUID userId, AvailabilitySummary summary) {
        return new AvailabilityResponse(
                userId,
                summary.window().start(),
                summary.window().end(),
                summary.freeSlots().stream().map(timeSlotMapper::toResponse).toList(),
                summary.busySlots().stream().map(timeSlotMapper::toResponse).toList());
    }
}
