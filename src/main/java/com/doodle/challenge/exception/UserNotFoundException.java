package com.metr.challenge.exception;

import java.util.UUID;

public class UserNotFoundException extends DomainException {

    public UserNotFoundException(UUID userId) {
        super("User not found: " + userId);
    }
}
