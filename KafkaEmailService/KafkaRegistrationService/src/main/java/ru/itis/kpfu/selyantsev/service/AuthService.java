package ru.itis.kpfu.selyantsev.service;

import lombok.NonNull;
import ru.itis.kpfu.selyantsev.dto.jwt.JwtRequest;
import ru.itis.kpfu.selyantsev.dto.jwt.JwtResponse;

public interface AuthService {
    JwtResponse login(@NonNull JwtRequest jwtRequest);
}
