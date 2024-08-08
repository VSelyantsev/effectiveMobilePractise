package ru.itis.kpfu.selyantsev.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.itis.kpfu.selyantsev.dto.request.UserRequest;
import ru.itis.kpfu.selyantsev.dto.response.TaskResponse;
import ru.itis.kpfu.selyantsev.dto.response.UserResponse;
import ru.itis.kpfu.selyantsev.exceptions.UserNotFoundException;
import ru.itis.kpfu.selyantsev.model.User;
import ru.itis.kpfu.selyantsev.repository.UserRepository;
import ru.itis.kpfu.selyantsev.service.TaskService;
import ru.itis.kpfu.selyantsev.service.UserService;
import ru.itis.kpfu.selyantsev.utils.mappers.UserMapper;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final TaskService taskService;
    private final UserMapper userMapper;

    @Override
    public Mono<UUID> create(UserRequest userRequest) {
        User mappedUser = userMapper.toEntity(userRequest);
        return userRepository.create(mappedUser);
    }

    @Cacheable(value = "users", key = "#userId")
    @Override
    public Mono<UserResponse> findUserById(UUID userId) {
        Mono<User> entity = userRepository.findUserById(userId)
                .onErrorMap(EmptyResultDataAccessException.class, e -> new UserNotFoundException(userId));

        Flux<TaskResponse> responseFlux = taskService.findAllTasksByUserId(userId);

        return entity.flatMap(user ->
                responseFlux.collectList()
                        .map(taskList -> {
                            UserResponse response = userMapper.toResponse(user);
                            response.setTaskList(taskList);
                            return response;
                        })
                );
    }

    @Cacheable(value = "allUsers")
    @Override
    public Flux<UserResponse> findAll() {
        Flux<User> userFlux = userRepository.findAll();

        return userFlux.flatMap(user ->
                taskService.findAllTasksByUserId(user.getUserId())
                        .collectList()
                        .map(taskList -> {
                            UserResponse userResponse = userMapper.toResponse(user);
                            userResponse.setTaskList(taskList);
                            return userResponse;
                        })
        );
    }

    @CachePut(value = "users", key = "#userId")
    @Override
    public Mono<UserResponse> updateUserById(UUID userId, UserRequest userRequest) {
        User mappedUser = userMapper.toEntity(userRequest);
        Mono<User> updatedUser = userRepository.updateUserById(userId, mappedUser)
                .onErrorMap(EmptyResultDataAccessException.class, exception -> new UserNotFoundException(userId));


        return updatedUser.flatMap(user ->
            taskService.findAllTasksByUserId(user.getUserId())
                    .collectList()
                    .map(taskList -> {
                        UserResponse response = userMapper.toResponse(user);
                        response.setTaskList(taskList);
                        return response;
                    })
        );
    }

    @CacheEvict(value = "users", key = "#userId")
    @Override
    public Mono<Void> deleteUserById(UUID userId) {
        return taskService.deleteUsersTaskById(userId)
                .then(userRepository.deleteUserById(userId));
    }
}
