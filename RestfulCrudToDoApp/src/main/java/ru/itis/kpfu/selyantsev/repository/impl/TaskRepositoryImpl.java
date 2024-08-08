package ru.itis.kpfu.selyantsev.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.itis.kpfu.selyantsev.exceptions.NotBelongUserTask;
import ru.itis.kpfu.selyantsev.exceptions.TaskNotFoundException;
import ru.itis.kpfu.selyantsev.model.Task;
import ru.itis.kpfu.selyantsev.repository.TaskRepository;
import ru.itis.kpfu.selyantsev.utils.rowMapper.TaskRowMapper;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class TaskRepositoryImpl implements TaskRepository {

    private final JdbcTemplate template;

    private static final String CREATE_TASK = "INSERT INTO t_task (id, task_name, is_complete, user_id) VALUES (?, ?, ?, ?)";
    private static final String FIND_TASK_BY_UUID = "SELECT * FROM t_task WHERE id = ?";
    private static final String FIND_TASKS_BY_USER_UUID = "SELECT * FROM t_task WHERE user_id = ?";
    private static final String UPDATE_COMPLETION_TASK_STATUS = "UPDATE t_task SET is_complete = ? WHERE id = ?";
    private static final String DELETE_ALL_TASKS_BY_USER_UUID = "DELETE FROM t_task WHERE id = ?";

    @Override
    public Mono<UUID> create(UUID userId, Task task) {
        return Mono.fromCallable(() -> {
            task.setUserId(userId);

            template.update(
                    CREATE_TASK,
                    task.getTaskId(),
                    task.getTaskName(),
                    task.isComplete(),
                    task.getUserId()
            );

            return task.getTaskId();
        });
    }

    @Override
    public Mono<Task> findTaskById(UUID taskId) {
        return Mono.fromCallable(() -> {
            Task entity = template.queryForObject(
                    FIND_TASK_BY_UUID,
                    new Object[]{taskId},
                    TaskRowMapper.rowMapper
            );

            return entity;
        }).onErrorMap(EmptyResultDataAccessException.class, exception -> new TaskNotFoundException(taskId));
    }

    @Override
    public Flux<Task> findAllTasksByUserId(UUID userId) {
        return Flux.fromIterable(
                template.query(FIND_TASKS_BY_USER_UUID, new Object[]{userId}, TaskRowMapper.rowMapper)
        );
    }

    @Override
    public Mono<Task> updateTaskCompletionStatus(UUID userId, UUID taskId, boolean isComplete) {
        return Mono.fromCallable(() -> {
            Task entity = template.queryForObject(
                    FIND_TASK_BY_UUID,
                    new Object[]{taskId},
                    TaskRowMapper.rowMapper
            );

            if (!entity.getUserId().equals(userId)) {
                throw new NotBelongUserTask(taskId, userId);
            }

            entity.setComplete(isComplete);

            template.update(
                    UPDATE_COMPLETION_TASK_STATUS,
                    entity.isComplete(),
                    entity.getTaskId()
            );

            return entity;
        }).onErrorMap(EmptyResultDataAccessException.class, exception -> new NotBelongUserTask(userId, taskId));
    }

    @Override
    public Mono<Void> deleteUsersTaskById(UUID userId) {
        return Mono.fromRunnable(() -> {
            List<Task> tasks = template.query(FIND_TASKS_BY_USER_UUID, new Object[]{userId}, TaskRowMapper.rowMapper);

            if (!tasks.isEmpty()) {
                List<Object[]> batchArgs = tasks.stream()
                        .map(task -> new Object[]{task.getTaskId()})
                        .collect(Collectors.toList());

                template.batchUpdate(DELETE_ALL_TASKS_BY_USER_UUID, batchArgs);
            }
        });
    }
}
