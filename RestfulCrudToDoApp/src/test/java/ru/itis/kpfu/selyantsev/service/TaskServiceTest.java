package ru.itis.kpfu.selyantsev.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.itis.kpfu.selyantsev.configuration.ContainerConfiguration;
import ru.itis.kpfu.selyantsev.dto.request.TaskRequest;
import ru.itis.kpfu.selyantsev.dto.response.TaskResponse;
import ru.itis.kpfu.selyantsev.exceptions.TaskNotFoundException;
import ru.itis.kpfu.selyantsev.model.Task;
import ru.itis.kpfu.selyantsev.repository.TaskRepository;
import ru.itis.kpfu.selyantsev.service.impl.TaskServiceImpl;
import ru.itis.kpfu.selyantsev.utils.mappers.TaskMapper;

import java.util.UUID;

import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {

    @Container
    PostgreSQLContainer<?> container = ContainerConfiguration.getInstance();

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskMapper taskMapper;

    @InjectMocks
    private TaskServiceImpl taskService;

    private static final UUID TASK_UUID = UUID.fromString("355da2f5-9992-4b8b-89e2-c9dad3c88828");
    private static final UUID NOT_EXIST_TASK_UUID = UUID.fromString("b29e23a3-f5fe-41da-90b5-677600b34c9b");
    private static final UUID USER_UUID = UUID.fromString("08514f4d-ea68-48f5-aa56-992aba4105be");

    private static final TaskRequest TASK_REQUEST = TaskRequest.builder()
            .taskName("ExampleTaskName")
            .build();

    private static final Task TASK_ENTITY = Task.builder()
            .taskId(TASK_UUID)
            .taskName(TASK_REQUEST.getTaskName())
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
    void testCreate() {
        when(taskMapper.toEntity(TASK_REQUEST)).thenReturn(TASK_ENTITY);
        when(taskRepository.create(TASK_UUID, TASK_ENTITY)).thenReturn(Mono.just(TASK_UUID));

        Mono<UUID> actualResult = taskService.create(TASK_UUID, TASK_REQUEST);

        StepVerifier.create(actualResult)
                .expectNext(TASK_UUID)
                .verifyComplete();
    }

    @Test
    void testFindTaskByValidUUID() {
        when(taskRepository.findTaskById(TASK_UUID)).thenReturn(Mono.just(TASK_ENTITY));
        when(taskMapper.toResponse(TASK_ENTITY)).thenReturn(TASK_RESPONSE);

        Mono<TaskResponse> actualResult = taskService.findTaskById(TASK_UUID);

        StepVerifier.create(actualResult)
                .expectNext(TASK_RESPONSE)
                .verifyComplete();
    }

    @Test
    void testFindTaskByNotExistUUID_shouldThrowTaskNotFoundException() {
        when(taskRepository.findTaskById(NOT_EXIST_TASK_UUID)).thenReturn(Mono.empty());

        Mono<TaskResponse> actualResponse = taskService.findTaskById(NOT_EXIST_TASK_UUID);

        StepVerifier.create(actualResponse)
                .expectError(TaskNotFoundException.class)
                .verify();
    }

    @Test
    void testFindAllTaskByValidUserUUID() {
        when(taskRepository.findAllTasksByUserId(USER_UUID)).thenReturn(Flux.just(TASK_ENTITY));
        when(taskMapper.toResponse(TASK_ENTITY)).thenReturn(TASK_RESPONSE);

        Flux<TaskResponse> actualResponse = taskService.findAllTasksByUserId(USER_UUID);

        StepVerifier.create(actualResponse)
                .expectNext(TASK_RESPONSE)
                .verifyComplete();
    }

    @Test
    void testUpdateTaskCompletion() {
        when(taskRepository.updateTaskCompletionStatus(USER_UUID, TASK_UUID, true))
                .thenReturn(Mono.just(TASK_ENTITY));

        when(taskMapper.toResponse(TASK_ENTITY)).thenReturn(TASK_RESPONSE);

        Mono<TaskResponse> actualResponse = taskService.updateTaskCompletionStatus(USER_UUID, TASK_UUID, true);

        StepVerifier.create(actualResponse)
                .expectNext(TASK_RESPONSE)
                .verifyComplete();
    }

    @Test
    void testDeleteUsersTaskById() {
        when(taskRepository.deleteUsersTaskById(USER_UUID)).thenReturn(Mono.empty());

        Mono<Void> actualMonoVoid = taskService.deleteUsersTaskById(USER_UUID);

        StepVerifier.create(actualMonoVoid)
                .verifyComplete();
    }
}
