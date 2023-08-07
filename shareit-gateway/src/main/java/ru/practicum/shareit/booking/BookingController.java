package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.State;
import ru.practicum.shareit.exception.UnsupportedStateException;
import ru.practicum.shareit.exception.ValidationException;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.time.LocalDateTime;

import static ru.practicum.shareit.booking.BookingClient.USER_ID;

@Controller
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Validated
public class BookingController {
    private final BookingClient bookingClient;

    @GetMapping
    public ResponseEntity<Object> getBookings(@RequestHeader(USER_ID) long userId,
                                              @RequestParam(name = "state", defaultValue = "all") String stateParam,
                                              @RequestParam(defaultValue = "0", required = false) @Min(0) Integer from,
                                              @RequestParam(defaultValue = "10", required = false) @Min(1) Integer size) {
        State state = State.from(stateParam)
                .orElseThrow(() -> new UnsupportedStateException("Unknown state: UNSUPPORTED_STATUS"));
        return bookingClient.getBookings(userId, state, from, size);
    }

    @PostMapping
    public ResponseEntity<Object> bookItem(@RequestHeader(USER_ID) long userId,
                                           @RequestBody @Valid BookingDto requestDto) {
        LocalDateTime end = requestDto.getEnd();
        LocalDateTime start = requestDto.getStart();
        if (!end.isAfter(start) || end.equals(start)) {
            throw new ValidationException("Дата окончания бронирования раньше даты начала или равней ей.");
        }
        return bookingClient.bookItem(userId, requestDto);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBooking(@RequestHeader(USER_ID) long userId,
                                             @PathVariable Long bookingId) {
        return bookingClient.getBooking(userId, bookingId);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> findAllBookingsForItems(@RequestHeader(USER_ID) Long userId,
                                                          @RequestParam(defaultValue = "ALL", required = false) String state,
                                                          @RequestParam(defaultValue = "0", required = false) @Min(0) int from,
                                                          @RequestParam(defaultValue = "10", required = false) @Min(1) int size) {
        State bookingState = State.from(state)
                .orElseThrow(() -> new UnsupportedStateException("Unknown state: UNSUPPORTED_STATUS"));
        return bookingClient.findAllBookingsForItems(userId, bookingState, from, size);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> confirmBookingByOwner(@RequestHeader(USER_ID) Long userId,
                                                        @PathVariable Long bookingId, @RequestParam Boolean approved) {
        return bookingClient.confirmBookingByOwner(userId, bookingId, approved);
    }
}