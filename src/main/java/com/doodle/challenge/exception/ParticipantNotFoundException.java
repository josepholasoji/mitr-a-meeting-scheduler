package com.doodle.challenge.exception;

import java.util.UUID;

public class ParticipantNotFoundException extends DomainException {

    public ParticipantNotFoundException(UUID meetingId, UUID userId) {
        super("User " + userId + " is not a participant of meeting " + meetingId);
    }
}
