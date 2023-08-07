package ru.practicum.shareit.exceptions;

public class AlreadyExistsException extends IllegalArgumentException {

    public AlreadyExistsException(String message) {
        super(message);
    }
}