package com.doodle.challenge.validator;

import com.doodle.challenge.entity.TimeRange;
import com.doodle.challenge.entity.TimeSlot;
import com.doodle.challenge.exception.SlotOverlapException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

// persistence-ignorant: takes candidate slots from the caller rather than querying, so it's easy to unit test
@Component
public class SlotOverlapValidator {

    public void validate(UUID userId, TimeRange candidate, List<TimeSlot> existingSlots) {
        boolean overlapsExisting = existingSlots.stream()
                .filter(slot -> !slot.isDeleted())
                .anyMatch(slot -> slot.overlaps(candidate));
        if (overlapsExisting) {
            throw new SlotOverlapException(userId, candidate.toString());
        }
    }
}
