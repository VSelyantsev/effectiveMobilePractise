package ru.itis.kpfu.selyantsev.service;

import ru.itis.kpfu.selyantsev.dto.UserRequest;

import java.util.UUID;

public interface UserService {
    UUID create(UserRequest userRequest);
}
