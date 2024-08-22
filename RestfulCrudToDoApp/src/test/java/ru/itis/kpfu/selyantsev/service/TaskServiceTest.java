package ru.itis.kpfu.selyantsev.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.itis.kpfu.selyantsev.configuration.ContainerConfiguration;
import ru.itis.kpfu.selyantsev.dto.request.TaskRequest;
import ru.itis.kpfu.selyantsev.dto.response.TaskResponse;
import ru.itis.kpfu.selyantsev.model.Task;
import ru.itis.kpfu.selyantsev.service.impl.TaskServiceImpl;

import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.yml")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class TaskServiceTest {

    @Container
    PostgreSQLContainer<?> container = ContainerConfiguration.getInstance();

    @Autowired
    private TaskServiceImpl taskService;

    private static final UUID TASK_UUID = UUID.fromString("435cb8f4-470d-4343-bdff-5209002e3c8c");
    private static final UUID USER_UUID = UUID.fromString("9c186286-0ecb-422d-b19b-3e10c13221db");

    private static final TaskRequest TASK_REQUEST = TaskRequest.builder()
            .taskName("ExampleTaskName")
            .build();

    private static final Task TASK_ENTITY = Task.builder()
            .taskId(TASK_UUID)
            .taskName("FirstTaskName")
            .isComplete(false)
            .userId(USER_UUID)
            .build();

    private static final TaskResponse TASK_RESPONSE = TaskResponse.builder()
            .taskId(TASK_UUID)
            .taskName(TASK_ENTITY.getTaskName())
            .isComplete(TASK_ENTITY.isComplete())
            .userId(USER_UUID)
            .build();

    @Test
    @Sql(scripts = {"classpath:/sql/task.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void testCreate() {
        Mono<UUID> result = taskService.create(USER_UUID, TASK_REQUEST);

        StepVerifier.create(result)
                .expectNextMatches(Objects::nonNull)
                .verifyComplete();
    }

    @Test
    @Sql(scripts = {"classpath:/sql/task.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void testFindTaskById() {
        Mono<TaskResponse> actualResponse = taskService.findTaskById(TASK_UUID);

        StepVerifier.create(actualResponse)
                .expectNextMatches(
                        response -> response.getTaskName().equals(TASK_RESPONSE.getTaskName()) &&
                        response.getUserId().equals(TASK_RESPONSE.getUserId())
                ).verifyComplete();
    }

    @Test
    @Sql(scripts = {"classpath:/sql/task.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void testFindAll() {
        Flux<TaskResponse> actualResult = taskService.findAll(1, 10);

        StepVerifier.create(actualResult)
                .expectNextMatches(
                        response -> response.getTaskName().equals(TASK_RESPONSE.getTaskName()) &&
                        response.getUserId().equals(TASK_RESPONSE.getUserId())
                ).verifyComplete();
    }

    @Test
    @Sql(scripts = {"classpath:/sql/task.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void testFindAllTaskByUserUUID() {
        Flux<TaskResponse> actualResult = taskService.findAllTasksByUserId(USER_UUID);

        StepVerifier.create(actualResult)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    @Sql(scripts = {"classpath:/sql/task.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void testUpdateTask() {
        Mono<TaskResponse> actualResponse = taskService.updateTaskCompletionStatus(USER_UUID, TASK_UUID, true);

        StepVerifier.create(actualResponse)
                .assertNext(response -> {
                    assertTrue(response.isComplete());
                    assertEquals(response.getUserId(), USER_UUID);
                }).verifyComplete();
    }

    @Test
    @Sql(scripts = {"classpath:/sql/task.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void testDeleteUser() {
        Mono<Void> actualResult = taskService.deleteTaskById(TASK_UUID);

        StepVerifier.create(actualResult)
                .verifyComplete();
    }

}
