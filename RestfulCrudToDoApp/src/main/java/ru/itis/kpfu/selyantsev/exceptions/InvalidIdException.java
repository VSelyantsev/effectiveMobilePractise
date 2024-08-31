package ru.itis.kpfu.selyantsev.exceptions;

import java.util.UUID;

public class InvalidIdException extends RuntimeException {
    public InvalidIdException(UUID invalidUUID) {
        super(String.format("Invalid userId provided: %s", invalidUUID));
    }
}
