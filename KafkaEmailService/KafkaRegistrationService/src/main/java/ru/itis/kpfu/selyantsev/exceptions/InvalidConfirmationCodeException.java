package ru.itis.kpfu.selyantsev.exceptions;

public class InvalidConfirmationCodeException extends RuntimeException {
    public InvalidConfirmationCodeException(String code, String email) {
        super(String.format("Ivalid confirmation code: %s for this emai: %s", code, email));
    }
}
