package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoByOwner;
import ru.practicum.shareit.ObjectMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {
    private static final LocalDateTime NOW = LocalDateTime.now();
    @Mock
    ItemRepository repository;
    @Mock
    UserRepository userRepository;
    @Mock
    BookingRepository bookingRepository;
    @Mock
    CommentRepository commentRepository;
    @InjectMocks
    ItemServiceImpl service;
    private User owner;
    private User booker;
    private Item item;
    private Item item2;
    private Booking booking;
    private Comment comment;

    @BeforeEach
    void setup() {
        LocalDateTime start = NOW.minusDays(3);
        LocalDateTime end = NOW.minusDays(1);
        owner = User.builder().id(1L).name("test").email("test@mail.ru").build();
        booker = User.builder().id(2L).name("test2").email("test2@mail.ru").build();
        item = Item.builder().id(4L).name("table").description("red").available(true).owner(owner).build();
        item2 = Item.builder().id(5L).name("bed").description("white").available(true).owner(owner).build();
        booking = Booking.builder().id(1L).item(item).booker(booker).start(start).end(end)
                .status(Status.APPROVED).build();
        comment = Comment.builder().id(1L).author(booker).text("super").item(item).created(NOW).build();
    }

    @Test
    void succeedCreateItem() {
        when(repository.save(any())).thenReturn(item);
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        ItemDto itemDto = service.saveItem(ObjectMapper.toItemDto(item), owner.getId());
        assertNotNull(itemDto);
        assertEquals(item.getId(), itemDto.getId());
        verify(repository, times(1)).save(any());
    }

    @Test
    void createItemFailByUserNotFound() {
        long userNotFoundId = 0L;
        String error = "Пользователь не найден.";
        when(userRepository.findById(userNotFoundId)).thenThrow(new NotFoundException(error));
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> service.saveItem(ObjectMapper.toItemDto(item), userNotFoundId)
        );
        assertEquals(error, exception.getMessage());
        verify(repository, times(0)).save(any());
    }

    @Test
    void succeedUpdateItem() {
        long itemId = item.getId();
        long userId = owner.getId();
        Item updatedItem = Item.builder().id(itemId).name("chair").description("green").available(false).build();
        when(repository.findById(itemId)).thenReturn(Optional.ofNullable(item));
        when(userRepository.findById(userId)).thenReturn(Optional.ofNullable(owner));
        when(repository.save(any())).thenReturn(updatedItem);
        ItemDto itemDtoUpdated = service.updateItem(ObjectMapper.toItemDto(updatedItem), itemId, userId);
        assertNotNull(itemDtoUpdated);
        assertEquals(itemId, itemDtoUpdated.getId());
        assertEquals("chair", itemDtoUpdated.getName());
        assertEquals("green", itemDtoUpdated.getDescription());
        assertEquals(false, itemDtoUpdated.getAvailable());
    }

    @Test
    void updateItemFailByUserNotFound() {
        User user = User.builder().id(0L).name("test").email("test@mail.ru").build();
        long itemId = item.getId();
        long userId = user.getId();
        when(userRepository.findById(0L)).thenReturn(Optional.empty());
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> service.updateItem(ObjectMapper.toItemDto(item), itemId, userId));
        assertEquals("Пользователь не найден.", exception.getMessage());
    }

    @Test
    void updateItemFailByItemNotFound() {
        long userId = owner.getId();
        long itemNotFoundId = 0L;
        when(userRepository.findById(userId)).thenReturn(Optional.ofNullable(owner));
        when(repository.findById(itemNotFoundId)).thenReturn(Optional.empty());
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> service.updateItem(ObjectMapper.toItemDto(item), itemNotFoundId, userId));
        assertEquals("Вещь не найдена.", exception.getMessage());
    }

    @Test
    void updateItemBlancName() {
        long itemId = item.getId();
        long userId = owner.getId();
        Item updatedItem = Item.builder().id(itemId).description("black").available(false).build();
        when(repository.findById(itemId)).thenReturn(Optional.ofNullable(item));
        when(userRepository.findById(userId)).thenReturn(Optional.ofNullable(owner));
        when(repository.save(any())).thenReturn(item);
        ItemDto itemDtoUpdated = service.updateItem(ObjectMapper.toItemDto(updatedItem), itemId, userId);
        assertNotNull(itemDtoUpdated);
        assertEquals(itemId, itemDtoUpdated.getId());
        assertEquals("table", itemDtoUpdated.getName());
    }

    @Test
    void updateItemBlancDescription() {
        long itemId = item.getId();
        long userId = owner.getId();
        Item updatedItem = Item.builder().id(itemId).name("table").available(false).build();
        when(repository.findById(itemId)).thenReturn(Optional.ofNullable(item));
        when(userRepository.findById(userId)).thenReturn(Optional.ofNullable(owner));
        when(repository.save(any())).thenReturn(item);
        ItemDto itemDtoUpdated = service.updateItem(ObjectMapper.toItemDto(updatedItem), itemId, userId);
        assertNotNull(itemDtoUpdated);
        assertEquals(itemId, itemDtoUpdated.getId());
        assertEquals("red", itemDtoUpdated.getDescription());
    }

    @Test
    void updateItemBlancAvailable() {
        long itemId = item.getId();
        long userId = owner.getId();
        Item updatedItem = Item.builder().id(itemId).name("table").description("black").build();
        when(repository.findById(itemId)).thenReturn(Optional.ofNullable(item));
        when(userRepository.findById(userId)).thenReturn(Optional.ofNullable(owner));
        when(repository.save(any())).thenReturn(item);
        ItemDto itemDtoUpdated = service.updateItem(ObjectMapper.toItemDto(updatedItem), itemId, userId);
        assertNotNull(itemDtoUpdated);
        assertEquals(itemId, itemDtoUpdated.getId());
        assertEquals(true, itemDtoUpdated.getAvailable());
    }

    @Test
    void succeedFindItemById() {
        long ownerId = owner.getId();
        long itemId = item.getId();
        when(repository.findById(itemId)).thenReturn(Optional.of(item));
        when(bookingRepository.findByItemIdAndItemOwnerIdAndStartIsBeforeAndStatusIsNot(anyLong(), anyLong(), any(),
                any())).thenReturn(List.of(booking));
        when(bookingRepository.findByItemIdAndItemOwnerIdAndStartIsAfterAndStatusIsNot(anyLong(), anyLong(), any(),
                any())).thenReturn(List.of(booking));
        when(commentRepository.findByItemId(itemId)).thenReturn(List.of(comment));
        ItemDtoByOwner itemDto = service.getItemById(ownerId, itemId);
        assertNotNull(itemDto);
        assertEquals(itemId, itemDto.getId());
        assertEquals(comment.getId(), itemDto.getComments().get(0).getId());
    }

    @Test
    void findItemByIdFailItemNotFound() {
        long itemNotFoundId = 0L;
        String error = "Вещь не найдена.";
        when(repository.findById(itemNotFoundId)).thenThrow(new NotFoundException(error));
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> service.getItemById(owner.getId(), itemNotFoundId)
        );
        assertEquals(error, exception.getMessage());
    }

    @Test
    void findItemByIdWithoutComments() {
        long ownerId = owner.getId();
        long itemId = item2.getId();
        when(repository.findById(itemId)).thenReturn(Optional.of(item2));
        when(bookingRepository.findByItemIdAndItemOwnerIdAndStartIsBeforeAndStatusIsNot(anyLong(), anyLong(), any(),
                any())).thenReturn(List.of(booking));
        when(bookingRepository.findByItemIdAndItemOwnerIdAndStartIsAfterAndStatusIsNot(anyLong(), anyLong(), any(),
                any())).thenReturn(List.of(booking));
        when(commentRepository.findByItemId(itemId)).thenReturn(Collections.emptyList());
        ItemDtoByOwner itemDto = service.getItemById(ownerId, itemId);
        assertNotNull(itemDto);
        assertEquals(itemId, itemDto.getId());
        assertEquals(0, itemDto.getComments().size());
    }

    @Test
    void succeedAddComment() {
        long userId = booker.getId();
        long itemId = item.getId();
        when(userRepository.findById(userId)).thenReturn(Optional.of(owner));
        when(repository.findById(itemId)).thenReturn(Optional.of(item));
        when(bookingRepository
                .findTopByStatusNotLikeAndBookerIdAndItemIdOrderByEndAsc(any(), anyLong(), anyLong()))
                .thenReturn(booking);
        when(commentRepository.save(any())).thenReturn(comment);
        CommentDto commentDto = service.addComment(ObjectMapper.toCommentDto(comment), userId, itemId);
        assertNotNull(commentDto);
        assertEquals(comment.getId(), commentDto.getId());
        verify(commentRepository, times(1)).save(any());
    }

    @Test
    void addCommentFailItemNotFound() {
        long userId = booker.getId();
        long itemId = 0L;
        String error = "Вещь не найдена.";
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));
        when(repository.findById(anyLong())).thenThrow(new NotFoundException(error));
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> service.addComment(ObjectMapper.toCommentDto(comment), userId, itemId));
        assertEquals(error, exception.getMessage());
    }

    @Test
    void addCommentFailByUserNotFound() {
        long itemId = item.getId();
        long userId = 0L;
        String error = "Пользователь не найден.";
        when(userRepository.findById(anyLong())).thenThrow(new NotFoundException(error));
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> service.addComment(ObjectMapper.toCommentDto(comment), userId, itemId));
        assertEquals(error, exception.getMessage());
    }

    @Test
    void addCommentFailByBookingNotFound() {
        long itemId = item.getId();
        String error = String.format("Пользователь %s не пользовался вещью %s.", owner.getName(), item.getName());
        when(userRepository.findById(anyLong())).thenReturn(Optional.ofNullable(owner));
        when(repository.findById(anyLong())).thenReturn(Optional.of(item));
        when(bookingRepository
                .findTopByStatusNotLikeAndBookerIdAndItemIdOrderByEndAsc(any(), anyLong(), anyLong()))
                .thenThrow(new NotFoundException(error));
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> service.addComment(ObjectMapper.toCommentDto(comment), booker.getId(), itemId));
        assertEquals(error, exception.getMessage());
    }

    @Test
    void succeedRemoveItemById() {
        long userId = owner.getId();
        long itemId = item.getId();
        when(repository.getReferenceById(itemId)).thenReturn(item);
        when(repository.findById(itemId)).thenReturn(Optional.of(item));
        doNothing().when(repository).deleteById(itemId);
        service.deleteItem(userId, itemId);
        verify(repository, times(1)).findById(any());
        verify(repository, times(1)).deleteById(any());
    }

    @Test
    void removeItemByIdFailItemNotFound() {
        long itemNotFoundId = 0L;
        String error = "Вещь не найдена.";
        when(repository.findById(itemNotFoundId)).thenThrow(new NotFoundException(error));
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> service.deleteItem(owner.getId(), itemNotFoundId));
        assertEquals(error, exception.getMessage());
    }

    @Test
    void removeItemByIdFailAccess() {
        long itemId = item.getId();
        long notOwnerId = booker.getId();
        String error = "Редактировать может только владелец.";
        when(repository.findById(itemId)).thenReturn(Optional.of(item));
        when(repository.getReferenceById(itemId)).thenReturn(item);
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> service.deleteItem(notOwnerId, itemId));
        assertEquals(error, exception.getMessage());
    }

    @Test
    void succeedFindItemByDescription() {
        String text = "tAblE";
        when(repository.findByAvailableTrueAndDescriptionContainingIgnoreCaseOrNameContainingIgnoreCase(text, text))
                .thenReturn(List.of(item));
        List<ItemDto> itemDtos = service.getUserItemByText(text);
        assertNotNull(itemDtos);
        assertEquals(1, itemDtos.size());
        assertEquals(item.getId(), itemDtos.get(0).getId());
    }

    @Test
    void findItemByDescriptionShouldReturnEmptyList() {
        String text = "";
        List<ItemDto> itemDtos = service.getUserItemByText(text);
        assertNotNull(itemDtos);
        assertEquals(0, itemDtos.size());
    }

    @Test
    void succeedFindAll() {
        long userId = owner.getId();
        int from = 0;
        int size = 1;
        PageRequest page = PageRequest.of(from / size, size);
        when(commentRepository.findByItemIdIn(any())).thenReturn(List.of(comment));
        when(bookingRepository.findByItemIdAndItemOwnerIdAndStartIsBeforeAndStatusIsNot(anyLong(),
                anyLong(), any(), any(), any())).thenReturn(List.of(booking));
        when(bookingRepository.findByItemIdAndItemOwnerIdAndStartIsAfterAndStatusIsNot(anyLong(),
                anyLong(), any(), any(), any())).thenReturn(List.of(booking));
        when(repository.findItemsByOwnerId(userId, page)).thenReturn(List.of(item));
        List<ItemDtoByOwner> itemDtos = service.findByOwnerId(userId, from, size);
        assertNotNull(itemDtos);
        assertEquals(1, itemDtos.size());
        assertEquals(booking.getId(), itemDtos.get(0).getLastBooking().getId());
    }

    @Test
    void findAllEmptyList() {
        long userId = booker.getId();
        int from = 0;
        int size = 1;
        PageRequest page = PageRequest.of(from / size, size);
        when(repository.findItemsByOwnerId(userId, page)).thenReturn(Collections.emptyList());
        List<ItemDtoByOwner> itemDtos = service.findByOwnerId(userId, from, size);
        assertNotNull(itemDtos);
        assertEquals(0, itemDtos.size());
    }
}