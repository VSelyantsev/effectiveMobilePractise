package ru.itis.kpfu.selyantsev.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.itis.kpfu.selyantsev.exceptions.DatabaseException;
import ru.itis.kpfu.selyantsev.model.Task;
import ru.itis.kpfu.selyantsev.repository.TaskRepository;
import ru.itis.kpfu.selyantsev.utils.rowMapper.TaskRowMapper;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static reactor.core.scheduler.Schedulers.fromExecutor;

@Repository
@RequiredArgsConstructor
@SuppressWarnings("BlockingMethodInNonBlockingContext")
public class TaskRepositoryImpl implements TaskRepository {

    private final JdbcTemplate template;

    private static final String CREATE_TASK = "INSERT INTO t_task (id, task_name, is_complete, user_id) VALUES (?, ?, ?, ?)";
    private static final String FIND_TASK_BY_UUID = "SELECT * FROM t_task WHERE id = ?";
    private static final String PAGEABLE_FIND_ALL_TASKS = "SELECT * FROM t_task LIMIT ? OFFSET ?";
    private static final String FIND_TASKS_BY_USER_UUID = "SELECT * FROM t_task WHERE user_id = ?";
    private static final String UPDATE_COMPLETION_TASK_STATUS = "UPDATE t_task SET is_complete = ? WHERE id = ?";
    private static final String DELETE_TASK = "DELETE FROM t_task WHERE id = ?";

    private static final ExecutorService blockingPool = Executors.newFixedThreadPool(5);

    @Override
    public Mono<Integer> create(Task task) {
        return Mono.fromCallable(
                // This is a blocking call, but it is isolated in a separate thread pool
                () -> template.update(
                        CREATE_TASK,
                        task.getTaskId(),
                        task.getTaskName(),
                        task.isComplete(),
                        task.getUserId()
                )
        )
        .subscribeOn(fromExecutor(blockingPool))
        .onErrorMap(DatabaseException::new);
    }

    @Override
    public Mono<Task> findById(UUID uuid) {
        return Mono.fromCallable(
                // This is a blocking call, but it is isolated in a separate thread pool
                () -> template.queryForObject(
                        FIND_TASK_BY_UUID,
                        new Object[]{uuid},
                        TaskRowMapper.rowMapper
                )
        )
        .subscribeOn(fromExecutor(blockingPool))
        .onErrorResume(EmptyResultDataAccessException.class, ex -> Mono.empty())
        .onErrorMap(DatabaseException::new);
    }

    @Override
    public Flux<Task> findAll(int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return Mono.fromCallable(
                // This is a blocking call, but it is isolated in a separate thread pool
                () -> template.query(
                        PAGEABLE_FIND_ALL_TASKS,
                        new Object[]{pageSize, offset},
                        TaskRowMapper.rowMapper
                )
        )
        .subscribeOn(fromExecutor(blockingPool))
        .flatMapMany(Flux::fromIterable)
        .onErrorMap(DatabaseException::new);
    }

    @Override
    public Mono<Integer> update(Task task) {
        return Mono.fromCallable(() ->
                // This is a blocking call, but it is isolated in a separate thread pool
                template.update(
                        UPDATE_COMPLETION_TASK_STATUS,
                        task.isComplete(),
                        task.getTaskId()
                )
        )
        .subscribeOn(fromExecutor(blockingPool))
        .onErrorResume(EmptyResultDataAccessException.class, ex -> Mono.empty())
        .onErrorMap(DatabaseException::new);
    }

    @Override
    public Mono<Integer> deleteById(UUID uuid) {
        return Mono.fromCallable(
                // This is a blocking call, but it is isolated in a separate thread pool
                () -> template.update(DELETE_TASK, uuid)
        )
        .subscribeOn(fromExecutor(blockingPool))
        .onErrorResume(EmptyResultDataAccessException.class, ex -> Mono.empty())
        .onErrorMap(DatabaseException::new);
    }

    @Override
    public Flux<Task> findAllTasksByUserId(UUID userId) {
        return Mono.fromCallable(
                () -> template.query(
                        FIND_TASKS_BY_USER_UUID,
                        new Object[]{userId},
                        TaskRowMapper.rowMapper
                )
        )
        .subscribeOn(fromExecutor(blockingPool))
        .flatMapMany(Flux::fromIterable)
        .onErrorMap(DatabaseException::new);
    }
}
