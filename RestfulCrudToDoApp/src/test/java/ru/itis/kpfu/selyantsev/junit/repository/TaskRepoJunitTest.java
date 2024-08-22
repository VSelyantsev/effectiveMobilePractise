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
import ru.itis.kpfu.selyantsev.model.Task;
import ru.itis.kpfu.selyantsev.repository.impl.TaskRepositoryImpl;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskRepoJunitTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private TaskRepositoryImpl taskRepository;

    private static final Task TASK_ENTITY = Task.builder()
            .taskId(UUID.randomUUID())
            .taskName("testTaskName")
            .userId(UUID.randomUUID())
            .build();

    @Test
    void testCreate() {
        when(jdbcTemplate.update(anyString(), any(Object[].class)))
                .thenReturn(1);

        Mono<Integer> result = taskRepository.create(TASK_ENTITY);

        StepVerifier.create(result)
                .expectNext(1)
                .verifyComplete();
    }

    @Test
    void testCreate_shouldReturnDatabaseException() {
        when(jdbcTemplate.update(anyString(), any(Object[].class)))
                .thenThrow(DatabaseException.class);

        Mono<Integer> result = taskRepository.create(TASK_ENTITY);

        StepVerifier.create(result)
                .expectError(DatabaseException.class)
                .verify();
    }

    @Test
    void testFindById_shouldReturnValidResponse() {
        when(jdbcTemplate.queryForObject(
                anyString(),
                any(Object[].class),
                any(RowMapper.class)
        )).thenReturn(TASK_ENTITY);

        Mono<Task> result = taskRepository.findById(TASK_ENTITY.getTaskId());

        StepVerifier.create(result)
                .expectNext(TASK_ENTITY)
                .verifyComplete();
    }

    @Test
    void testFindById_shouldReturnEmptyResultException() {
        when(jdbcTemplate.queryForObject(
                anyString(),
                any(Object[].class),
                any(RowMapper.class)
        )).thenThrow(new EmptyResultDataAccessException(1));

        Mono<Task> result = taskRepository.findById(TASK_ENTITY.getTaskId());

        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void testFindById_shouldReturnDatabaseException() {
        when(jdbcTemplate.queryForObject(
                anyString(),
                any(Object[].class),
                any(RowMapper.class)
        )).thenThrow(DatabaseException.class);


        Mono<Task> result = taskRepository.findById(TASK_ENTITY.getTaskId());

        StepVerifier.create(result)
                .expectError(DatabaseException.class)
                .verify();
    }

    @Test
    void testFindAll_shouldReturnValidResponse() {
        when(jdbcTemplate.query(
                anyString(),
                any(Object[].class),
                any(RowMapper.class)
        )).thenReturn(List.of(TASK_ENTITY));

        Flux<Task> result = taskRepository.findAll(0, 10);

        StepVerifier.create(result)
                .expectNext(TASK_ENTITY)
                .verifyComplete();
    }

    @Test
    void testFindAll_shouldReturnDatabaseException() {
        when(jdbcTemplate.query(
                anyString(),
                any(Object[].class),
                any(RowMapper.class)
        )).thenThrow(DatabaseException.class);

        Flux<Task> result = taskRepository.findAll(0, 10);

        StepVerifier.create(result)
                .expectError(DatabaseException.class)
                .verify();
    }

    @Test
    void testUpdate_shouldReturnValidResponse() {
        when(jdbcTemplate.update(anyString(), any(Object[].class)))
                .thenReturn(1);

        Mono<Integer> result = taskRepository.update(TASK_ENTITY);

        StepVerifier.create(result)
                .expectNext(1)
                .verifyComplete();
    }

    @Test
    void testUpdate_shouldReturnEmptyResultException() {
        when(jdbcTemplate.update(anyString(), any(Object[].class)))
                .thenThrow(new EmptyResultDataAccessException(1));

        Mono<Integer> result = taskRepository.update(TASK_ENTITY);

        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void testUpdate_shouldReturnDatabaseException() {
        when(jdbcTemplate.update(anyString(), any(Object[].class)))
                .thenThrow(DatabaseException.class);

        Mono<Integer> result = taskRepository.update(TASK_ENTITY);

        StepVerifier.create(result)
                .expectError(DatabaseException.class)
                .verify();
    }

    @Test
    void testDelete_shouldReturnValidResponse() {
        when(jdbcTemplate.update(anyString(), any(Object[].class)))
                .thenReturn(1);

        Mono<Integer> result = taskRepository.deleteById(TASK_ENTITY.getTaskId());

        StepVerifier.create(result)
                .expectNext(1)
                .verifyComplete();
    }

    @Test
    void testDelete_shouldReturnEmptyResultException() {
        when(jdbcTemplate.update(anyString(), any(Object[].class)))
                .thenThrow(new EmptyResultDataAccessException(1));

        Mono<Integer> result = taskRepository.deleteById(TASK_ENTITY.getTaskId());

        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void testFindAllTasksByUserId_shouldReturnValidResponse() {
        when(jdbcTemplate.query(
                anyString(),
                any(Object[].class),
                any(RowMapper.class)
        )).thenReturn(List.of(TASK_ENTITY));

        Flux<Task> result = taskRepository.findAllTasksByUserId(TASK_ENTITY.getUserId());

        StepVerifier.create(result)
                .expectNext(TASK_ENTITY)
                .verifyComplete();
    }

    @Test
    void testFindAllTasksByUserId_shouldReturnDatabaseException() {
        when(jdbcTemplate.query(
                anyString(),
                any(Object[].class),
                any(RowMapper.class)
        )).thenThrow(DatabaseException.class);

        Flux<Task> result = taskRepository.findAllTasksByUserId(TASK_ENTITY.getUserId());

        StepVerifier.create(result)
                .expectError(DatabaseException.class)
                .verify();
    }
}
