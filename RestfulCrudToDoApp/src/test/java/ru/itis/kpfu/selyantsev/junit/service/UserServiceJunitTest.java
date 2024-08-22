package ru.itis.kpfu.selyantsev.junit.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.itis.kpfu.selyantsev.dto.request.UserRequest;
import ru.itis.kpfu.selyantsev.dto.response.TaskResponse;
import ru.itis.kpfu.selyantsev.dto.response.UserResponse;
import ru.itis.kpfu.selyantsev.exceptions.FailedExecuteOperation;
import ru.itis.kpfu.selyantsev.exceptions.UserNotFoundException;
import ru.itis.kpfu.selyantsev.model.User;
import ru.itis.kpfu.selyantsev.repository.CrudRepository;
import ru.itis.kpfu.selyantsev.service.TaskService;
import ru.itis.kpfu.selyantsev.service.impl.UserServiceImpl;
import ru.itis.kpfu.selyantsev.utils.mappers.UserMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceJunitTest {

    @Mock
    private CrudRepository<User, UUID> userRepository;

    @Mock
    private TaskService taskService;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private static final UUID USER_UUID = UUID.fromString("de6b0ea8-002e-42ad-be7b-95abf0efcd3d");
    private static final UUID TASK_UUID = UUID.fromString("9f5e37f7-a0aa-4435-920a-8ff3f1f7b05a");

    private static final UserRequest USER_REQUEST = UserRequest.builder()
            .firstName("Vlad1")
            .lastName("LastName2")
            .build();

    private static final User USER_ENTITY = User.builder()
            .userId(USER_UUID)
            .firstName(USER_REQUEST.getFirstName())
            .lastName(USER_REQUEST.getLastName())
            .build();

    private static final UserResponse UPDATED_USER_RESPONSE = UserResponse.builder()
            .userId(UUID.randomUUID())
            .firstName("UpdatedName")
            .lastName("UpdatedLastName")
            .build();

    private static final TaskResponse TASK_RESPONSE = TaskResponse.builder()
            .taskId(TASK_UUID)
            .taskName("taskNameForUser")
            .userId(USER_UUID)
            .build();

    private static final UserResponse USER_RESPONSE = UserResponse.builder()
            .userId(USER_ENTITY.getUserId())
            .firstName(USER_ENTITY.getFirstName())
            .lastName(USER_ENTITY.getLastName())
            .taskList(Collections.singletonList(TASK_RESPONSE))
            .build();

    @Test
    void testCreate() {
        when(userMapper.toEntity(USER_REQUEST)).thenReturn(USER_ENTITY);
        when(userRepository.create(USER_ENTITY)).thenReturn(Mono.just(1));

        Mono<UUID> result = userService.create(USER_REQUEST);

        StepVerifier.create(result)
                .expectNext(USER_UUID)
                .verifyComplete();
    }

    @Test
    void testCreate_shouldReturnError_WhenUserCreationFailed() {
        when(userMapper.toEntity(USER_REQUEST)).thenReturn(USER_ENTITY);
        when(userRepository.create(USER_ENTITY)).thenReturn(Mono.just(0));

        Mono<UUID> result = userService.create(USER_REQUEST);

        StepVerifier.create(result)
                .expectErrorMatches(
                        throwable -> throwable instanceof FailedExecuteOperation &&
                        throwable.getMessage().contains(USER_UUID.toString()))
                .verify();
    }

    @Test
    void testFindUserWithValidUUID_shouldReturnValidResponse() {
        when(userRepository.findById(USER_UUID)).thenReturn(Mono.just(USER_ENTITY));
        when(taskService.findAllTasksByUserId(USER_UUID)).thenReturn(Flux.just(TASK_RESPONSE));
        when(userMapper.toResponse(USER_ENTITY)).thenReturn(USER_RESPONSE);

        Mono<UserResponse> result = userService.findUserById(USER_UUID);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(USER_RESPONSE.getUserId(), response.getUserId());
                    assertEquals(USER_RESPONSE.getFirstName(), response.getFirstName());
                    assertEquals(USER_RESPONSE.getLastName(), response.getLastName());
                    assertEquals(USER_RESPONSE.getTaskList(), response.getTaskList());
                }).verifyComplete();
    }

    @Test
    void testFindUser_shouldReturnError() {
        when(userRepository.findById(USER_UUID)).thenReturn(Mono.empty());

        Mono<UserResponse> result = userService.findUserById(USER_UUID);

        StepVerifier.create(result)
                .expectErrorMatches(
                        throwable -> throwable instanceof UserNotFoundException &&
                        throwable.getMessage().contains(USER_UUID.toString())
                ).verify();
    }

    @Test
    void testFindAll() {
        when(userRepository.findAll(0, 10)).thenReturn(Flux.just(USER_ENTITY));
        when(taskService.findAllTasksByUserId(USER_UUID)).thenReturn(Flux.just(TASK_RESPONSE));
        when(userMapper.toResponse(USER_ENTITY)).thenReturn(USER_RESPONSE);

        Flux<UserResponse> result = userService.findAll(0, 10);

        StepVerifier.create(result)
                .expectNext(USER_RESPONSE)
                .verifyComplete();
    }

    @Test
    void testFindAll_whenTasksListEmpty() {
        UserResponse responseWithEmptyTaskList = UserResponse.builder()
                .userId(USER_UUID)
                .firstName("EmptyTaskList")
                .lastName("EmptyTaskList")
                .taskList(new ArrayList<>())
                .build();

        when(userRepository.findAll(0, 10)).thenReturn(Flux.just(USER_ENTITY));
        when(taskService.findAllTasksByUserId(USER_UUID)).thenReturn(Flux.empty());
        when(userMapper.toResponse(USER_ENTITY)).thenReturn(responseWithEmptyTaskList);

        Flux<UserResponse> result = userService.findAll(0, 10);

        StepVerifier.create(result)
                .assertNext(userResponse -> assertTrue(userResponse.getTaskList().isEmpty()))
                .verifyComplete();
    }

    @Test
    void testUpdateUserById_shouldReturnUpdatedResponse() {
        when(userMapper.toEntity(USER_REQUEST)).thenReturn(USER_ENTITY);
        when(userRepository.findById(USER_UUID)).thenReturn(Mono.just(USER_ENTITY));
        when(userRepository.update(USER_ENTITY)).thenReturn(Mono.just(1));
        when(taskService.findAllTasksByUserId(USER_UUID)).thenReturn(Flux.just(TASK_RESPONSE));
        when(userMapper.toResponse(USER_ENTITY)).thenReturn(UPDATED_USER_RESPONSE);

        Mono<UserResponse> result = userService.updateUserById(USER_UUID, USER_REQUEST);

        StepVerifier.create(result)
                .expectNext(UPDATED_USER_RESPONSE)
                .verifyComplete();
    }

    @Test
    void testUpdateUserByInvalidUUID_shouldReturnUserNotFoundException() {
        when(userRepository.findById(USER_UUID)).thenReturn(Mono.empty());

        Mono<UserResponse> result = userService.updateUserById(USER_UUID, USER_REQUEST);

        StepVerifier.create(result)
                .expectErrorMatches(
                        throwable -> throwable instanceof UserNotFoundException &&
                        throwable.getMessage().contains(USER_UUID.toString())
                ).verify();
    }

    @Test
    void testUpdateUserByValidUUID_WhenUpdateFails_shouldReturnFailedExecutionException() {
        when(userMapper.toEntity(USER_REQUEST)).thenReturn(USER_ENTITY);
        when(userRepository.findById(USER_UUID)).thenReturn(Mono.just(USER_ENTITY));
        when(userRepository.update(USER_ENTITY)).thenReturn(Mono.just(0));

        Mono<UserResponse> result = userService.updateUserById(USER_UUID, USER_REQUEST);

        StepVerifier.create(result)
                .expectErrorMatches(
                        throwable -> throwable instanceof FailedExecuteOperation &&
                        throwable.getMessage().contains(USER_UUID.toString())
                ).verify();
    }

    @Test
    void testDeleteUserByValidUUID() {
        when(userRepository.deleteById(USER_UUID)).thenReturn(Mono.just(1));

        Mono<Void> result = userService.deleteUserById(USER_UUID);

        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void testDeleteUserByInvalidUUID_shouldReturnUserNotFoundException() {
        when(userRepository.deleteById(USER_UUID)).thenReturn(Mono.empty());

        Mono<Void> result = userService.deleteUserById(USER_UUID);

        StepVerifier.create(result)
                .expectErrorMatches(
                        throwable -> throwable instanceof UserNotFoundException &&
                        throwable.getMessage().contains(USER_UUID.toString())
                ).verify();
    }
}
