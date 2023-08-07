package ru.practicum.shareit.exceptions;

public class ValidationException extends IllegalArgumentException {

    public ValidationException(String message) {
        super(message);
    }

    @Override
    public String getMessage() {
        return "Ошибка валидации";
    }
}