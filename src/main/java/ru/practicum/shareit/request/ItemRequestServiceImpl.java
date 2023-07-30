package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.ObjectMapper;
import ru.practicum.shareit.SortType;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDtoByOwner;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.util.*;
import java.util.stream.Collectors;

import static ru.practicum.shareit.SortType.CREATED;

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
        Sort sort = CREATED.getSortValue();
        PageRequest pageRequest = PageRequest.of(from / size, size, sort);
        Page<ItemRequest> requests = requestRepository.findAllByRequesterIdNot(userId, pageRequest);
        log.info("Получены запросы пользовталея c id {}", userId);
        return findAndMap(requests.toList());
    }

    public List<ItemRequestDtoByOwner> findAllUsersRequestsWithReplies(Long userId) {
        checkUserAvailability(userId);
        List<ItemRequest> requests = requestRepository.findAllByRequesterId(userId);
        log.info("Получены запросы пользовталея c id {}", userId);
        return findAndMap(requests);
    }

    @Override
    public ItemRequestDtoByOwner getRequestById(Long userId, Long requestId) {
        checkUserAvailability(userId);
        ItemRequest request = requestRepository.findById(requestId).orElseThrow(() ->
                new NotFoundException("Запрос не найден."));
        List<ItemDto> reply = itemRepository.findByRequestId(requestId).stream()
                .map(ObjectMapper::toItemDto)
                .collect(Collectors.toList());
        log.info("Получен запрос c id {}", requestId);
        return ObjectMapper.toItemRequestDtoByOwner(request, reply);
    }

    private List<ItemRequestDtoByOwner> findAndMap(List<ItemRequest> requests) {
        List<Long> requestIds = requests.stream()
                .map(ItemRequest::getId)
                .collect(Collectors.toList());
        List<Item> items = itemRepository.findByRequestIdIn(requestIds);
        Map<Long, List<ItemDto>> itemMap = new HashMap<>();
        items.stream()
                .map(ObjectMapper::toItemDto)
                .forEach(itemDto -> itemMap.computeIfAbsent(itemDto.getRequestId(),
                        k -> new ArrayList<>()).add(itemDto));
        return requests.stream()
                .map(itemRequest -> ObjectMapper.toItemRequestDtoByOwner(itemRequest,
                        itemMap.getOrDefault(itemRequest.getId(), Collections.emptyList())))
                .collect(Collectors.toList());
    }

    public void checkUserAvailability(long id) {
        if (!userRepository.existsById(id)) {
            throw new NotFoundException("Пользователь с запрашиваемым id не зарегистрирован.");
        }
    }
}