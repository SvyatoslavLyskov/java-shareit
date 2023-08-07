package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import javax.validation.Valid;
import javax.validation.constraints.Min;

import static ru.practicum.shareit.booking.BookingClient.USER_ID;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping(path = "/requests")
public class ItemRequestController {
    private final ItemRequestClient itemRequestClient;

    @PostMapping
    public ResponseEntity<Object> createRequest(@RequestHeader(USER_ID) Long userId,
                                                @Valid @RequestBody ItemRequestDto requestDto) {
        return itemRequestClient.createRequest(userId, requestDto);
    }

    @GetMapping
    public ResponseEntity<Object> findAllWithReplies(@RequestHeader(USER_ID) Long userId) {
        return itemRequestClient.findAllWithReplies(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> findAll(@RequestHeader(USER_ID) Long userId,
                                          @RequestParam(defaultValue = "0", required = false) @Min(0) int from,
                                          @RequestParam(defaultValue = "10", required = false) @Min(1) int size) {
        return itemRequestClient.findAll(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> findByIdWithReplies(@RequestHeader(USER_ID) Long userId,
                                                      @PathVariable Long requestId) {
        return itemRequestClient.findByIdWithReplies(userId, requestId);
    }
}
