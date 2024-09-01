package ru.itis.kpfu.selyantsev.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ru.itis.kpfu.selyantsev.dto.request.TaskRequest;
import ru.itis.kpfu.selyantsev.dto.response.TaskResponse;
import ru.itis.kpfu.selyantsev.exceptions.NotBelongUserTask;
import ru.itis.kpfu.selyantsev.exceptions.TaskNotFoundException;
import ru.itis.kpfu.selyantsev.model.Task;
import ru.itis.kpfu.selyantsev.model.User;
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
        mappedEntity.setUser(User.builder().userId(userId).build());
        return Mono.fromCallable(
                () -> repository.save(mappedEntity).getTaskId()
        ).subscribeOn(Schedulers.boundedElastic());
    }

    @Cacheable(value = "tasks", key = "#taskId")
    @Override
    public Mono<TaskResponse> findTaskById(UUID taskId) {
        return Mono.fromCallable(
                () -> repository.findById(taskId)
                        .orElseThrow(() -> new TaskNotFoundException(taskId))
        ).subscribeOn(Schedulers.boundedElastic())
        .map(mapper::toResponse);
    }

    @Override
    public Flux<TaskResponse> findAll(int page, int pageSize) {
        Pageable pageable = PageRequest.of(page - 1, pageSize);

        return Flux.defer(() -> {
            Page<Task> taskPage = repository.findAll(pageable);
            return Flux.fromIterable(taskPage.getContent());
        }).map(mapper::toResponse);
    }

    @CachePut(value = "tasks", key = "#taskId")
    @Override
    public Mono<TaskResponse> updateTaskCompletionStatus(UUID userId, UUID taskId, boolean isComplete) {
        return Mono.fromCallable(
                () -> repository.findById(taskId)
                        .orElseThrow(() -> new TaskNotFoundException(taskId))
        ).flatMap(task -> {
            if (!task.getUser().getUserId().equals(userId)) {
                return Mono.error(new NotBelongUserTask(userId, taskId));
            }

            task.setComplete(isComplete);
            return Mono.just(mapper.toResponse(repository.save(task)));
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @CacheEvict(value = "taskByUser", key = "#taskId")
    @Override
    public Mono<Void> deleteTaskById(UUID taskId) {
        return Mono.fromRunnable(() -> repository.deleteById(taskId))
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }
}
