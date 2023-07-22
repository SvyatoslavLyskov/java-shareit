package ru.practicum.shareit.item;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoByOwner;
import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Objects;

public interface ItemService {
    ItemDto saveItem(ItemDto itemDto, Long ownerId);

    ItemDto updateItem(ItemDto itemDto, Long itemId, Long ownerId);

    static void checkItemExists(ItemRepository itemRepository, long itemId) {
        if (!itemRepository.existsById(itemId)) {
            throw new NotFoundException("Вещь с указанным айди не найдена.");
        }
    }

    static void checkItemAccess(ItemRepository itemRepository, long userId, long itemId) {
        Item item = itemRepository.getReferenceById(itemId);
        Long ownerId = item.getOwner().getId();
        if (!Objects.equals(userId, ownerId)) {
            throw new NotFoundException("Редактировать может только владелец.");
        }
    }

    void deleteItem(long userId, long itemId);

    ItemDtoByOwner getItemById(Long userId, Long itemId);

    List<ItemDtoByOwner> findByOwnerId(Long userId);

    Page<Item> findByOwnerId(Long ownerId, Pageable pageable);

    List<ItemDto> getUserItemByText(String text);

    CommentDto addComment(CommentDto commentDto, long userId, long itemId);
}