package ru.itis.kpfu.selyantsev.exceptions.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.itis.kpfu.selyantsev.exceptions.TaskNotFoundException;
import ru.itis.kpfu.selyantsev.exceptions.UserNotFoundException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ExceptionMessage> handleValidateException(MethodArgumentNotValidException exception) {
        Map<String, String> actualError = new HashMap<>();
        exception.getBindingResult().getFieldErrors()
                .forEach(fieldError -> {
                    actualError.put(
                            fieldError.getField(),
                            String.format("Rejected Value in field %s = %s. Description: %s",
                                    fieldError.getField(),
                                    fieldError.getRejectedValue(),
                                    fieldError.getDefaultMessage())
                    );
                });

        ExceptionMessage actualMessage = new ExceptionMessage(actualError);
        return ResponseEntity.badRequest().body(actualMessage);
    }

    @ExceptionHandler(value = {UserNotFoundException.class, TaskNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public final SimpleMessage handlerOnNotFoundExceptionMessage(Exception exception) {
        return getInfoAboutMessage(exception);
    }

    private SimpleMessage getInfoAboutMessage(Exception exception) {
        return new SimpleMessage(exception.getMessage(), exception.getClass().getName());
    }
}
