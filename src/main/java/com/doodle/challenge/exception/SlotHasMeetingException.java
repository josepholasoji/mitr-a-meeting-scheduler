package com.doodle.challenge.exception;

import java.util.UUID;

public class SlotHasMeetingException extends DomainException {

    public SlotHasMeetingException(UUID slotId) {
        super("Time slot " + slotId + " cannot be deleted while it has a meeting");
    }
}
