package ru.itis.kpfu.selyantsev.junit.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.itis.kpfu.selyantsev.dto.request.TaskRequest;
import ru.itis.kpfu.selyantsev.dto.response.TaskResponse;
import ru.itis.kpfu.selyantsev.exceptions.FailedExecuteOperation;
import ru.itis.kpfu.selyantsev.exceptions.NotBelongUserTask;
import ru.itis.kpfu.selyantsev.exceptions.TaskNotFoundException;
import ru.itis.kpfu.selyantsev.model.Task;
import ru.itis.kpfu.selyantsev.repository.impl.TaskRepositoryImpl;
import ru.itis.kpfu.selyantsev.service.impl.TaskServiceImpl;
import ru.itis.kpfu.selyantsev.utils.mappers.TaskMapper;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskServiceJunitTest {

    @Mock
    private TaskRepositoryImpl taskRepository;

    @Mock
    private TaskMapper taskMapper;

    @InjectMocks
    private TaskServiceImpl taskService;

    private static final UUID USER_UUID = UUID.fromString("de6b0ea8-002e-42ad-be7b-95abf0efcd3d");
    private static final UUID TASK_UUID = UUID.fromString("9f5e37f7-a0aa-4435-920a-8ff3f1f7b05a");

    private static final TaskRequest TASK_REQUEST = TaskRequest.builder()
            .taskName("taskNameServiceTest")
            .build();

    private static final Task TASK_ENTITY = Task.builder()
            .taskId(TASK_UUID)
            .taskName(TASK_REQUEST.getTaskName())
            .userId(USER_UUID)
            .build();

    private static final TaskResponse TASK_RESPONSE = TaskResponse.builder()
            .taskId(TASK_ENTITY.getTaskId())
            .taskName(TASK_ENTITY.getTaskName())
            .userId(TASK_ENTITY.getUserId())
            .build();

    @Test
    void testCreate() {
        when(taskMapper.toEntity(TASK_REQUEST)).thenReturn(TASK_ENTITY);
        when(taskRepository.create(TASK_ENTITY)).thenReturn(Mono.just(1));

        Mono<UUID> result = taskService.create(USER_UUID, TASK_REQUEST);

        StepVerifier.create(result)
                .expectNext(TASK_UUID)
                .verifyComplete();
    }

    @Test
    void testCreate_shouldReturnError_WhenTaskCreationFailed() {
        when(taskMapper.toEntity(TASK_REQUEST)).thenReturn(TASK_ENTITY);
        when(taskRepository.create(TASK_ENTITY)).thenReturn(Mono.just(0));

        Mono<UUID> result = taskService.create(USER_UUID, TASK_REQUEST);

        StepVerifier.create(result)
                .expectErrorMatches(
                        throwable -> throwable instanceof FailedExecuteOperation &&
                        throwable.getMessage().contains(TASK_UUID.toString())
                ).verify();
    }

    @Test
    void testFindTaskWithValidUUID_shouldReturnValidResponse() {
        when(taskRepository.findById(TASK_UUID)).thenReturn(Mono.just(TASK_ENTITY));
        when(taskMapper.toResponse(TASK_ENTITY)).thenReturn(TASK_RESPONSE);

        Mono<TaskResponse> result = taskService.findTaskById(TASK_UUID);

        StepVerifier.create(result)
                .assertNext(taskResponse -> {
                    assertEquals(TASK_RESPONSE.getTaskId(), taskResponse.getTaskId());
                    assertEquals(TASK_RESPONSE.getTaskName(), taskResponse.getTaskName());
                    assertEquals(TASK_RESPONSE.getUserId(), taskResponse.getUserId());
                }).verifyComplete();
    }

    @Test
    void testFindTaskWithInvalidUUID_shouldReturnTaskNotFoundException() {
        when(taskRepository.findById(TASK_UUID)).thenReturn(Mono.empty());

        Mono<TaskResponse> result = taskService.findTaskById(TASK_UUID);

        StepVerifier.create(result)
                .expectErrorMatches(
                        throwable -> throwable instanceof TaskNotFoundException &&
                        throwable.getMessage().contains(TASK_UUID.toString())
                ).verify();
    }

    @Test
    void testFindAll() {
        when(taskRepository.findAll(0, 10)).thenReturn(Flux.just(TASK_ENTITY));
        when(taskMapper.toResponse(TASK_ENTITY)).thenReturn(TASK_RESPONSE);

        Flux<TaskResponse> result = taskService.findAll(0, 10);

        StepVerifier.create(result)
                .expectNext(TASK_RESPONSE)
                .verifyComplete();
    }

    @Test
    void testFindAll_whenResultIsEmpty() {
        when(taskRepository.findAll(0, 10)).thenReturn(Flux.empty());

        Flux<TaskResponse> result = taskService.findAll(0, 10);

        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void testFindAllTaskByUserUUID_shouldReturnValidResponse() {
        when(taskRepository.findAllTasksByUserId(USER_UUID)).thenReturn(Flux.just(TASK_ENTITY));
        when(taskMapper.toResponse(TASK_ENTITY)).thenReturn(TASK_RESPONSE);

        Flux<TaskResponse> result = taskService.findAllTasksByUserId(USER_UUID);

        StepVerifier.create(result)
                .assertNext(taskResponse -> {
                    assertEquals(TASK_RESPONSE.getTaskId(), taskResponse.getTaskId());
                    assertEquals(TASK_RESPONSE.getTaskName(), taskResponse.getTaskName());
                    assertEquals(TASK_RESPONSE.getUserId(), taskResponse.getUserId());
                }).verifyComplete();
    }

    @Test
    void testFindAllTasksByUserUUID_whenResultListIsEmpty() {
        when(taskRepository.findAllTasksByUserId(USER_UUID)).thenReturn(Flux.empty());

        Flux<TaskResponse> result = taskService.findAllTasksByUserId(USER_UUID);

        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void testUpdateTask_shouldReturnValidResponse() {
        when(taskRepository.findById(TASK_UUID)).thenReturn(Mono.just(TASK_ENTITY));
        when(taskRepository.update(TASK_ENTITY)).thenReturn(Mono.just(1));
        when(taskMapper.toResponse(TASK_ENTITY)).thenReturn(TASK_RESPONSE);

        Mono<TaskResponse> result = taskService.updateTaskCompletionStatus(USER_UUID, TASK_UUID, true);

        StepVerifier.create(result)
                .expectNext(TASK_RESPONSE)
                .verifyComplete();
    }

    @Test
    void testUpdateTask_shouldReturnTaskNotFoundException() {
        when(taskRepository.findById(TASK_UUID)).thenReturn(Mono.empty());

        Mono<TaskResponse> result = taskService.updateTaskCompletionStatus(USER_UUID, TASK_UUID, true);

        StepVerifier.create(result)
                .expectErrorMatches(
                        throwable -> throwable instanceof TaskNotFoundException &&
                        throwable.getMessage().contains(TASK_UUID.toString())
                ).verify();
    }

    @Test
    void testUpdateTask_shouldReturnNotBelongUserTask() {
        UUID taskId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID differentUserId = UUID.randomUUID();
        boolean isComplete = false;

        Task actualTask = Task.builder()
                .taskId(taskId)
                .taskName("testTaskName")
                .userId(differentUserId)
                .isComplete(isComplete)
                .build();

        when(taskRepository.findById(taskId)).thenReturn(Mono.just(actualTask));

        Mono<TaskResponse> result = taskService.updateTaskCompletionStatus(userId, taskId, isComplete);

        StepVerifier.create(result)
                .expectErrorMatches(
                        throwable -> throwable instanceof NotBelongUserTask &&
                        throwable.getMessage().contains(userId.toString())
                ).verify();
    }

    @Test
    void testUpdateTask_shouldReturnFailedExecutionException() {
        when(taskRepository.findById(TASK_UUID)).thenReturn(Mono.just(TASK_ENTITY));
        when(taskRepository.update(TASK_ENTITY)).thenReturn(Mono.just(0));

        Mono<TaskResponse> result = taskService.updateTaskCompletionStatus(USER_UUID, TASK_UUID, true);

        StepVerifier.create(result)
                .expectErrorMatches(
                        throwable -> throwable instanceof FailedExecuteOperation &&
                        throwable.getMessage().contains(TASK_UUID.toString())
                ).verify();
    }

    @Test
    void deleteTaskByValidUUID() {
        when(taskRepository.deleteById(TASK_UUID)).thenReturn(Mono.just(1));

        Mono<Void> result = taskService.deleteTaskById(TASK_UUID);

        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void deleteTaskByInvalidUUID_shouldReturnTaskNotFoundException() {
        when(taskRepository.deleteById(TASK_UUID)).thenReturn(Mono.empty());

        Mono<Void> result = taskService.deleteTaskById(TASK_UUID);

        StepVerifier.create(result)
                .expectErrorMatches(
                        throwable -> throwable instanceof TaskNotFoundException &&
                        throwable.getMessage().contains(TASK_UUID.toString())
                ).verify();
    }
}
