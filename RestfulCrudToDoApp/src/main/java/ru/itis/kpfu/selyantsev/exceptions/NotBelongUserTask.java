package ru.itis.kpfu.selyantsev.exceptions;

import java.util.UUID;

public class NotBelongUserTask extends RuntimeException {
    public NotBelongUserTask(UUID userId, UUID taskId) {
        super(String.format("Current task: %s does not belong to user: %s", taskId, userId));
    }
}
