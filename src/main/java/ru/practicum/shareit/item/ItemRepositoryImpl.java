package ru.practicum.shareit.item;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.*;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class ItemRepositoryImpl implements ItemRepository {
    private final Map<Long, Item> items = new HashMap<>();
    private Long itemIdGenerator = 1L;

    @Override
    public Item save(Item item) {
        item.setId(itemIdGenerator);
        items.put(itemIdGenerator++, item);
        return item;
    }

    @Override
    public Item update(Item item, Long itemId) {
        return items.put(itemId, item);
    }

    @Override
    public void delete(Long itemId) {
        items.remove(itemId);
    }

    @Override
    public List<Item> getAllItemsByUserId(Long userId) {
        return items.values()
                .stream()
                .filter(item -> item.getOwnerId().equals(userId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Item> getUserItemsByText(Long userId, String text) {
        if (StringUtils.isBlank(text)) {
            return Collections.emptyList();
        }
        return items.values()
                .stream()
                .filter(item -> BooleanUtils.isTrue(item.getAvailable())
                        && (StringUtils.containsAnyIgnoreCase(item.getName(), text)
                        || StringUtils.containsAnyIgnoreCase(item.getDescription(), text)))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Item> getItemById(Long itemId) {
        return Optional.ofNullable(items.get(itemId));
    }
}