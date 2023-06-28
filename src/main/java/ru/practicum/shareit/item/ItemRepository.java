package ru.practicum.shareit.item;

import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

public interface ItemRepository {
    Item save(Item item);

    Item update(Item item, Long itemId);

    void delete(Long itemId);

    List<Item> getAllItemsByUserId(Long userId);

    List<Item> getUserItemsByText(Long userId, String text);

    Optional<Item> getItemById(Long itemId);
}
