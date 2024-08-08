package ru.itis.kpfu.selyantsev.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.itis.kpfu.selyantsev.api.UserApi;
import ru.itis.kpfu.selyantsev.dto.request.UserRequest;
import ru.itis.kpfu.selyantsev.dto.response.UserResponse;
import ru.itis.kpfu.selyantsev.service.UserService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class UserController implements UserApi {

    private final UserService userService;

    @Override
    public Mono<UUID> createUser(UserRequest userRequest) {
        return userService.create(userRequest);
    }

    @Override
    public Mono<UserResponse> findUserById(UUID userId) {
        return userService.findUserById(userId);
    }

    @Override
    public Flux<UserResponse> findAll() {
        return userService.findAll();
    }

    @Override
    public Mono<UserResponse> updateUserById(UUID userId, UserRequest userRequest) {
        return userService.updateUserById(userId, userRequest);
    }

    @Override
    public Mono<Void> deleteUserById(UUID userId) {
        return userService.deleteUserById(userId);
    }
}
