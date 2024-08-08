package ru.itis.kpfu.selyantsev.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.itis.kpfu.selyantsev.configuration.ContainerConfiguration;
import ru.itis.kpfu.selyantsev.exceptions.NotBelongUserTask;
import ru.itis.kpfu.selyantsev.exceptions.TaskNotFoundException;
import ru.itis.kpfu.selyantsev.model.Task;
import ru.itis.kpfu.selyantsev.repository.impl.TaskRepositoryImpl;
import ru.itis.kpfu.selyantsev.utils.rowMapper.TaskRowMapper;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.yml")
public class TaskRepoImplTest {

    @Container
    PostgreSQLContainer<?> container = ContainerConfiguration.getInstance();

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TaskRepositoryImpl taskRepository;

    private static final String FIND_TASK_BY_UUID = "SELECT * FROM t_task WHERE id = ?";
    private static final String  COUNT_TASK = "SELECT COUNT(*) FROM t_task";
    private static final String IF_EXISTS = "SELECT COUNT(*) FROM t_task WHERE user_id = ?";

    private static final UUID USER_UUID = UUID.fromString("9c186286-0ecb-422d-b19b-3e10c13221db");
    private static final UUID NOT_VALID_UUID = UUID.fromString("1ba9b3b4-8db8-4c57-a788-2fbec242dd0a");
    private static final UUID TASK_UUID = UUID.fromString("435cb8f4-470d-4343-bdff-5209002e3c8c");
    private static final UUID NOT_VALID_TASK_UUID = UUID.fromString("84a639dd-c816-48ba-ba89-5666863d463f");

    private static final Task MAPPED_TASK = Task.builder()
            .taskId(UUID.randomUUID())
            .taskName("First Task")
            .isComplete(false)
            .build();

    private Integer countTasks() {
        return jdbcTemplate.queryForObject(COUNT_TASK, Integer.class);
    }

    private Integer isExist(UUID userId) {
        return jdbcTemplate.queryForObject(
                IF_EXISTS,
                new Object[]{userId},
                Integer.class
        );
    }


    @Test
    void testCreate() {
        Mono<UUID> taskIdMono = taskRepository.create(USER_UUID, MAPPED_TASK);

        StepVerifier.create(taskIdMono)
                .expectNext(MAPPED_TASK.getTaskId())
                .verifyComplete();

        Task taskFromDb = jdbcTemplate.queryForObject(
                FIND_TASK_BY_UUID,
                new Object[]{MAPPED_TASK.getTaskId()},
                TaskRowMapper.rowMapper
        );

        assertNotNull(taskFromDb);
        assertEquals(taskFromDb.getTaskId(), MAPPED_TASK.getTaskId());
        assertEquals(taskFromDb.getTaskName(), MAPPED_TASK.getTaskName());
        assertEquals(taskFromDb.isComplete(), MAPPED_TASK.isComplete());
        assertEquals(taskFromDb.getUserId(), USER_UUID);
    }

    @Test
    @Sql(scripts = {"classpath:/sql/task.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"classpath:/sql/cleanup.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void testFindTaskByValidUUID() {
        Mono<Task> actualTask = taskRepository.findTaskById(TASK_UUID);

        Task taskFromDb = jdbcTemplate.queryForObject(
                FIND_TASK_BY_UUID,
                new Object[]{TASK_UUID},
                TaskRowMapper.rowMapper
        );

        assertNotNull(taskFromDb);

        StepVerifier.create(actualTask)
                .expectNextMatches(
                        foundedTask -> foundedTask.getTaskId().equals(taskFromDb.getTaskId()) &&
                        foundedTask.getTaskName().equals(taskFromDb.getTaskName()) &&
                        foundedTask.isComplete() == taskFromDb.isComplete() &&
                        foundedTask.getUserId().equals(taskFromDb.getUserId())
                ).verifyComplete();
    }

    @Test
    @Sql(scripts = {"classpath:/sql/task.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"classpath:/sql/cleanup.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void testFindTaskByNotExistTaskUUID_shouldThrowTaskNotFoundException() {
        Mono<Task> notExistTask = taskRepository.findTaskById(NOT_VALID_TASK_UUID);

        StepVerifier.create(notExistTask)
                .expectErrorMatches(
                        throwable -> throwable instanceof TaskNotFoundException &&
                        throwable.getMessage().contains(NOT_VALID_TASK_UUID.toString())
                ).verify();
    }

    @Test
    @Sql(scripts = {"classpath:/sql/task.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"classpath:/sql/cleanup.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void findAllTaskByValidUserId() {
        Flux<Task> taskFlux = taskRepository.findAllTasksByUserId(USER_UUID);

        StepVerifier.create(taskFlux)
                .expectNextCount(countTasks())
                .expectComplete()
                .verify();
    }

    @Test
    @Sql(scripts = {"classpath:/sql/task.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"classpath:/sql/cleanup.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void testUpdateTaskCompletionStatusWithRelatedUserUUID() {
        Mono<Task> updatedTask = taskRepository.updateTaskCompletionStatus(USER_UUID, TASK_UUID, true);

        StepVerifier.create(updatedTask)
                .assertNext(task -> {
                    assertEquals(TASK_UUID, task.getTaskId());
                    assertEquals(USER_UUID, task.getUserId());
                }).verifyComplete();
    }

    @Test
    @Sql(scripts = {"classpath:/sql/task.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"classpath:/sql/cleanup.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void testUpdateTaskCompletionStatusWithNotRelatedUserUUID_shouldThrowNotBelongTaskException() {
        Mono<Task> updateTask = taskRepository.updateTaskCompletionStatus(NOT_VALID_UUID, TASK_UUID, true);

        StepVerifier.create(updateTask)
                .expectError(NotBelongUserTask.class)
                .verify();
    }

    @Test
    @Sql(scripts = {"classpath:/sql/task.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"classpath:/sql/cleanup.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void deleteTasksByRelatedValidUUID() {
        Mono<Void> expectedVoidResult = taskRepository.deleteUsersTaskById(USER_UUID);

        StepVerifier.create(expectedVoidResult)
                .expectComplete()
                .verify();

        int exist = isExist(USER_UUID);
        assertEquals(0, exist);
    }


}
