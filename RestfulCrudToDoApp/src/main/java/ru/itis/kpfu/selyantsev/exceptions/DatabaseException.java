package ru.itis.kpfu.selyantsev.exceptions;

public class DatabaseException extends RuntimeException {
    public DatabaseException(Throwable cause) {
        super("Error accessing the database: ", cause);
    }
}
