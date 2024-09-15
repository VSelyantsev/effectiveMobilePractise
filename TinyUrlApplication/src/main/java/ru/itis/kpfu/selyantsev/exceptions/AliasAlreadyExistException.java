package ru.itis.kpfu.selyantsev.exceptions;

public class AliasAlreadyExistException extends RuntimeException {
    public AliasAlreadyExistException(String alias) {
        super(String.format("Alias with this value: %s already exist!", alias));
    }
}
