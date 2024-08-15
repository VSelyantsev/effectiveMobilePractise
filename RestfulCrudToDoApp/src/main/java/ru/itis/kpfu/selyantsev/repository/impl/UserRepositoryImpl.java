package ru.itis.kpfu.selyantsev.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.itis.kpfu.selyantsev.exceptions.DatabaseException;
import ru.itis.kpfu.selyantsev.model.User;
import ru.itis.kpfu.selyantsev.repository.CrudRepository;
import ru.itis.kpfu.selyantsev.utils.rowMapper.UserRowMapper;

import java.util.UUID;
import java.util.concurrent.*;

import static reactor.core.scheduler.Schedulers.fromExecutor;

@Repository
@RequiredArgsConstructor
@SuppressWarnings("BlockingMethodInNonBlockingContext")
public class UserRepositoryImpl implements CrudRepository<User, UUID> {
    private final JdbcTemplate template;

    private static final String CREATE_USER = "INSERT INTO t_user (id, first_name, last_name) values (?, ?, ?)";
    private static final String FIND_USER_BY_UUID = "SELECT * FROM t_user WHERE id = ?";
    private static final String PAGEABLE_FIND_ALL = "SELECT * FROM t_user LIMIT ? OFFSET ?";
    private static final String UPDATE_USER = "UPDATE t_user SET first_name = ?, last_name = ? WHERE id = ?";
    private static final String DELETE_USER = "DELETE FROM t_user WHERE id = ?";
    private static final String DELETE_TASKS_BY_USER_ID = "DELETE FROM t_task WHERE user_id = ?";

    private final ExecutorService blockingPool = Executors.newFixedThreadPool(5);

    @Override
    public Mono<Integer> create(User user) {
        return Mono.fromCallable(
                // This is a blocking call, but it is isolated in a separate thread pool
                () -> template.update(
                        CREATE_USER,
                        user.getUserId(),
                        user.getFirstName(),
                        user.getLastName()
                )
        )
        .subscribeOn(fromExecutor(blockingPool))
        .onErrorResume(EmptyResultDataAccessException.class, ex -> Mono.empty())
        .onErrorMap(DatabaseException::new);
    }

    @Override
    public Mono<User> findById(UUID uuid) {
        return Mono.fromCallable(
                // This is a blocking call, but it is isolated in a separate thread pool
                () -> template.queryForObject(
                        FIND_USER_BY_UUID,
                        new Object[]{uuid},
                        UserRowMapper.rowMapper
                )
        )
        .subscribeOn(fromExecutor(blockingPool))
        .onErrorResume(EmptyResultDataAccessException.class, ex -> Mono.empty())
        .onErrorMap(DatabaseException::new);
    }

    @Override
    public Flux<User> findAll(int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return Flux.fromIterable(
                // This is a blocking call, but it is isolated in a separate thread pool
                template.query(
                        PAGEABLE_FIND_ALL,
                        new Object[]{pageSize, offset},
                        UserRowMapper.rowMapper
                )
        )
        .subscribeOn(fromExecutor(blockingPool))
        .onErrorMap(DatabaseException::new);
    }

    @Override
    public Mono<Integer> update(User user) {
        return Mono.fromCallable(() ->
                // This is a blocking call, but it is isolated in a separate thread pool
                template.update(
                        UPDATE_USER,
                        user.getFirstName(),
                        user.getLastName(),
                        user.getUserId()
            )
        )
        .subscribeOn(fromExecutor(blockingPool))
        .onErrorResume(EmptyResultDataAccessException.class, ex -> Mono.empty())
        .onErrorMap(DatabaseException::new);
    }

    @Override
    public Mono<Integer> deleteById(UUID uuid) {
        return Mono.fromCallable(() -> {
            // This is a blocking call, but it is isolated in a separate thread pool
            template.update(DELETE_TASKS_BY_USER_ID, uuid);

            // This is a blocking call, but it is isolated in a separate thread pool
            return template.update(DELETE_USER, uuid);
        })
        .subscribeOn(fromExecutor(blockingPool))
        .onErrorResume(EmptyResultDataAccessException.class, ex -> Mono.empty())
        .onErrorMap(DatabaseException::new);
    }
}
