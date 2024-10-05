package ru.itis.kpfu.selyantsev.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import ru.itis.kpfu.selyantsev.api.UserApi;
import ru.itis.kpfu.selyantsev.dto.UserRequest;
import ru.itis.kpfu.selyantsev.service.UserService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class UserController implements UserApi {

    private final UserService userService;

    @Override
    public UUID userRegistration(UserRequest userRequest) {
        return userService.create(userRequest);
    }
}
