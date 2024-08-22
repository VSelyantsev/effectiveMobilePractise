package ru.itis.kpfu.selyantsev.repository;

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
import ru.itis.kpfu.selyantsev.model.User;
import ru.itis.kpfu.selyantsev.repository.impl.UserRepositoryImpl;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.yml")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UserRepoImplTest {

    @Container
    PostgreSQLContainer<?> container = ContainerConfiguration.getInstance();

    @Autowired
    private UserRepositoryImpl userRepository;

    private static final UUID USER_UUID = UUID.fromString("8004666d-0787-4a79-b338-ef88a6e1c6b0");
    private static final UUID USER_UUID_FOR_CREATE = UUID.fromString("8004666d-0787-4a79-b338-ef88a6e1c619");

    private static final User MAPPED_USER = User.builder()
            .userId(USER_UUID)
            .firstName("test1")
            .lastName("test2")
            .build();

    @Test
    void testCreateUser() {
        User userForCreate = User.builder()
                .userId(USER_UUID_FOR_CREATE)
                .firstName("testName")
                .lastName("testName")
                .build();

        Mono<Integer> result = userRepository.create(userForCreate);

        StepVerifier.create(result)
                .expectNext(1)
                .verifyComplete();

        Mono<User> userFromDb = userRepository.findById(USER_UUID_FOR_CREATE);

        StepVerifier.create(userFromDb)
                .expectNextMatches(
                        user -> user.getFirstName().equals(userForCreate.getFirstName()) &&
                        user.getUserId().equals(userForCreate.getUserId()) &&
                        user.getLastName().equals(userForCreate.getLastName())
                ).verifyComplete();
    }

    @Test
    @Sql(scripts = {"classpath:/sql/user.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void testFindUserByValidUserUUID() {
        Mono<User> result = userRepository.findById(USER_UUID);

        StepVerifier.create(result)
                .expectNextMatches(
                        foundedUser -> foundedUser.getUserId().equals(MAPPED_USER.getUserId()) &&
                        foundedUser.getFirstName().equals(MAPPED_USER.getFirstName()) &&
                        foundedUser.getLastName().equals(MAPPED_USER.getLastName())
                ).verifyComplete();
    }

    @Test
    @Sql(scripts = {"classpath:/sql/user.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void testFindAll() {
        Flux<User> userFlux = userRepository.findAll(1, 10);

        StepVerifier.create(userFlux.count())
                .assertNext(count -> assertTrue(count > 0))
                .verifyComplete();
    }

    @Test
    @Sql(scripts = {"classpath:/sql/user.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void testUpdateUserByValidUUID() {
        MAPPED_USER.setFirstName("updatedName");
        MAPPED_USER.setLastName("updatedName2");

        Mono<Integer> updatedUser = userRepository.update(MAPPED_USER);

        StepVerifier.create(updatedUser)
                .expectNext(1)
                .verifyComplete();

        Mono<User> userFromDb = userRepository.findById(USER_UUID);

        StepVerifier.create(userFromDb)
                .expectNextMatches(
                        user -> user.getFirstName().equals("updatedName") &&
                        user.getLastName().equals("updatedName2")
                ).verifyComplete();
    }

    @Test
    @Sql(scripts = {"classpath:/sql/user.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void testDeleteUserByValidUUID() {
        Mono<Integer> expectedVoidResult = userRepository.deleteById(USER_UUID);

        StepVerifier.create(expectedVoidResult)
                .expectNext(1)
                .verifyComplete();

        Mono<User> userFromDb = userRepository.findById(USER_UUID);

        StepVerifier.create(userFromDb)
                .expectComplete()
                .verify();
    }

}
