package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDtoByOwner;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Validated
public class ItemRequestController {
    private static final String HEADER = "X-Sharer-User-Id";
    private final ItemRequestService itemRequestService;

    @PostMapping
    public ItemRequestDto addRequest(@RequestHeader(HEADER) Long userId,
                                     @Valid @RequestBody ItemRequestDto itemRequestDto) {
        return itemRequestService.addRequest(itemRequestDto, userId);
    }

    @GetMapping
    public List<ItemRequestDtoByOwner> findAllWithReplies(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemRequestService.findAllUsersRequestsWithReplies(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDtoByOwner> getAllRequests(@RequestHeader(HEADER) Long userId,
                                                      @RequestParam(defaultValue = "0", required = false)
                                                      @Min(0) int from,
                                                      @RequestParam(defaultValue = "10", required = false)
                                                      @Min(1) int size) {
        return itemRequestService.getAllRequests(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDtoByOwner getRequestById(@RequestHeader(HEADER) Long userId,
                                                @PathVariable Long requestId) {
        return itemRequestService.getRequestById(userId, requestId);
    }
}
