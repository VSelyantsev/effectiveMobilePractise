package ru.itis.kpfu.selyantsev.service.impl;

import org.springframework.stereotype.Service;
import ru.itis.kpfu.selyantsev.service.ConfirmationCodeStorageService;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ConfirmationCodeStorageServiceImpl implements ConfirmationCodeStorageService {

    private final Map<String, String> confirmationCodeMap = new ConcurrentHashMap<>();

    @Override
    public void saveConfirmationCode(String email, String code) {
        confirmationCodeMap.put(email, code);
    }

    @Override
    public Optional<String> getConfirmationCode(String email) {
        return Optional.of(confirmationCodeMap.get(email));
    }
}
