package ru.itis.kpfu.selyantsev.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.itis.kpfu.selyantsev.configuration.ContainerConfiguration;
import ru.itis.kpfu.selyantsev.dto.request.TaskRequest;
import ru.itis.kpfu.selyantsev.dto.request.UserRequest;
import ru.itis.kpfu.selyantsev.dto.response.TaskResponse;
import ru.itis.kpfu.selyantsev.model.Task;
import ru.itis.kpfu.selyantsev.model.User;
import ru.itis.kpfu.selyantsev.repository.CrudRepository;
import ru.itis.kpfu.selyantsev.repository.TaskRepository;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.yml")
public class TaskControllerTest {

    @Container
    PostgreSQLContainer<?> container = ContainerConfiguration.getInstance();

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private CrudRepository<User, UUID> userRepository;

    @Autowired
    private WebTestClient webTestClient;

    private static final String API_PATH = "/api/v1/tasks";

    private static final UUID USER_UUID = UUID.fromString("9c186286-0ecb-422d-b19b-3e10c13221db");
    private static final UUID TASK_UUID = UUID.fromString("e2fabc2c-1722-4ecc-8a09-59a00eebc91b");

    private static final UserRequest USER_REQUEST = UserRequest.builder()
            .firstName("FirstName")
            .lastName("LastName")
            .build();
    private static final User USER_ENTITY = User.builder()
            .userId(USER_UUID)
            .firstName(USER_REQUEST.getFirstName())
            .lastName(USER_REQUEST.getLastName())
            .build();

    private static final TaskRequest TASK_REQUEST = TaskRequest.builder()
            .taskName("ExampleTask")
            .build();

    private static final Task TASK_ENTITY = Task.builder()
            .taskId(TASK_UUID)
            .taskName(TASK_REQUEST.getTaskName())
            .isComplete(false)
            .userId(USER_UUID)
            .build();

    @BeforeEach
    public void setUp() {
        jdbcTemplate.update("TRUNCATE TABLE t_user");
        jdbcTemplate.update("TRUNCATE TABLE t_task");

        userRepository.create(USER_ENTITY).block();
        taskRepository.create(TASK_ENTITY).block();
    }

    @Test
    void testCreate() {
        webTestClient.post()
                .uri(API_PATH + "/{userId}", USER_UUID)
                .contentType(APPLICATION_JSON)
                .bodyValue(TASK_REQUEST)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(UUID.class)
                .value(taskId -> {
                    Mono<Task> taskFromDb = taskRepository.findById(TASK_UUID);
                    assertNotNull(taskFromDb);

                    StepVerifier.create(taskFromDb)
                            .expectNextMatches(
                                    task -> task.getTaskName().equals(TASK_REQUEST.getTaskName()) &&
                                    task.getUserId().equals(USER_UUID)
                            ).verifyComplete();
                });
    }

    @Test
    void testFindTaskById() {
        webTestClient.get()
                .uri(API_PATH + "/{taskId}", TASK_UUID)
                .exchange()
                .expectStatus().isOk()
                .expectBody(TaskResponse.class)
                .value(taskResponse -> {
                    assertAll(
                            () -> assertNotNull(taskResponse),
                            () -> assertEquals(TASK_UUID, taskResponse.getTaskId()),
                            () -> assertEquals(USER_UUID, taskResponse.getUserId()),
                            () -> assertEquals(TASK_REQUEST.getTaskName(), taskResponse.getTaskName())
                    );
                });
    }

    @Test
    void testFindAll() {
        webTestClient.get().uri(uriBuilder ->
                uriBuilder.path(API_PATH)
                        .queryParam("page", 1)
                        .queryParam("pageSize", 10)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TaskResponse.class)
                .hasSize(1)
                .consumeWith(responseList -> {
                    TaskResponse actualResponse = responseList.getResponseBody().get(0);
                    assertAll(
                            () -> assertEquals(actualResponse.getTaskId(), TASK_UUID),
                            () -> assertEquals(actualResponse.getUserId(), USER_UUID)
                    );
                });
    }

    @Test
    void testFindAllTasksByUserId() {
        webTestClient.get().uri(uriBuilder ->
                        uriBuilder.path(API_PATH + "/users")
                                .queryParam("userId", USER_UUID)
                                .build())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TaskResponse.class)
                .value(response -> {
                    assertEquals(response.size(), 1);
                    assertEquals(response.get(0).getUserId(), USER_UUID);
                });
    }

    @Test
    void testUpdateTaskCompletion() {
        webTestClient.put().uri(uriBuilder ->
                        uriBuilder.path(API_PATH)
                                .queryParam("userId", USER_UUID)
                                .queryParam("taskId", TASK_UUID)
                                .queryParam("isComplete", true)
                                .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(TaskResponse.class)
                .value(taskResponse -> {
                    assertAll(
                            () -> assertNotNull(taskResponse),
                            () -> assertTrue(taskResponse.isComplete()),
                            () -> assertEquals(USER_UUID, taskResponse.getUserId())
                    );
                });
    }

    @Test
    void testDeleteUsersTaskById() {
        webTestClient.delete().uri(uriBuilder ->
                        uriBuilder.path(API_PATH)
                                .queryParam("taskId", TASK_UUID)
                                .build())
                .exchange()
                .expectStatus().isNoContent();
    }
}
