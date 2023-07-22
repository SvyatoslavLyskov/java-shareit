package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingOutputDto;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping(path = "/bookings")
public class BookingController {
    private static final String HEADER = "X-Sharer-User-Id";
    private final BookingService bookingService;

    @PostMapping
    public BookingOutputDto createBooking(@RequestHeader(HEADER) Long userId,
                                          @Valid @RequestBody BookingDto dto) {
        return bookingService.createBooking(dto, userId);
    }

    @PatchMapping("/{bookingId}")
    public BookingOutputDto confirmBookingByOwner(@RequestHeader(HEADER) Long userId,
                                                  @PathVariable Long bookingId, @RequestParam Boolean approved) {
        return bookingService.confirmBookingByOwner(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingOutputDto findBookingById(@RequestHeader(HEADER) Long userId,
                                            @PathVariable Long bookingId) {
        return bookingService.findBookingById(userId, bookingId);
    }

    @GetMapping
    public List<BookingOutputDto> findAllUsersBooking(@RequestHeader(HEADER) Long userId,
                                                      @RequestParam(defaultValue = "ALL", required = false) String state,
                                                      @RequestParam(defaultValue = "0", required = false) @Min(0) int from,
                                                      @RequestParam(defaultValue = "10", required = false) @Min(1) int size) {
        return bookingService.findAllUsersBooking(userId, state, from, size);
    }

    @GetMapping("/owner")
    public List<BookingOutputDto> findAllBookingsForItems(@RequestHeader(HEADER) Long userId,
                                                          @RequestParam(defaultValue = "ALL", required = false) String state,
                                                          @RequestParam(defaultValue = "0", required = false) @Min(0) int from,
                                                          @RequestParam(defaultValue = "10", required = false) @Min(1) int size) {
        return bookingService.findAllBookingsForItems(userId, state, from, size);
    }
}
