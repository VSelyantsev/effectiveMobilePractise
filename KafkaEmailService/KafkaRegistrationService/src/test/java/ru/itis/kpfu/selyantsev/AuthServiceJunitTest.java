package ru.itis.kpfu.selyantsev;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.itis.kpfu.selyantsev.dto.jwt.JwtRequest;
import ru.itis.kpfu.selyantsev.dto.jwt.JwtResponse;
import ru.itis.kpfu.selyantsev.exceptions.ConfirmationCodeNotFoundException;
import ru.itis.kpfu.selyantsev.exceptions.InvalidConfirmationCodeException;
import ru.itis.kpfu.selyantsev.exceptions.UserNotFoundException;
import ru.itis.kpfu.selyantsev.filter.JwtProvider;
import ru.itis.kpfu.selyantsev.model.User;
import ru.itis.kpfu.selyantsev.repository.UserRepository;
import ru.itis.kpfu.selyantsev.service.ConfirmationCodeStorageService;
import ru.itis.kpfu.selyantsev.service.impl.AuthServiceImpl;

import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceJunitTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private ConfirmationCodeStorageService storageService;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void testLogin_Success() {
        JwtRequest request = new JwtRequest("testEmail@mail.ru", "1000");
        User user = User.builder()
                .userId(UUID.randomUUID())
                .email(request.getEmail())
                .build();
        String accessToken = "mockedAccessToken";

        when(userRepository.findUserByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(storageService.getConfirmationCode(user.getEmail())).thenReturn(Optional.of("1000"));
        when(jwtProvider.generateAccessToken(user)).thenReturn(accessToken);

        JwtResponse jwtResponse = authService.login(request);

        Assertions.assertEquals(jwtResponse.getAccessToken(), accessToken);

        verify(userRepository, times(1)).findUserByEmail(request.getEmail());
        verify(storageService, times(1)).getConfirmationCode(request.getEmail());
        verify(jwtProvider, times(1)).generateAccessToken(user);
    }

    @Test
    void testLogin_shouldThrowUserNotFoundException() {
        JwtRequest request = new JwtRequest("notExistEmail@mail.ru", "1000");

        when(userRepository.findUserByEmail(request.getEmail())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> authService.login(request));
        verify(userRepository, times(1)).findUserByEmail(request.getEmail());
    }

    @Test
    void testLogin_shouldThrowCodeNotFoundException() {
        JwtRequest request = new JwtRequest("testEmail@mail.ru", "1000");
        User user = User.builder()
                .userId(UUID.randomUUID())
                .email(request.getEmail())
                .build();

        when(userRepository.findUserByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(storageService.getConfirmationCode(user.getEmail())).thenReturn(Optional.empty());

        assertThrows(ConfirmationCodeNotFoundException.class, () -> authService.login(request));

        verify(userRepository, times(1)).findUserByEmail(request.getEmail());
        verify(storageService, times(1)).getConfirmationCode(request.getEmail());
    }

    @Test
    void testLogin_shouldThrowInvalidConfirmationCodeException() {
        JwtRequest request = new JwtRequest("testEmail@mail.ru", "1000");
        User user = User.builder()
                .userId(UUID.randomUUID())
                .email(request.getEmail())
                .build();

        when(userRepository.findUserByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(storageService.getConfirmationCode(user.getEmail())).thenReturn(Optional.of("1234"));

        assertThrows(InvalidConfirmationCodeException.class, () -> authService.login(request));

        verify(userRepository).findUserByEmail(request.getEmail());
        verify(storageService).getConfirmationCode(user.getEmail());
    }
}
