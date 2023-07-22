package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.ObjectMapper;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDtoByOwner;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ru.practicum.shareit.booking.BookingServiceImpl.checkUserAvailability;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    @Override
    public ItemRequestDto addRequest(ItemRequestDto itemRequestDto, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException(
                String.format("Пользователь с id=%d не найден", userId)));
        itemRequestDto.setRequester(user);
        ItemRequest request = ObjectMapper.toItemRequest(itemRequestDto);
        ItemRequest savedRequest = requestRepository.save(request);
        log.info("Добавлен запрос c id {}", savedRequest.getId());
        return ObjectMapper.toItemRequestDto(savedRequest);
    }

    @Override
    public List<ItemRequestDtoByOwner> getAllRequests(Long userId, int from, int size) {
        Sort sort = Sort.by(Sort.Direction.DESC, "created");
        PageRequest pageRequest = PageRequest.of(from / size, size, sort);
        List<ItemRequest> requests = requestRepository.findAllByRequesterIdNot(userId, pageRequest);
        return findAndMap(requests);
    }


    public List<ItemRequestDtoByOwner> findAllUsersRequestsWithReplies(Long userId) {
        checkUserAvailability(userRepository, userId);
        List<ItemRequest> requests = requestRepository.findAllByRequesterId(userId);
        return findAndMap(requests);
    }

    @Override
    public ItemRequestDtoByOwner getRequestById(Long userId, Long requestId) {
        checkUserAvailability(userRepository, userId);
        ItemRequest request = requestRepository.findById(requestId).orElseThrow(() -> new NotFoundException("Запрос не найден."));

        List<Item> items = itemRepository.findByRequestId(requestId);
        List<ItemDto> reply = new ArrayList<>();
        for (Item item : items) {
            reply.add(ObjectMapper.toItemDto(item));
        }
        return ObjectMapper.toItemRequestDtoByOwner(request, reply);
    }

    private List<ItemRequestDtoByOwner> findAndMap(List<ItemRequest> requests) {
        List<Long> requestIds = requests.stream()
                .map(ItemRequest::getId)
                .collect(Collectors.toList());
        List<Item> items = itemRepository.findByRequestIdIn(requestIds);
        Map<Long, List<Item>> itemMap = items.stream()
                .collect(Collectors.groupingBy(item -> item.getRequest().getId()));
        List<ItemRequestDtoByOwner> result = new ArrayList<>();
        for (ItemRequest itemRequest : requests) {
            List<ItemDto> itemDtoList = itemMap.getOrDefault(itemRequest.getId(), Collections.emptyList()).stream()
                    .map(ObjectMapper::toItemDto)
                    .collect(Collectors.toList());
            result.add(ObjectMapper.toItemRequestDtoByOwner(itemRequest, itemDtoList));
        }
        return result;
    }
}