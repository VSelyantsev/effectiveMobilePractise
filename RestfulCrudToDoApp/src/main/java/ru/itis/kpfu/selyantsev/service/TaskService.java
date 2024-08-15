package ru.itis.kpfu.selyantsev.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.itis.kpfu.selyantsev.dto.request.TaskRequest;
import ru.itis.kpfu.selyantsev.dto.response.TaskResponse;

import java.util.UUID;

public interface TaskService {
    Mono<UUID> create(UUID userId, TaskRequest taskRequest);
    Mono<TaskResponse> findTaskById(UUID taskId);
    Flux<TaskResponse> findAll(int page, int pageSize);
    Flux<TaskResponse> findAllTasksByUserId(UUID userId);
    Mono<TaskResponse> updateTaskCompletionStatus(UUID userId, UUID taskId, boolean isComplete);
    Mono<Void> deleteTaskById(UUID taskId);
}
