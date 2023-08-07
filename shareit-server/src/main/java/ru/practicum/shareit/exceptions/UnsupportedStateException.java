package ru.practicum.shareit.exceptions;

public class UnsupportedStateException extends RuntimeException {
    public UnsupportedStateException(String s) {
        super(s);
    }
}