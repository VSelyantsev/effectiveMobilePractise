package ru.itis.kpfu.selyantsev.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.itis.kpfu.selyantsev.dto.request.TaskRequest;
import ru.itis.kpfu.selyantsev.dto.response.TaskResponse;
import ru.itis.kpfu.selyantsev.exceptions.FailedExecuteOperation;
import ru.itis.kpfu.selyantsev.exceptions.NotBelongUserTask;
import ru.itis.kpfu.selyantsev.exceptions.TaskNotFoundException;
import ru.itis.kpfu.selyantsev.model.Task;
import ru.itis.kpfu.selyantsev.repository.TaskRepository;
import ru.itis.kpfu.selyantsev.service.TaskService;
import ru.itis.kpfu.selyantsev.utils.mappers.TaskMapper;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository repository;
    private final TaskMapper mapper;

    @Override
    public Mono<UUID> create(UUID userId, TaskRequest taskRequest) {
        Task mappedEntity = mapper.toEntity(taskRequest);
        mappedEntity.setUserId(userId);
        return repository.create(mappedEntity)
                .flatMap(rowsInserted -> {
                    if (rowsInserted == 0) {
                        return Mono.error(new FailedExecuteOperation(mappedEntity.getTaskId()));
                    }
                    return Mono.just(mappedEntity.getTaskId());
                });
    }

    @Cacheable(value = "tasks", key = "#taskId")
    @Override
    public Mono<TaskResponse> findTaskById(UUID taskId) {
        return repository.findById(taskId)
                .switchIfEmpty(Mono.error(new TaskNotFoundException(taskId)))
                .map(mapper::toResponse);
    }

    @Override
    public Flux<TaskResponse> findAll(int page, int pageSize) {
        return repository.findAll(page, pageSize)
                .map(mapper::toResponse);
    }

    @Cacheable(value = "taskByUser", key = "#userId")
    @Override
    public Flux<TaskResponse> findAllTasksByUserId(UUID userId) {
        return repository.findAllTasksByUserId(userId)
                .map(mapper::toResponse);
    }

    @CachePut(value = "tasks", key = "#taskId")
    @Override
    public Mono<TaskResponse> updateTaskCompletionStatus(UUID userId, UUID taskId, boolean isComplete) {
        return repository.findById(taskId)
                .switchIfEmpty(Mono.error(new TaskNotFoundException(taskId)))
                .flatMap(task -> {
                    if (!task.getUserId().equals(userId)) {
                        return Mono.error(new NotBelongUserTask(userId, taskId));
                    }

                    task.setComplete(isComplete);

                    return repository.update(task)
                            .flatMap(rowsUpdated -> {
                                if (rowsUpdated == 0) {
                                    return Mono.error(new FailedExecuteOperation(task.getTaskId()));
                                }

                                return Mono.just(mapper.toResponse(task));
                            });
                });
    }

    @CacheEvict(value = "taskByUser", key = "#userId")
    @Override
    public Mono<Void> deleteTaskById(UUID taskId) {
        return repository.deleteById(taskId)
                .switchIfEmpty(Mono.error(new TaskNotFoundException(taskId)))
                .then();
    }
}
