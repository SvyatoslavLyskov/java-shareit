package ru.practicum.shareit.booking;

import ru.practicum.shareit.exceptions.UnsupportedStateException;

public enum State {
    ALL,
    CURRENT,
    PAST,
    FUTURE,
    WAITING,
    REJECTED;

    public static void checkEnumExist(String state) {
        for (State available : State.values()) {
            if (available.name().equals(state)) {
                return;
            }
        }
        throw new UnsupportedStateException("Unknown state: UNSUPPORTED_STATUS");
    }
}

