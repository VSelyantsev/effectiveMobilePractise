package ru.itis.kpfu.selyantsev.api;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.itis.kpfu.selyantsev.dto.UserRequest;

import java.util.UUID;

public interface UserApi {

    @PostMapping(value = "/registration")
    UUID userRegistration(@RequestBody UserRequest userRequest);
}
