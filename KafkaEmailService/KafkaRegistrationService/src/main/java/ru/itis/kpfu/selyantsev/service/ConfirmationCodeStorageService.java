package ru.itis.kpfu.selyantsev.service;

import java.util.Optional;

public interface ConfirmationCodeStorageService {
    void saveConfirmationCode(String email, String code);
    Optional<String> getConfirmationCode(String email);
}
