package com.doodle.challenge.exception;

import java.util.UUID;

public class SlotNotFreeException extends DomainException {

    public SlotNotFreeException(UUID slotId) {
        super("Time slot " + slotId + " is not free");
    }
}
