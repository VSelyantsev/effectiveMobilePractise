package ru.itis.kpfu.selyantsev.service.impl;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.itis.kpfu.selyantsev.dto.jwt.JwtRequest;
import ru.itis.kpfu.selyantsev.dto.jwt.JwtResponse;
import ru.itis.kpfu.selyantsev.exceptions.ConfirmationCodeNotFoundException;
import ru.itis.kpfu.selyantsev.exceptions.InvalidConfirmationCodeException;
import ru.itis.kpfu.selyantsev.exceptions.UserNotFoundException;
import ru.itis.kpfu.selyantsev.model.User;
import ru.itis.kpfu.selyantsev.repository.UserRepository;
import ru.itis.kpfu.selyantsev.service.AuthService;
import ru.itis.kpfu.selyantsev.service.ConfirmationCodeStorageService;
import ru.itis.kpfu.selyantsev.filter.JwtProvider;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final ConfirmationCodeStorageService storageService;

    @Override
    public JwtResponse login(@NonNull JwtRequest jwtRequest) {
        final User user = userRepository.findUserByEmail(jwtRequest.getEmail())
                .orElseThrow(() -> new UserNotFoundException(jwtRequest.getEmail()));

        String actualConfirmationCode = storageService.getConfirmationCode(user.getEmail())
                .orElseThrow(() -> new ConfirmationCodeNotFoundException(user.getEmail()));
        if (!actualConfirmationCode.equals(jwtRequest.getConfirmationCode())) {
            throw new InvalidConfirmationCodeException(jwtRequest.getConfirmationCode(), jwtRequest.getEmail());
        }
        final String accessToken = jwtProvider.generateAccessToken(user);
        return new JwtResponse(accessToken);
    }
}
