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
import ru.itis.kpfu.selyantsev.model.Task;
import ru.itis.kpfu.selyantsev.repository.impl.TaskRepositoryImpl;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.yml")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class TaskRepoImplTest {

    @Container
    PostgreSQLContainer<?> container = ContainerConfiguration.getInstance();

    @Autowired
    private TaskRepositoryImpl taskRepository;

    private static final UUID USER_UUID = UUID.fromString("9c186286-0ecb-422d-b19b-3e10c13221db");
    private static final UUID NOT_VALID_UUID = UUID.fromString("1ba9b3b4-8db8-4c57-a788-2fbec242dd0a");
    private static final UUID TASK_UUID = UUID.fromString("435cb8f4-470d-4343-bdff-5209002e3c8c");
    private static final UUID TASK_UUID_FOR_CREATION = UUID.fromString("435cb8f4-470d-4343-bdff-5209002e3c7f");
    private static final UUID NOT_VALID_TASK_UUID = UUID.fromString("84a639dd-c816-48ba-ba89-5666863d463f");

    private static final Task MAPPED_TASK = Task.builder()
            .taskId(TASK_UUID)
            .taskName("FirstTaskName")
            .isComplete(false)
            .userId(USER_UUID)
            .build();

    @Test
    void testCreate() {
        Task taskForCreate = Task.builder()
                .taskId(TASK_UUID_FOR_CREATION)
                .taskName("testTask")
                .isComplete(false)
                .userId(NOT_VALID_UUID)
                .build();

        Mono<Integer> result = taskRepository.create(taskForCreate);

        StepVerifier.create(result)
                .expectNext(1)
                .verifyComplete();

        Mono<Task> taskFromDb = taskRepository.findById(TASK_UUID_FOR_CREATION);

        StepVerifier.create(taskFromDb)
                .expectNextMatches(
                        task -> task.getTaskId().equals(taskForCreate.getTaskId()) &&
                        task.getTaskName().equals(taskForCreate.getTaskName()) &&
                        task.isComplete() == taskForCreate.isComplete() &&
                        task.getUserId().equals(taskForCreate.getUserId())
                ).verifyComplete();
    }

    @Test
    @Sql(scripts = {"classpath:/sql/task.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void testFindTaskByValidUUID() {
        Mono<Task> actualTask = taskRepository.findById(TASK_UUID);

        StepVerifier.create(actualTask)
                .expectNextMatches(
                        foundedTask -> foundedTask.getTaskId().equals(MAPPED_TASK.getTaskId()) &&
                                foundedTask.getTaskName().equals(MAPPED_TASK.getTaskName()) &&
                                foundedTask.isComplete() == MAPPED_TASK.isComplete() &&
                                foundedTask.getUserId().equals(MAPPED_TASK.getUserId())
                ).verifyComplete();
    }

    @Test
    @Sql(scripts = {"classpath:/sql/task.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void testFindAll() {
        Flux<Task> taskFlux = taskRepository.findAll(1, 10);

        StepVerifier.create(taskFlux.count())
                .assertNext(count -> assertTrue(count > 0))
                .verifyComplete();
    }

    @Test
    @Sql(scripts = {"classpath:/sql/task.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void testFindAllTaskByValidUserId() {
        Flux<Task> taskFlux = taskRepository.findAllTasksByUserId(USER_UUID);

        StepVerifier.create(taskFlux.count())
                .assertNext(count -> assertTrue(count > 0))
                .verifyComplete();
    }

    @Test
    @Sql(scripts = {"classpath:/sql/task.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void testUpdateTaskCompletionStatusWithRelatedUserUUID() {
        Task taskForCreate = Task.builder()
                .taskId(TASK_UUID_FOR_CREATION)
                .taskName("testTask")
                .isComplete(false)
                .userId(NOT_VALID_UUID)
                .build();

        Mono<Integer> dbTask = taskRepository.create(taskForCreate);

        StepVerifier.create(dbTask)
                .expectNext(1)
                .verifyComplete();

        taskForCreate.setComplete(true);

        Mono<Integer> updatedTask = taskRepository.update(taskForCreate);

        StepVerifier.create(updatedTask)
                .expectNext(1)
                .verifyComplete();

        Mono<Task> taskFromDb = taskRepository.findById(TASK_UUID_FOR_CREATION);

        StepVerifier.create(taskFromDb)
                .expectNextMatches(task -> task.isComplete() == taskForCreate.isComplete())
                .verifyComplete();
    }

    @Test
    @Sql(scripts = {"classpath:/sql/task.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void deleteTasksByRelatedValidUUID() {
        Mono<Integer> expectedVoidResult = taskRepository.deleteById(TASK_UUID);

        StepVerifier.create(expectedVoidResult)
                .expectNext(1)
                .verifyComplete();

        Mono<Task> taskFromDb = taskRepository.findById(TASK_UUID);

        StepVerifier.create(taskFromDb)
                .expectComplete()
                .verify();
    }
}
