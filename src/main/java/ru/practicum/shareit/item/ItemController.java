package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoByOwner;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private static final String HEADER = "X-Sharer-User-Id";
    private final ItemService itemService;

    @PostMapping
    public ItemDto saveItem(@RequestHeader(HEADER) Long userId, @Valid @RequestBody ItemDto dto) {
        return itemService.saveItem(dto, userId);
    }

    @PatchMapping("{itemId}")
    public ItemDto updateItem(@RequestHeader(HEADER) Long userId, @RequestBody ItemDto dto,
                              @PathVariable Long itemId) {
        return itemService.updateItem(dto, itemId, userId);
    }

    @DeleteMapping("{itemId}")
    public void removeItemById(@RequestHeader(HEADER) Long userId,
                               @PathVariable Long itemId) {
        itemService.deleteItem(userId, itemId);
    }

    @GetMapping("{itemId}")
    public ItemDtoByOwner findItemById(@RequestHeader(HEADER) Long userId, @PathVariable Long itemId) {
        return itemService.getItemById(userId, itemId);
    }

    @GetMapping
    public List<ItemDtoByOwner> findAll(@RequestHeader(HEADER) Long userId) {
        return itemService.getUserItemsById(userId);
    }

    @GetMapping("/search")
    public List<ItemDto> findItemByDescription(@RequestParam(required = false) String text) {
        return itemService.getUserItemByText(text);
    }

    @PostMapping("{itemId}/comment")
    public CommentDto addComment(@RequestHeader(HEADER) Long userId, @Valid @RequestBody CommentDto commentDto,
                                 @PathVariable Long itemId) {
        return itemService.addComment(commentDto, userId, itemId);
    }
}