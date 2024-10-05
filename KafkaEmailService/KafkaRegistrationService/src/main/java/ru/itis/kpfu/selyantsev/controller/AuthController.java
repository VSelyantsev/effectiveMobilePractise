package ru.itis.kpfu.selyantsev.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import ru.itis.kpfu.selyantsev.api.AuthApi;
import ru.itis.kpfu.selyantsev.dto.jwt.JwtRequest;
import ru.itis.kpfu.selyantsev.dto.jwt.JwtResponse;
import ru.itis.kpfu.selyantsev.service.AuthService;

@RestController
@RequiredArgsConstructor
public class AuthController implements AuthApi {

    private final AuthService authService;

    @Override
    public ResponseEntity<JwtResponse> login(JwtRequest jwtRequest) {
        final JwtResponse token = authService.login(jwtRequest);
        return ResponseEntity.ok(token);
    }

    @Override
    public ResponseEntity<String> securedEndpoint() {
        return ResponseEntity.ok("This is secured endpoint!!!!");
    }
}
