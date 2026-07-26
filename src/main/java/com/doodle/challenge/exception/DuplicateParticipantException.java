package com.doodle.challenge.exception;

import java.util.UUID;

public class DuplicateParticipantException extends DomainException {

    public DuplicateParticipantException(UUID meetingId, UUID userId) {
        super("User " + userId + " is already a participant of meeting " + meetingId);
    }
}
