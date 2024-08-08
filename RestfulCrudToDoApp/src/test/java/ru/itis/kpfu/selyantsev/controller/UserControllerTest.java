package ru.itis.kpfu.selyantsev.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebFlux;
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
import ru.itis.kpfu.selyantsev.dto.response.UserResponse;
import ru.itis.kpfu.selyantsev.exceptions.UserNotFoundException;
import ru.itis.kpfu.selyantsev.model.Task;
import ru.itis.kpfu.selyantsev.model.User;
import ru.itis.kpfu.selyantsev.repository.TaskRepository;
import ru.itis.kpfu.selyantsev.repository.UserRepository;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.yml")
@AutoConfigureWebFlux
public class UserControllerTest {

    @Container
    PostgreSQLContainer<?> container = ContainerConfiguration.getInstance();

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private WebTestClient webTestClient;

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

    private static final UserRequest UPDATE_REQUEST = UserRequest.builder()
            .firstName("UpdatedName")
            .lastName("UpdatedLast")
            .build();

    private static final TaskRequest TASK_REQUEST = TaskRequest.builder()
            .taskName("ExampleTaskName")
            .build();

    private static final Task TASK_ENTITY = Task.builder()
            .taskId(TASK_UUID)
            .taskName(TASK_REQUEST.getTaskName())
            .isComplete(false)
            .build();

    @BeforeEach
    public void setUp() {
        jdbcTemplate.update("TRUNCATE TABLE t_user");
        jdbcTemplate.update("TRUNCATE TABLE t_task");

        userRepository.create(USER_ENTITY).block();
        taskRepository.create(USER_UUID, TASK_ENTITY).block();
    }

    @Test
    void createUser() {
        webTestClient.post()
                .uri("/api/v1/users")
                .contentType(APPLICATION_JSON)
                .bodyValue(USER_REQUEST)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(UUID.class)
                .value(userId -> {
                    Mono<User> userFromDb = userRepository.findUserById(userId);
                    assertNotNull(userFromDb);

                    StepVerifier.create(userFromDb)
                            .expectNextMatches(
                                    user -> user.getFirstName().equals(USER_REQUEST.getFirstName()) &&
                                    user.getLastName().equals(USER_REQUEST.getLastName())
                            ).verifyComplete();
                });
    }

    @Test
    void testFindUserById() {
        webTestClient.get()
                .uri("/api/v1/users/{userId}", USER_UUID)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserResponse.class)
                .value(userResponse -> {
                    Mono<User> userFromDb = userRepository.findUserById(userResponse.getUserId());
                    assertNotNull(userFromDb);

                    StepVerifier.create(userFromDb)
                            .expectNextMatches(
                                    user -> user.getFirstName().equals(userResponse.getFirstName()) &&
                                    user.getLastName().equals(userResponse.getLastName())
                            );
                });
    }

    @Test
    void testFindAll() {
        webTestClient.get()
                .uri("/api/v1/users")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(UserResponse.class)
                .value(userList ->
                        assertEquals(1, userList.size())
                );
    }

    @Test
    void testUpdateUserById() {
        webTestClient.put()
                .uri("/api/v1/users/{userId}", USER_UUID)
                .bodyValue(UPDATE_REQUEST)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserResponse.class)
                .value(userResponse -> {
                    userResponse.getFirstName().equals(UPDATE_REQUEST.getFirstName());
                    userResponse.getLastName().equals(UPDATE_REQUEST.getLastName());
                    assertEquals(userResponse.getTaskList().size(), 1);
                });
    }

    @Test
    void testDeleteUserById() {
        webTestClient.delete()
                .uri("/api/v1/users/{userId}", USER_UUID)
                .exchange()
                .expectStatus().isNoContent()
                .expectBody()
                .consumeWith(response -> {
                    Mono<User> deletedUser = userRepository.findUserById(USER_UUID);

                    StepVerifier.create(deletedUser)
                            .expectError(UserNotFoundException.class)
                            .verify();
                });
    }
}
