package ru.itis.kpfu.selyantsev.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.itis.kpfu.selyantsev.dto.request.UserRequest;
import ru.itis.kpfu.selyantsev.dto.response.UserResponse;

import java.util.UUID;

public interface UserService {
    Mono<UUID> create(UserRequest userRequest);
    Mono<UserResponse> findUserByUserIdWithTasks(UUID userId);
    Flux<UserResponse> findAll(int page, int pageSize);
    Mono<UserResponse> updateUserById(UUID userId, UserRequest userRequest);
    Mono<Void> deleteUserById(UUID userId);
}
