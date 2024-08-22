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
import ru.itis.kpfu.selyantsev.dto.request.UserRequest;
import ru.itis.kpfu.selyantsev.dto.response.UserResponse;
import ru.itis.kpfu.selyantsev.model.User;
import ru.itis.kpfu.selyantsev.service.impl.UserServiceImpl;

import java.util.Objects;
import java.util.UUID;


@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.yml")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UserServiceTest {

    @Container
    PostgreSQLContainer<?> container = ContainerConfiguration.getInstance();

    @Autowired
    private UserServiceImpl userService;

    private static final UUID USER_UUID = UUID.fromString("8004666d-0787-4a79-b338-ef88a6e1c6b0");
    private static final UserRequest USER_REQUEST = UserRequest.builder()
            .firstName("test1")
            .lastName("test2")
            .build();

    private static final UserRequest REQUEST_FOR_UPDATE = UserRequest.builder()
            .firstName("updatedName1")
            .lastName("updatedName2")
            .build();

    private static final User USER_ENTITY = User.builder()
            .userId(USER_UUID)
            .firstName(USER_REQUEST.getFirstName())
            .lastName(USER_REQUEST.getLastName())
            .build();

    private static final UserResponse USER_RESPONSE = UserResponse.builder()
            .userId(USER_UUID)
            .firstName(USER_ENTITY.getFirstName())
            .lastName(USER_ENTITY.getLastName())
            .build();

    @Test
    @Sql(scripts = {"classpath:/sql/user.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void testCreateUser() {
        Mono<UUID> result = userService.create(USER_REQUEST);

        StepVerifier.create(result)
                .expectNextMatches(Objects::nonNull)
                .verifyComplete();
    }

    @Test
    @Sql(scripts = {"classpath:/sql/user.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void testFindUserByValidUUID() {
        Mono<UserResponse> userFromDb = userService.findUserById(USER_UUID);

        StepVerifier.create(userFromDb)
                .expectNextMatches(
                        response -> response.getFirstName().equals(USER_RESPONSE.getFirstName()) &&
                        response.getLastName().equals(USER_RESPONSE.getLastName()) &&
                        response.getTaskList().isEmpty()
                ).verifyComplete();
    }

    @Test
    @Sql(scripts = {"classpath:/sql/user.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void testFindAll() {
        Flux<UserResponse> actualResponse = userService.findAll(1, 10);

        StepVerifier.create(actualResponse)
                .expectNextMatches(
                        response -> response.getFirstName().equals(USER_RESPONSE.getFirstName()) &&
                        response.getLastName().equals(USER_RESPONSE.getLastName()) &&
                        response.getTaskList().isEmpty()
                ).verifyComplete();
    }

    @Test
    @Sql(scripts = {"classpath:/sql/user.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void testUpdateUser() {
        Mono<UserResponse> updatedUser = userService.updateUserById(USER_UUID, REQUEST_FOR_UPDATE);

        StepVerifier.create(updatedUser)
                .expectNextMatches(
                        response -> response.getFirstName().equals(REQUEST_FOR_UPDATE.getFirstName()) &&
                        response.getLastName().equals(REQUEST_FOR_UPDATE.getLastName()) &&
                        response.getTaskList().isEmpty()
                ).verifyComplete();
    }

    @Test
    @Sql(scripts = {"classpath:/sql/user.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void testDeleteUser() {
        Mono<Void> actualResult = userService.deleteUserById(USER_UUID);

        StepVerifier.create(actualResult)
                .verifyComplete();
    }
}
