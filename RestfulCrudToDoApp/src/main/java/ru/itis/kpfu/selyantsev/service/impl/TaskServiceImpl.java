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
import ru.itis.kpfu.selyantsev.exceptions.InvalidIdException;
import ru.itis.kpfu.selyantsev.exceptions.NotBelongUserTask;
import ru.itis.kpfu.selyantsev.exceptions.TaskNotFoundException;
import ru.itis.kpfu.selyantsev.model.Task;
import ru.itis.kpfu.selyantsev.model.User;
import ru.itis.kpfu.selyantsev.repository.CrudRepository;
import ru.itis.kpfu.selyantsev.service.TaskService;
import ru.itis.kpfu.selyantsev.utils.mappers.TaskMapper;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final CrudRepository<Task, UUID> repository;
    private final TaskMapper mapper;

    @Override
    public Mono<UUID> create(UUID userId, TaskRequest taskRequest) {
        Task mappedEntity = mapper.toEntity(taskRequest);
        mappedEntity.setUser(User.builder().userId(userId).build());
        return repository.create(mappedEntity)
                .flatMap(uuid -> {
                    if (uuid == null) {
                        return Mono.error(new FailedExecuteOperation(mappedEntity.getTaskId()));
                    }
                    return Mono.just(uuid);
                });
    }

    @Cacheable(value = "tasks", key = "#taskId")
    @Override
    public Mono<TaskResponse> findTaskById(UUID taskId) {
        return repository.findById(taskId)
                .switchIfEmpty(Mono.error(new InvalidIdException(taskId)))
                .flatMap(task -> {
                    if (task == null) {
                        return Mono.error(new TaskNotFoundException(taskId));
                    }
                    return Mono.just(task);
                })
                .map(mapper::toResponse);
    }

    @Override
    public Flux<TaskResponse> findAll(int page, int pageSize) {
        return repository.findAll(page, pageSize)
                .map(mapper::toResponse);
    }

    @CachePut(value = "tasks", key = "#taskId")
    @Override
    public Mono<TaskResponse> updateTaskCompletionStatus(UUID userId, UUID taskId, boolean isComplete) {
        return repository.findById(taskId)
                .switchIfEmpty(Mono.error(new InvalidIdException(taskId)))
                .flatMap(task -> {

                    if (task == null) {
                        return Mono.error(new TaskNotFoundException(taskId));
                    }

                    if (!task.getUser().getUserId().equals(userId)) {
                        return Mono.error(new NotBelongUserTask(userId, taskId));
                    }

                    task.setComplete(isComplete);

                    return repository.update(task)
                            .map(mapper::toResponse);
                });
    }

    @CacheEvict(value = "taskByUser", key = "#userId")
    @Override
    public Mono<Void> deleteTaskById(UUID taskId) {
        return repository.deleteById(taskId)
                .switchIfEmpty(Mono.error(new InvalidIdException(taskId)))
                .then();
    }
}
