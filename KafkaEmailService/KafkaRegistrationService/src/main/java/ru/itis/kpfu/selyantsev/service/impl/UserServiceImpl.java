package ru.itis.kpfu.selyantsev.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.itis.kpfu.selyantsev.dto.UserRequest;
import ru.itis.kpfu.selyantsev.model.User;
import ru.itis.kpfu.selyantsev.repository.UserRepository;
import ru.itis.kpfu.selyantsev.service.ConfirmationCodeStorageService;
import ru.itis.kpfu.selyantsev.service.UserService;
import ru.itis.kpfu.selyantsev.util.ConfirmationCodeGenerator;
import ru.itis.kpfu.selyantsev.util.UserMapper;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final ConfirmationCodeProducer producer;
    private final ConfirmationCodeStorageService storageService;

    @Override
    public UUID create(UserRequest userRequest) {
        User mappedUser = userMapper.toEntity(userRequest);
        String confirmationCode = ConfirmationCodeGenerator.generateConfirmationCode();
        storageService.saveConfirmationCode(mappedUser.getEmail(), confirmationCode);
        producer.sendConfirmationCode(mappedUser.getEmail(), confirmationCode);
        return userRepository.save(mappedUser).getUserId();
    }
}
