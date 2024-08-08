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
        Task entity = mapper.toEntity(taskRequest);
        return repository.create(userId, entity);
    }

    @Cacheable(value = "tasks", key = "#taskId")
    @Override
    public Mono<TaskResponse> findTaskById(UUID taskId) {
        return repository.findTaskById(taskId)
                .switchIfEmpty(Mono.error(new TaskNotFoundException(taskId)))
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
        return repository.updateTaskCompletionStatus(userId, taskId, isComplete)
                .map(mapper::toResponse);
    }

    @CacheEvict(value = "taskByUser", key = "#userId")
    @Override
    public Mono<Void> deleteUsersTaskById(UUID userId) {
        return repository.deleteUsersTaskById(userId);
    }
}
