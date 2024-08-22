package ru.itis.kpfu.selyantsev.junit.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.itis.kpfu.selyantsev.exceptions.DatabaseException;
import ru.itis.kpfu.selyantsev.model.User;
import ru.itis.kpfu.selyantsev.repository.impl.UserRepositoryImpl;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserRepoJunitTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private UserRepositoryImpl userRepository;

    private static final User USER_ENTITY = User.builder()
            .userId(UUID.randomUUID())
            .firstName("Vlad1")
            .lastName("LastName1")
            .build();

    @Test
    void testCreate() {
        when(jdbcTemplate.update(any(String.class), any(UUID.class), anyString(), anyString()))
                .thenReturn(1);

        Mono<Integer> result = userRepository.create(USER_ENTITY);

        StepVerifier.create(result)
                .expectNext(1)
                .verifyComplete();
    }

    @Test
    void testCreate_shouldThrowEmptyResultDataAccessException() {
        when(jdbcTemplate.update(any(String.class), any(UUID.class), anyString(), anyString()))
                .thenThrow(DatabaseException.class);

        Mono<Integer> result = userRepository.create(USER_ENTITY);

        StepVerifier.create(result)
                .expectError(DatabaseException.class)
                .verify();
    }

    @Test
    void testFindById() {
        when(jdbcTemplate.queryForObject(
                anyString(),
                any(Object[].class),
                any(RowMapper.class))
        ).thenReturn(USER_ENTITY);

        Mono<User> result = userRepository.findById(USER_ENTITY.getUserId());

        StepVerifier.create(result)
                .expectNext(USER_ENTITY)
                .verifyComplete();
    }

    @Test
    void testFindById_shouldThrowEmptyResultException() {
        when(jdbcTemplate.queryForObject(
                anyString(),
                any(Object[].class),
                any(RowMapper.class)
        )).thenThrow(new EmptyResultDataAccessException(1));

        Mono<User> result = userRepository.findById(USER_ENTITY.getUserId());

        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void testFindById_shouldThrowDatabaseException() {
        when(jdbcTemplate.queryForObject(
                anyString(),
                any(Object[].class),
                any(RowMapper.class)
        )).thenThrow(DatabaseException.class);

        Mono<User> result = userRepository.findById(USER_ENTITY.getUserId());

        StepVerifier.create(result)
                .expectError(DatabaseException.class)
                .verify();
    }

    @Test
    void testFindAll_shouldReturnValidResponse() {
        when(jdbcTemplate.query(anyString(), any(Object[].class), any(RowMapper.class)))
                .thenReturn(List.of(USER_ENTITY));

        Flux<User> result = userRepository.findAll(0, 10);

        StepVerifier.create(result)
                .expectNext(USER_ENTITY)
                .verifyComplete();
    }

    @Test
    void testFindAll_shouldReturnDatabaseException() {
        when(jdbcTemplate.query(
                anyString(),
                any(Object[].class),
                any(RowMapper.class)
        )).thenThrow(DatabaseException.class);

        Flux<User> result = userRepository.findAll(0, 10);

        StepVerifier.create(result)
                .expectError(DatabaseException.class)
                .verify();
    }

    @Test
    void testUpdateUser_shouldReturnValidResponse() {
        when(jdbcTemplate.update(anyString(), any(Object[].class)))
                .thenReturn(1);

        Mono<Integer> result = userRepository.update(USER_ENTITY);

        StepVerifier.create(result)
                .expectNext(1)
                .verifyComplete();
    }

    @Test
    void testUpdateUser_shouldReturnEmptyResultDataAccessException() {
        when(jdbcTemplate.update(anyString(), any(Object[].class)))
                .thenThrow(new EmptyResultDataAccessException(1));

        Mono<Integer> result = userRepository.update(USER_ENTITY);

        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void testUpdateUser_shouldReturnDatabaseException() {
        when(jdbcTemplate.update(anyString(), any(Object[].class)))
                .thenThrow(DatabaseException.class);

        Mono<Integer> result = userRepository.update(USER_ENTITY);

        StepVerifier.create(result)
                .expectError(DatabaseException.class)
                .verify();
    }

    @Test
    void testDeleteUser_shouldBeVerified() {
        when(jdbcTemplate.update(anyString(), any(Object[].class)))
                .thenReturn(1);

        Mono<Integer> result = userRepository.deleteById(USER_ENTITY.getUserId());

        StepVerifier.create(result)
                .expectNext(1)
                .verifyComplete();
    }

    @Test
    void testDeleteUser_shouldReturnEmptyResultDataAccessException() {
        when(jdbcTemplate.update(anyString(), any(Object[].class)))
                .thenThrow(new EmptyResultDataAccessException(1));

        Mono<Integer> result = userRepository.deleteById(USER_ENTITY.getUserId());

        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void testDeleteUser_shouldReturnDatabaseException() {
        when(jdbcTemplate.update(anyString(), any(Object[].class)))
                .thenThrow(DatabaseException.class);

        Mono<Integer> result = userRepository.deleteById(USER_ENTITY.getUserId());

        StepVerifier.create(result)
                .expectError(DatabaseException.class)
                .verify();
    }

}
