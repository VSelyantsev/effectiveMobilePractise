package ru.itis.kpfu.selyantsev.exceptions;

import org.springframework.dao.DataAccessException;

public class DatabaseException extends DataAccessException {
    public DatabaseException(Throwable cause) {
        super("Error accessing the database: ", cause);
    }
}
