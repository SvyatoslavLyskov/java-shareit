package ru.practicum.shareit.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {
    @ExceptionHandler
    public ResponseEntity<String> handleNotFoundException(final NotFoundException e) {
        return sendError(e, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    public ResponseEntity<String> handleValidationException(final ValidationException e) {
        return sendError(e, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<String> handleUserAlreadyExistException(final UserAlreadyExistsException e) {
        return sendError(e, HttpStatus.CONFLICT);
    }

    public static ResponseEntity<String> sendError(Throwable e, HttpStatus httpStatus) {
        log.info("{} {}: {}", httpStatus.value(), httpStatus.getReasonPhrase(), e.getMessage(), e);
        return new ResponseEntity<>(e.getMessage(), httpStatus);
    }
}