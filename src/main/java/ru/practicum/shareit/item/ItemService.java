package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    ItemDto saveItem(ItemDto itemDto, Long ownerId);

    ItemDto updateItem(ItemDto itemDto, Long itemId, Long ownerId);

    void deleteItem(Long itemId);

    ItemDto getItemById(Long itemId);

    List<ItemDto> getUserItemsById(Long ownerId);

    List<ItemDto> getUserItemByText(Long ownerId, String text);
}