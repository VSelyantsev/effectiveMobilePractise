package ru.itis.kpfu.selyantsev.exceptions;

public class UserNotFoundException extends NotFoundException {
    public UserNotFoundException(String email) {
        super(String.format("User with this email: %s IS NOT FOUND!", email));
    }
}
