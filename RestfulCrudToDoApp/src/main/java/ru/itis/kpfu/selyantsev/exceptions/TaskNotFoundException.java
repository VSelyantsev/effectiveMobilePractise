package ru.itis.kpfu.selyantsev.exceptions;

import java.util.UUID;

public class TaskNotFoundException extends RuntimeException {
    public TaskNotFoundException(UUID taskId) {
        super(String.format("Task with this id: %s not found", taskId));
    }
}
