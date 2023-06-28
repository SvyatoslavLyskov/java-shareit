package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ItemMapper itemMapper;

    @Override
    public ItemDto saveItem(ItemDto itemDto, Long ownerId) {
        checkValidOwner(ownerId);
        Item item = itemMapper.toItem(itemDto);
        item.setOwnerId(ownerId);
        Item savedItem = itemRepository.save(item);
        return itemMapper.toItemDto(savedItem);
    }

    @Override
    public ItemDto updateItem(ItemDto itemDto, Long itemId, Long ownerId) {
        checkValidOwner(ownerId);
        Item item = itemRepository.getItemById(itemId)
                .orElseThrow(() -> new NotFoundException(String.format("Вещь с id=%d не найдена", itemId)));
        checkValidOwnerToItem(item, ownerId);
        Item updatedItem = itemRepository.update(fillItemFields(item, itemDto), itemId);
        return itemMapper.toItemDto(updatedItem);
    }

    @Override
    public void deleteItem(Long itemId) {
        itemRepository.delete(itemId);
    }

    @Override
    public ItemDto getItemById(Long itemId) {
        Item item = itemRepository.getItemById(itemId)
                .orElseThrow(() -> new NotFoundException(String.format("Вещь с id=%d не найдена", itemId)));
        return itemMapper.toItemDto(item);
    }

    @Override
    public List<ItemDto> getUserItemsById(Long ownerId) {
        return itemRepository.getAllItemsByUserId(ownerId)
                .stream()
                .map(itemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> getUserItemByText(Long ownerId, String text) {
        return itemRepository.getUserItemsByText(ownerId, text)
                .stream()
                .map(itemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    private Item fillItemFields(Item item, ItemDto itemDto) {
        String itemName = itemDto.getName();
        String itemDescription = itemDto.getDescription();
        Boolean itemIsAvailable = itemDto.getAvailable();

        Optional.ofNullable(itemName).ifPresent(item1 -> item.setName(itemName));
        Optional.ofNullable(itemDescription).ifPresent(item1 -> item.setDescription(itemDescription));
        Optional.ofNullable(itemIsAvailable).ifPresent(item1 -> item.setAvailable(itemIsAvailable));
        return item;
    }

    private void checkValidOwner(Long ownerId) {
        if (userRepository.getUserById(ownerId).isEmpty()) {
            throw new NotFoundException(String.format("Пользователь с id=%d не найден", ownerId));
        }
    }

    private void checkValidOwnerToItem(Item item, Long ownerId) {
        if (!item.getOwnerId().equals(ownerId)) {
            throw new NotFoundException("У данного пользователя нет этой вещи");
        }
    }
}