package com.doodle.challenge.exception;

import java.util.UUID;

// takes a pre-formatted range description, not TimeRange itself, so exception has no dependency on entity
public class SlotOverlapException extends DomainException {

    public SlotOverlapException(UUID userId, String rangeDescription) {
        super("Time slot " + rangeDescription + " overlaps an existing slot for user " + userId);
    }
}
