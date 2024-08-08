package ru.itis.kpfu.selyantsev.exceptions;

import java.util.UUID;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(UUID userId) {
        super(String.format("User with this id %s not found", userId));
    }
}
