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
import ru.itis.kpfu.selyantsev.exceptions.UserNotFoundException;
import ru.itis.kpfu.selyantsev.model.User;
import ru.itis.kpfu.selyantsev.repository.impl.UserRepositoryImpl;
import ru.itis.kpfu.selyantsev.utils.rowMapper.UserRowMapper;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.yml")
public class UserRepoImplTest {

    @Container
    PostgreSQLContainer<?> container = ContainerConfiguration.getInstance();

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserRepositoryImpl userRepository;

    private static final String FIND_USER_BY_UUID = "SELECT * FROM t_user WHERE id = ?";
    private static final String COUNT_USER = "SELECT COUNT(*) FROM t_user";
    private static final String IF_EXISTS = "SELECT COUNT(*) FROM t_user WHERE id = ?";

    private static final UUID USER_UUID = UUID.fromString("e87f4a13-0bed-41ab-a243-ba3f353452fb");
    private static final UUID NOT_VALID_UUID = UUID.fromString("61eeee71-6eee-4df1-9306-a21b802dd19d");
    private static final User MAPPED_USER = User.builder()
            .userId(UUID.randomUUID())
            .firstName("vlad1")
            .lastName("sel1")
            .build();

    private static final User EMPTY_USER = User.builder().build();

    private Integer countUsers() {
        return jdbcTemplate.queryForObject(COUNT_USER, Integer.class);
    }

    private Integer checkIfExist(UUID userId) {
        return jdbcTemplate.queryForObject(
                IF_EXISTS,
                new Object[]{userId},
                Integer.class
        );
    }

    @Test
    void testCreateUser() {
        Mono<UUID> userIdMono = userRepository.create(MAPPED_USER);

        StepVerifier.create(userIdMono)
                .expectNext(MAPPED_USER.getUserId())
                .verifyComplete();

        User user = jdbcTemplate.queryForObject(
                FIND_USER_BY_UUID,
                new Object[]{MAPPED_USER.getUserId()},
                UserRowMapper.rowMapper
        );

        assertNotNull(user);
        assertEquals(user.getUserId(), MAPPED_USER.getUserId());
        assertEquals(user.getFirstName(), MAPPED_USER.getFirstName());
        assertEquals(user.getLastName(), MAPPED_USER.getLastName());
        assertNotNull(userIdMono);
    }

    @Test
    @Sql(scripts = {"classpath:/sql/user.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"classpath:/sql/cleanup.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void testFindUserByValidUserUUID() {
        Mono<User> result = userRepository.findUserById(USER_UUID);

        User userFromDb = jdbcTemplate.queryForObject(
                FIND_USER_BY_UUID,
                new Object[] {USER_UUID},
                UserRowMapper.rowMapper
        );

        assertNotNull(userFromDb);

        StepVerifier.create(result)
                .expectNextMatches(
                        foundedUser -> foundedUser.getUserId().equals(userFromDb.getUserId()) &&
                        foundedUser.getFirstName().equals(userFromDb.getFirstName()) &&
                        foundedUser.getLastName().equals(userFromDb.getLastName())
                ).verifyComplete();
    }

    @Test
    @Sql(scripts = {"classpath:/sql/user.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"classpath:/sql/cleanup.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void testFindUserByNotExistUUID_shouldThrowUserNotFoundException() {
        Mono<User> notExistUser = userRepository.findUserById(NOT_VALID_UUID);

        StepVerifier.create(notExistUser)
                .expectErrorMatches(
                        throwable -> throwable instanceof UserNotFoundException &&
                        throwable.getMessage().contains(NOT_VALID_UUID.toString())
                ).verify();
    }

    @Test
    @Sql(scripts = {"classpath:/sql/user.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"classpath:/sql/cleanup.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void testFindAll() {
        Flux<User> userFlux = userRepository.findAll();

        StepVerifier.create(userFlux)
                .expectNextCount(countUsers())
                .expectComplete()
                .verify();
    }

    @Test
    @Sql(scripts = {"classpath:/sql/user.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"classpath:/sql/cleanup.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void testUpdateUserByValidUUID() {
        User dataForUpdate = User.builder()
                .firstName("updatedUserFirstName")
                .lastName("updatedUserLastName")
                .build();

        Mono<User> updatedUser = userRepository.updateUserById(USER_UUID, dataForUpdate);

        assertNotNull(updatedUser);

        StepVerifier.create(updatedUser)
                .expectNextMatches(user ->
                        user.getUserId().equals(USER_UUID) &&
                        user.getFirstName().equals(dataForUpdate.getFirstName()) &&
                        user.getLastName().equals(dataForUpdate.getLastName())
                ).verifyComplete();


        User userFromDb = jdbcTemplate.queryForObject(
                FIND_USER_BY_UUID,
                new Object[] {USER_UUID},
                UserRowMapper.rowMapper
        );

        assertNotNull(userFromDb);
        assertEquals(dataForUpdate.getFirstName(), userFromDb.getFirstName());
        assertEquals(dataForUpdate.getLastName(), userFromDb.getLastName());
    }

    @Test
    @Sql(scripts = {"classpath:/sql/user.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"classpath:/sql/cleanup.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void testUpdateUserByNotExistUUID_shouldThrowUserNotFoundException() {

        Mono<User> notExistUser = userRepository.updateUserById(NOT_VALID_UUID, EMPTY_USER);

        StepVerifier.create(notExistUser)
                .expectErrorMatches(
                        throwable -> throwable instanceof UserNotFoundException &&
                        throwable.getMessage().contains(NOT_VALID_UUID.toString())
                ).verify();
    }

    @Test
    @Sql(scripts = {"classpath:/sql/user.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"classpath:/sql/cleanup.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void testDeleteUserByValidUUID() {
        Mono<Void> expectedVoidResult = userRepository.deleteUserById(USER_UUID);

        StepVerifier.create(expectedVoidResult)
                .expectComplete()
                .verify();

        int isExist = checkIfExist(USER_UUID);
        assertEquals(0, isExist);
    }

}
