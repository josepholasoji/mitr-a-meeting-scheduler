package com.doodle.challenge.exception;

import java.util.UUID;

public class TimeSlotNotFoundException extends DomainException {

    public TimeSlotNotFoundException(UUID slotId) {
        super("Time slot not found: " + slotId);
    }
}
