package ru.itis.kpfu.selyantsev.api;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.itis.kpfu.selyantsev.dto.jwt.JwtRequest;
import ru.itis.kpfu.selyantsev.dto.jwt.JwtResponse;

public interface AuthApi {

    @GetMapping(value = "/login")
    ResponseEntity<JwtResponse> login(@RequestBody JwtRequest jwtRequest);

    @GetMapping(value = "/secured")
    @PreAuthorize("hasRole('USER')")
    ResponseEntity<String> securedEndpoint();

}
