package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.ObjectMapper;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoByOwner;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Transactional
@Service
@Slf4j
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final BookingRepository bookingRepository;
    private final ItemRequestRepository itemRequestRepository;

    @Override
    public ItemDto saveItem(ItemDto itemDto, Long ownerId) {
        User user = userRepository.findById(ownerId).orElseThrow(() ->
                new NotFoundException("Пользователь не найден."));
        Item item = ObjectMapper.toItem(itemDto, doRequests(itemDto));
        item.setOwner(user);
        itemRepository.save(item);
        log.info("Добавлена вещь c id {}", item.getId());
        return ObjectMapper.toItemDto(item);
    }

    @Override
    public ItemDto updateItem(ItemDto itemDto, Long itemId, Long ownerId) {
        User user = userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден."));
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена."));
        Item newItem = itemRepository.save(
                Optional.of(item)
                        .map(i -> {
                            i.setName(Optional.ofNullable(itemDto.getName()).orElse(i.getName()));
                            i.setDescription(Optional.ofNullable(itemDto.getDescription()).orElse(i.getDescription()));
                            i.setAvailable(Optional.ofNullable(itemDto.getAvailable()).orElse(i.getAvailable()));
                            i.setOwner(user);
                            i.setId(itemId);
                            return i;
                        })
                        .orElseThrow(() -> new RuntimeException("Ошибка при обновлении вещи."))
        );
        log.info("Обновлена вещь c id {}", newItem.getId());
        return ObjectMapper.toItemDto(newItem);
    }

    @Override
    public void deleteItem(long userId, long itemId) {
        itemRepository.findById(itemId).orElseThrow(() -> new NotFoundException("Вещь с не найдена."));
        ItemService.checkItemAccess(itemRepository, userId, itemId);
        itemRepository.deleteById(itemId);
        log.info("Удалена вещь с id {}", itemId);
    }

    @Override
    public ItemDtoByOwner getItemById(Long userId, Long itemId) {
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new NotFoundException("Вещь не найдена."));
        List<Comment> comments = commentRepository.findByItemId(itemId);
        LocalDateTime now = LocalDateTime.now();
        List<Booking> lastBookings = bookingRepository.findByItemIdAndItemOwnerIdAndStartIsBeforeAndStatusIsNot(itemId, userId,
                now, Status.REJECTED);
        List<Booking> nextBookings = bookingRepository.findByItemIdAndItemOwnerIdAndStartIsAfterAndStatusIsNot(itemId, userId,
                now, Status.REJECTED);
        log.info("Найдена вещь с id {}", itemId);
        return ObjectMapper.toItemDtoByOwner(item, lastBookings, nextBookings, comments);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemDtoByOwner> findByOwnerId(Long userId) {
        List<Item> userItems = itemRepository.findItemsByOwnerId(userId);
        List<Comment> comments = commentRepository.findByItemIdIn(userItems.stream()
                .map(Item::getId)
                .collect(Collectors.toList()));
        LocalDateTime now = LocalDateTime.now();
        log.info("Найдены вещи пользователя с id {}", userId);
        return userItems.stream()
                .map(item -> ObjectMapper.toItemDtoByOwner(item,
                        bookingRepository.findByItemIdAndItemOwnerIdAndStartIsBeforeAndStatusIsNot(item.getId(), userId, now,
                                Status.REJECTED),
                        bookingRepository.findByItemIdAndItemOwnerIdAndStartIsAfterAndStatusIsNot(item.getId(), userId, now,
                                Status.REJECTED),
                        comments))
                .collect(Collectors.toList());
    }

    @Override
    public Page<Item> findByOwnerId(Long ownerId, Pageable pageable) {
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemDto> getUserItemByText(String text) {
        if (text.isBlank()) {
            return Collections.emptyList();
        }
        log.info("Список вещей по запросу {}", text);
        return itemRepository.findByAvailableTrueAndDescriptionContainingIgnoreCaseOrNameContainingIgnoreCase(text, text)
                .stream()
                .map(ObjectMapper::toItemDto)
                .collect(Collectors.toList());
    }


    @Override
    public CommentDto addComment(CommentDto commentDto, long userId, long itemId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ValidationException("Пользователь не найден."));
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new ValidationException("Вещь не найдена."));
        Booking booking = bookingRepository
                .findTopByStatusNotLikeAndBookerIdAndItemIdOrderByEndAsc(Status.REJECTED, userId, itemId);
        Comment comment = ObjectMapper.toComment(commentDto, user, item);
        if (booking == null) {
            throw new ValidationException(String
                    .format("Пользователь %s не пользовался вещью %s.", user.getName(), item.getName()));
        }
        if (comment.getCreated().isBefore(booking.getEnd())) {
            throw new ValidationException("Завершите аренду для написания комментария.");
        }
        log.info("Добавлен комментарий c id {}", comment.getId());
        return ObjectMapper.toCommentDto(commentRepository.save(comment));
    }

    private ItemRequest doRequests(ItemDto dto) {
        ItemRequest requests;
        if (dto.getRequestId() != null) {
            requests = itemRequestRepository.findById(dto.getRequestId())
                    .orElseThrow(() -> new NotFoundException("Запрос не найден."));
        } else {
            requests = null;
        }
        return requests;
    }
}