package com.doodle.challenge.mapper;

import com.doodle.challenge.dto.TimeSlotResponse;
import com.doodle.challenge.entity.TimeSlot;
import org.springframework.stereotype.Component;

@Component
public class TimeSlotMapper {

    public TimeSlotResponse toResponse(TimeSlot slot) {
        return new TimeSlotResponse(
                slot.getId(),
                slot.getUserId(),
                slot.getRange().start(),
                slot.getRange().end(),
                slot.getStatus(),
                slot.getCreatedAt(),
                slot.getUpdatedAt());
    }
}
