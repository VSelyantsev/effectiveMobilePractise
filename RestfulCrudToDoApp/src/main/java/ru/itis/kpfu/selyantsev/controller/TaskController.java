package ru.itis.kpfu.selyantsev.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.itis.kpfu.selyantsev.api.TaskApi;
import ru.itis.kpfu.selyantsev.dto.request.TaskRequest;
import ru.itis.kpfu.selyantsev.dto.response.TaskResponse;
import ru.itis.kpfu.selyantsev.service.TaskService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class TaskController implements TaskApi {

    private final TaskService taskService;

    @Override
    public Mono<UUID> create(UUID userId, TaskRequest taskRequest) {
        return taskService.create(userId, taskRequest);
    }

    @Override
    public Mono<TaskResponse> findTaskById(UUID taskId) {
        return taskService.findTaskById(taskId);
    }

    @Override
    public Flux<TaskResponse> findAllTasksByUserId(UUID userId) {
        return taskService.findAllTasksByUserId(userId);
    }

    @Override
    public Mono<TaskResponse> updateTaskCompletionStatus(UUID userId, UUID taskId, boolean isComplete) {
        return taskService.updateTaskCompletionStatus(userId, taskId, isComplete);
    }

    @Override
    public Mono<Void> deleteUsersTaskById(UUID userId) {
        return taskService.deleteUsersTaskById(userId);
    }
}
