package ru.itis.kpfu.selyantsev.service.impl;

import org.springframework.stereotype.Service;
import ru.itis.kpfu.selyantsev.service.ConfirmationCodeStorageService;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class ConfirmationCodeStorageServiceImpl implements ConfirmationCodeStorageService {

    private static final Logger logger = Logger.getLogger(ConfirmationCodeStorageServiceImpl.class.getName());

    private final Map<String, String> confirmationCodeMap = new ConcurrentHashMap<>();

    @Override
    public void saveConfirmationCode(String email, String code) {
        logger.log(Level.INFO, "saving message in a local storage...");
        confirmationCodeMap.put(email, code);
    }

    @Override
    public Optional<String> getConfirmationCode(String email) {
        logger.log(Level.INFO, "getting message from local storage...");
        return Optional.of(confirmationCodeMap.get(email));
    }
}
