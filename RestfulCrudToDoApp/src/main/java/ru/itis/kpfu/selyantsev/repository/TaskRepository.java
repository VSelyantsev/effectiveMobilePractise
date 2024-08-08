package ru.itis.kpfu.selyantsev.repository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.itis.kpfu.selyantsev.model.Task;

import java.util.UUID;

public interface TaskRepository {
    Mono<UUID> create(UUID userId, Task task);
    Mono<Task> findTaskById(UUID taskId);
    Flux<Task> findAllTasksByUserId(UUID userId);
    Mono<Task> updateTaskCompletionStatus(UUID userId, UUID taskId, boolean isComplete);
    Mono<Void> deleteUsersTaskById(UUID userId);
}
