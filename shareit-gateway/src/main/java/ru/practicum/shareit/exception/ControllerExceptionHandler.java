package ru.practicum.shareit.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class ControllerExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> methodArgumentNotValidException(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors()
                .forEach((error) -> {
                    FieldError fieldError = ((FieldError) error);
                    String fieldName = fieldError.getField();
                    String errorMessage = error.getDefaultMessage();
                    errors.put(fieldName, errorMessage);
                    log.error(errorMessage + " " + fieldError.getRejectedValue(), fieldName);
                });
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(UnsupportedStateException.class)
    public ResponseEntity<?> unsupportedStateException(UnsupportedStateException e) {
        Map<String, String> errors = new HashMap<>();
        errors.put("error", e.getMessage());
        log.error(e.getStackTrace()[0].getMethodName() + ": " + e.getMessage());
        return ResponseEntity.status(500).body(errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> constraintViolationException(ConstraintViolationException e) {
        Map<String, String> errors = new HashMap<>();
        errors.put("Некорректное значение", e.getMessage());
        log.error(e.getStackTrace()[0].getMethodName() + ": " + e.getMessage());
        return ResponseEntity.status(400).body(errors);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<?> notValidIdException(ValidationException e) {
        Map<String, String> errors = new HashMap<>();
        errors.put("Некорректное значение", e.getMessage());
        log.error(e.getStackTrace()[0].getMethodName() + ": " + e.getMessage());
        return ResponseEntity.status(400).body(errors);
    }
}
