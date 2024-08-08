package ru.itis.kpfu.selyantsev.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.itis.kpfu.selyantsev.exceptions.UserNotFoundException;
import ru.itis.kpfu.selyantsev.model.User;
import ru.itis.kpfu.selyantsev.repository.UserRepository;
import ru.itis.kpfu.selyantsev.utils.rowMapper.UserRowMapper;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final JdbcTemplate template;

    private static final String CREATE_USER = "INSERT INTO t_user (id, first_name, last_name) values (?, ?, ?)";
    private static final String FIND_USER_BY_UUID = "SELECT * FROM t_user WHERE id = ?";
    private static final String FIND_ALL = "SELECT * FROM t_user";
    private static final String UPDATE_USER = "UPDATE t_user SET first_name = ?, last_name = ? WHERE id = ?";
    private static final String DELETE_USER = "DELETE FROM t_user WHERE id = ?";

    @Override
    public Mono<UUID> create(User user) {
        return Mono.fromCallable(() -> {
            template.update(CREATE_USER, user.getUserId(), user.getFirstName(), user.getLastName());
            return user.getUserId();
        });
    }

    @Override
    public Mono<User> findUserById(UUID userId) {
        return Mono.fromCallable(
                () -> template.queryForObject(
                    FIND_USER_BY_UUID,
                    new Object[]{userId},
                    UserRowMapper.rowMapper
            )
        ).onErrorMap(EmptyResultDataAccessException.class, exception -> new UserNotFoundException(userId));
    }

    @Override
    public Flux<User> findAll() {
        return Flux.fromIterable(
                template.query(FIND_ALL, UserRowMapper.rowMapper)
        );
    }

    @Override
    public Mono<User> updateUserById(UUID userId, User user) {
        return Mono.fromCallable(() -> {
            User userToUpdate = template.queryForObject(
                    FIND_USER_BY_UUID,
                    new Object[]{userId},
                    UserRowMapper.rowMapper
            );

            userToUpdate.setFirstName(user.getFirstName());
            userToUpdate.setLastName(user.getLastName());

            template.update(
                    UPDATE_USER,
                    userToUpdate.getFirstName(),
                    userToUpdate.getLastName(),
                    userToUpdate.getUserId()
            );

            return userToUpdate;
        }).onErrorMap(EmptyResultDataAccessException.class, exception -> new UserNotFoundException(userId));
    }

    @Override
    public Mono<Void> deleteUserById(UUID userId) {
        return Mono.fromRunnable(
                () -> template.update(DELETE_USER, userId)
        );

    }
}
