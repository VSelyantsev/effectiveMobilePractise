package ru.itis.kpfu.selyantsev.exceptions.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.itis.kpfu.selyantsev.exceptions.AliasAlreadyExistException;
import ru.itis.kpfu.selyantsev.exceptions.ExpiredLink;
import ru.itis.kpfu.selyantsev.exceptions.NotFoundException;
import ru.itis.kpfu.selyantsev.exceptions.UrlNotFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AliasAlreadyExistException.class)
    public final ResponseEntity<ExceptionMessage> onAlreadyExistException(AliasAlreadyExistException exception) {
        ExceptionMessage actualMessage = getInfoAboutMessage(exception);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(actualMessage);
    }

    @ExceptionHandler(ExpiredLink.class)
    public final ResponseEntity<ExceptionMessage> onExpiredLinkException(ExpiredLink exception) {
        ExceptionMessage actualMessage = getInfoAboutMessage(exception);
        return ResponseEntity.status(HttpStatus.GONE).body(actualMessage);
    }

    @ExceptionHandler(value = {UrlNotFoundException.class})
    public final ResponseEntity<ExceptionMessage> handlerOnAllNotFoundException(NotFoundException exception) {
        ExceptionMessage actualMessage = getInfoAboutMessage(exception);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(actualMessage);
    }

    private ExceptionMessage getInfoAboutMessage(Exception exception) {
        return new ExceptionMessage(exception.getMessage(), exception.getClass().getName());
    }
}
