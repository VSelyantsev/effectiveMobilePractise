package ru.itis.kpfu.selyantsev.exceptions;

public class ConfirmationCodeNotFoundException extends NotFoundException {
    public ConfirmationCodeNotFoundException(String email) {
        super(String.format("Confirmation Code for this email: %s NOT FOUND", email));
    }
}
