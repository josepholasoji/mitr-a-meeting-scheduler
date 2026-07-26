package com.doodle.challenge.exception;

import java.util.UUID;

public class MeetingNotFoundException extends DomainException {

    public MeetingNotFoundException(UUID meetingId) {
        super("Meeting not found: " + meetingId);
    }
}
