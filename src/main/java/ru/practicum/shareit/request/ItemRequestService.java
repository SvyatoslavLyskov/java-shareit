package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.ItemRequestDtoByOwner;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto addRequest(ItemRequestDto itemRequestDto, Long userId);

    List<ItemRequestDtoByOwner> findAllUsersRequestsWithReplies(Long userId);

    List<ItemRequestDtoByOwner> getAllRequests(Long userId, int from, int size);

    ItemRequestDtoByOwner getRequestById(Long userId, Long requestId);
}