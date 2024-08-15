package ru.itis.kpfu.selyantsev.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.itis.kpfu.selyantsev.dto.request.UserRequest;
import ru.itis.kpfu.selyantsev.dto.response.UserResponse;
import ru.itis.kpfu.selyantsev.exceptions.FailedExecuteOperation;
import ru.itis.kpfu.selyantsev.exceptions.UserNotFoundException;
import ru.itis.kpfu.selyantsev.model.User;
import ru.itis.kpfu.selyantsev.repository.CrudRepository;
import ru.itis.kpfu.selyantsev.service.TaskService;
import ru.itis.kpfu.selyantsev.service.UserService;
import ru.itis.kpfu.selyantsev.utils.mappers.UserMapper;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final CrudRepository<User, UUID> userRepository;
    private final TaskService taskService;
    private final UserMapper userMapper;

    @Override
    public Mono<UUID> create(UserRequest userRequest) {
        User mappedUser = userMapper.toEntity(userRequest);
        return userRepository.create(mappedUser)
                .flatMap(rowsInserted -> {
                    if (rowsInserted == 0) {
                        return Mono.error(new FailedExecuteOperation(mappedUser.getUserId()));
                    }
                    return Mono.just(mappedUser.getUserId());
                });
    }

    @Cacheable(value = "users", key = "#userId")
    @Override
    public Mono<UserResponse> findUserById(UUID userId) {
        Mono<User> entity = userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new UserNotFoundException(userId)));

        return entity.flatMap(user ->
                taskService.findAllTasksByUserId(userId).collectList()
                        .map(taskList -> {
                            UserResponse response = userMapper.toResponse(user);
                            response.setTaskList(taskList);
                            return response;
                        })
        );
    }
    
    @Cacheable(value = "allUsers")
    @Override
    public Flux<UserResponse> findAll(int page, int pageSize) {
        Flux<User> userFlux = userRepository.findAll(page, pageSize);

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

        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new UserNotFoundException(userId)))
                .flatMap(existingUser -> {
                    existingUser.setFirstName(mappedUser.getFirstName());
                    existingUser.setLastName(mappedUser.getLastName());

                    return userRepository.update(existingUser)
                            .flatMap(rowsUpdated -> {
                                if (rowsUpdated == 0) {
                                    return Mono.error(new FailedExecuteOperation(existingUser.getUserId()));
                                }

                                UserResponse response = userMapper.toResponse(existingUser);
                                return taskService.findAllTasksByUserId(userId).collectList()
                                        .map(taskResponses -> {
                                            response.setTaskList(taskResponses);
                                            return response;
                                        });
                            });
                });
    }

    @CacheEvict(value = "users", key = "#userId")
    @Override
    public Mono<Void> deleteUserById(UUID userId) {
        return userRepository.deleteById(userId)
                .switchIfEmpty(Mono.error(new UserNotFoundException(userId)))
                .then();
    }
}
