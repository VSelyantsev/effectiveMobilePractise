package ru.itis.kpfu.selyantsev.exceptions;

import java.util.UUID;

public class FailedExecuteOperation extends RuntimeException {
    public FailedExecuteOperation(UUID entityUUID) {
        super(String.format("Failed executing operation: %s", entityUUID));
    }
}
