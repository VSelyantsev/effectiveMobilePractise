package ru.itis.kpfu.selyantsev;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.itis.kpfu.selyantsev.dto.UserRequest;
import ru.itis.kpfu.selyantsev.model.User;
import ru.itis.kpfu.selyantsev.repository.UserRepository;
import ru.itis.kpfu.selyantsev.service.ConfirmationCodeStorageService;
import ru.itis.kpfu.selyantsev.service.impl.ConfirmationCodeProducer;
import ru.itis.kpfu.selyantsev.service.impl.UserServiceImpl;
import ru.itis.kpfu.selyantsev.util.UserMapper;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceJunitTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private ConfirmationCodeProducer codeProducer;

    @Mock
    ConfirmationCodeStorageService storageService;

    @InjectMocks
    private UserServiceImpl userService;

    private static final UUID VALID_UUID = UUID.fromString("a6571036-aa02-45a4-984b-9fc3cbc33e71");

    private static final UserRequest VALID_REQUEST = UserRequest.builder()
            .firstName("TestFirstName")
            .lastName("TestLastName")
            .email("testEmail@mail.ru")
            .build();

    private static final User VALID_ENTITY = User.builder()
            .userId(VALID_UUID)
            .firstName(VALID_REQUEST.getFirstName())
            .lastName(VALID_REQUEST.getLastName())
            .email(VALID_REQUEST.getEmail())
            .build();

    @Test
    void testCreate() {
        when(userMapper.toEntity(VALID_REQUEST)).thenReturn(VALID_ENTITY);
        when(userRepository.save(VALID_ENTITY)).thenReturn(VALID_ENTITY);

        UUID actualUUID = userService.create(VALID_REQUEST);

        verify(userMapper, times(1)).toEntity(VALID_REQUEST);
        verify(storageService, times(1)).saveConfirmationCode(anyString(), anyString());
        verify(codeProducer, times(1)).sendConfirmationCode(anyString(), anyString());
        verify(userRepository, times(1)).save(VALID_ENTITY);

        Assertions.assertEquals(VALID_UUID, actualUUID);
    }

}
