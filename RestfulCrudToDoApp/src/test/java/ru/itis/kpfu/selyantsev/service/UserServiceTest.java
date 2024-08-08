package ru.itis.kpfu.selyantsev.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.itis.kpfu.selyantsev.configuration.ContainerConfiguration;
import ru.itis.kpfu.selyantsev.dto.request.UserRequest;
import ru.itis.kpfu.selyantsev.dto.response.TaskResponse;
import ru.itis.kpfu.selyantsev.dto.response.UserResponse;
import ru.itis.kpfu.selyantsev.exceptions.UserNotFoundException;
import ru.itis.kpfu.selyantsev.model.User;
import ru.itis.kpfu.selyantsev.repository.UserRepository;
import ru.itis.kpfu.selyantsev.service.impl.UserServiceImpl;
import ru.itis.kpfu.selyantsev.utils.mappers.UserMapper;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Container
    PostgreSQLContainer<?> container = ContainerConfiguration.getInstance();

    @Mock
    private UserMapper userMapper;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TaskService taskService;
    @InjectMocks
    private UserServiceImpl userService;

    private static final UUID USER_UUID = UUID.fromString("8004666d-0787-4a79-b338-ef88a6e1c6b0");
    private static final UUID NOT_EXIST_UUID = UUID.fromString("739fe59b-b8d3-47f3-9e8d-f2614e53d599");
    private static final UUID TASK_UUID = UUID.fromString("68481493-f6ab-4225-84b7-22f2bf928366");
    private static final UserRequest USER_REQUEST = UserRequest.builder()
            .firstName("ExampleFirstName")
            .lastName("ExampleLastName")
            .build();

    private static final User USER_ENTITY = User.builder()
            .userId(USER_UUID)
            .firstName(USER_REQUEST.getFirstName())
            .lastName(USER_REQUEST.getLastName())
            .build();

    private static final TaskResponse TASK_RESPONSE = TaskResponse.builder()
            .taskId(TASK_UUID)
            .taskName("ExampleTaskName")
            .isComplete(false)
            .userId(USER_UUID)
            .build();

    private static final UserResponse USER_RESPONSE = UserResponse.builder()
            .userId(USER_UUID)
            .firstName(USER_ENTITY.getFirstName())
            .lastName(USER_ENTITY.getLastName())
            .taskList(List.of(TASK_RESPONSE))
            .build();

    @Test
    void testCreateUser() {
        when(userMapper.toEntity(USER_REQUEST)).thenReturn(USER_ENTITY);
        when(userRepository.create(USER_ENTITY)).thenReturn(Mono.just(USER_UUID));

        Mono<UUID> actualResult = userService.create(USER_REQUEST);

        StepVerifier.create(actualResult)
                .expectNext(USER_UUID)
                .verifyComplete();
    }

    @Test
    void testFindUserByValidUUID() {
        when(userRepository.findUserById(USER_UUID)).thenReturn(Mono.just(USER_ENTITY));
        when(taskService.findAllTasksByUserId(USER_UUID)).thenReturn(Flux.just(TASK_RESPONSE));
        when(userMapper.toResponse(USER_ENTITY)).thenReturn(USER_RESPONSE);

        Mono<UserResponse> actualResponse = userService.findUserById(USER_UUID);

        StepVerifier.create(actualResponse)
                .expectNextMatches(response ->
                        response.getUserId().equals(USER_UUID) &&
                        response.getFirstName().equals(USER_ENTITY.getFirstName()) &&
                        response.getLastName().equals(USER_ENTITY.getLastName()) &&
                        response.getTaskList() != null &&
                        response.getTaskList().size() == 1
                ).verifyComplete();
    }

    @Test
    void testFindUserByNotExistUserUUID_shouldThrowUserNotFoundException() {
        // check in other repo classes
        when(userRepository.findUserById(NOT_EXIST_UUID))
                .thenReturn(Mono.error(new EmptyResultDataAccessException(1)));

        Mono<UserResponse> actualResult = userService.findUserById(NOT_EXIST_UUID);

        StepVerifier.create(actualResult)
                .expectError(UserNotFoundException.class)
                .verify();
    }

    @Test
    void testFindAll() {
        when(userRepository.findAll()).thenReturn(Flux.just(USER_ENTITY));
        when(taskService.findAllTasksByUserId(USER_UUID))
                .thenReturn(Flux.just(TASK_RESPONSE));

        when(userMapper.toResponse(USER_ENTITY))
                .thenReturn(USER_RESPONSE);

        Flux<UserResponse> actualResult = userService.findAll();

        StepVerifier.create(actualResult)
                .expectNextMatches(
                        userResponse -> userResponse.getUserId().equals(USER_UUID) &&
                        userResponse.getTaskList().size() == 1 &&
                        userResponse.getTaskList().get(0).getUserId().equals(USER_UUID)
                ).verifyComplete();
    }

    @Test
    void testUpdateUserByValidUUID() {
        when(userMapper.toEntity(USER_REQUEST))
                .thenReturn(USER_ENTITY);
        when(userRepository.updateUserById(eq(USER_UUID), any(User.class)))
                .thenReturn(Mono.just(USER_ENTITY));

        when(userMapper.toResponse(USER_ENTITY))
                .thenReturn(USER_RESPONSE);
        when(taskService.findAllTasksByUserId(USER_UUID))
                .thenReturn(Flux.just(TASK_RESPONSE));

        Mono<UserResponse> actualResponse = userService.updateUserById(USER_UUID, USER_REQUEST);

        StepVerifier.create(actualResponse)
                .expectNext(USER_RESPONSE)
                .verifyComplete();
    }

    @Test
    void testUpdateUserByNotExistUUID_shouldThrowUserNotFoundException() {
        when(userMapper.toEntity(USER_REQUEST)).thenReturn(USER_ENTITY);
        when(userRepository.updateUserById(NOT_EXIST_UUID, USER_ENTITY))
                .thenReturn(Mono.error(new EmptyResultDataAccessException(1)));


        Mono<UserResponse> actualResult = userService.updateUserById(NOT_EXIST_UUID, USER_REQUEST);

        StepVerifier.create(actualResult)
                .expectError(UserNotFoundException.class)
                .verify();
    }

    @Test
    void deleteUserByValidUUID() {
        when(taskService.deleteUsersTaskById(USER_UUID)).thenReturn(Mono.empty());
        when(userRepository.deleteUserById(USER_UUID)).thenReturn(Mono.empty());

        Mono<Void> actualMonoVoid = userService.deleteUserById(USER_UUID);

        StepVerifier.create(actualMonoVoid)
                .verifyComplete();

        verify(taskService).deleteUsersTaskById(USER_UUID);
        verify(userRepository).deleteUserById(USER_UUID);
    }
}
