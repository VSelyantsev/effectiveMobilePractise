package ru.itis.kpfu.selyantsev.exceptions.handlers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.itis.kpfu.selyantsev.exceptions.InvalidConfirmationCodeException;
import ru.itis.kpfu.selyantsev.exceptions.NotFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public final ResponseEntity<ExceptionMessage> onNotFoundException(NotFoundException exception) {
        ExceptionMessage actualMessage = getInfoAboutMessage(exception);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(actualMessage);
    }

    @ExceptionHandler(InvalidConfirmationCodeException.class)
    public final ResponseEntity<ExceptionMessage> onConflictException(InvalidConfirmationCodeException exception) {
        ExceptionMessage actualMessage = getInfoAboutMessage(exception);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(actualMessage);
    }

    private ExceptionMessage getInfoAboutMessage(Exception exception) {
        return new ExceptionMessage(exception.getMessage(), exception.getClass().getName());
    }
}
