package ru.practicum.shareit.booking;

import static lombok.AccessLevel.PRIVATE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingOutputDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.UnsupportedStateException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

@ExtendWith(MockitoExtension.class)
@FieldDefaults(level = PRIVATE)
class BookingServiceTest {
    @Mock
    BookingRepository bookingRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    ItemRepository itemRepository;
    @InjectMocks
    BookingServiceImpl service;
    static final LocalDateTime NOW = LocalDateTime.now();
    User owner;
    User booker;
    Item item;
    Booking booking;
    BookingDto bookingToSave;

    @BeforeEach
    void started() {
        owner = User.builder()
                .id(1L)
                .name("nick")
                .email("nick@mail.ru")
                .build();
        booker = User.builder()
                .id(2L)
                .name("fred")
                .email("fred@mail.ru")
                .build();
        item = Item.builder()
                .id(4L)
                .name("table")
                .description("red")
                .available(true)
                .owner(owner)
                .build();
        booking = Booking.builder()
                .id(1L)
                .item(item)
                .booker(booker)
                .start(NOW.plusDays(20))
                .end(NOW.plusDays(30))
                .status(Status.WAITING)
                .build();
        bookingToSave = BookingDto.builder()
                .itemId(item.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .build();
    }

    @Test
    void succeedCreateBooking() {
        when(userRepository.existsById(anyLong())).thenReturn(true);
        when(itemRepository.findById(anyLong())).thenReturn(Optional.ofNullable(item));
        when(bookingRepository.save(any())).thenReturn(booking);
        BookingOutputDto bookingOutDto = service.createBooking(bookingToSave, booker.getId());
        assertNotNull(bookingOutDto);
        assertEquals(booking.getId(), bookingOutDto.getId());
    }

    @Test
    void createBookingFailByValidationPeriod() {
        bookingToSave.setStart(booking.getEnd());
        bookingToSave.setEnd(booking.getStart());
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> service.createBooking(bookingToSave, booker.getId()));
        assertEquals("Ошибка валидации", exception.getMessage());
    }

    @Test
    void createBookingFailByUserNotFound() {
        long userNotFoundId = 0L;
        String error = "Пользователь с запрашиваемым id не зарегистрирован.";
        when(userRepository.existsById(anyLong())).thenReturn(false);
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> service.createBooking(bookingToSave, userNotFoundId)
        );
        assertEquals(error, exception.getMessage());
        verify(bookingRepository, times(0)).save(any());
    }

    @Test
    void createBookingFailByItemNotFound() {
        long itemNotFoundId = 0L;
        bookingToSave.setItemId(itemNotFoundId);
        when(userRepository.existsById(anyLong())).thenReturn(true);
        when(itemRepository.findById(itemNotFoundId)).thenReturn(Optional.empty());
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> service.createBooking(bookingToSave, booker.getId()));
        assertEquals("Вещь с указанным id не найдена.", exception.getMessage());
    }

    @Test
    void createBookingFailByItemNotAvailable() {
        item.setAvailable(false);
        when(userRepository.existsById(anyLong())).thenReturn(true);
        when(itemRepository.findById(anyLong())).thenReturn(Optional.ofNullable(item));
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> service.createBooking(bookingToSave, booker.getId()));
        assertEquals("Ошибка валидации", exception.getMessage());
        item.setAvailable(true);
        item.setOwner(booker);
        when(userRepository.existsById(anyLong())).thenReturn(true);
        when(itemRepository.findById(anyLong())).thenReturn(Optional.ofNullable(item));
        NotFoundException e = assertThrows(
                NotFoundException.class,
                () -> service.createBooking(bookingToSave, booker.getId()));
        assertEquals("Владелец вещи не может её забронировать.", e.getMessage());
    }

    @Test
    void succeedConfirmBookingByOwner() {
        when(userRepository.existsById(anyLong())).thenReturn(true);
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.ofNullable(booking));
        when(bookingRepository.save(any())).thenReturn(booking);
        when(itemRepository.existsById(anyLong())).thenReturn(true);
        when(itemRepository.getReferenceById(anyLong())).thenReturn(item);
        BookingOutputDto bookingOutDto = service.confirmBookingByOwner(owner.getId(), booking.getId(), true);
        assertNotNull(bookingOutDto);
        assertEquals(booking.getId(), bookingOutDto.getId());
        assertEquals(Status.APPROVED, bookingOutDto.getStatus());
        bookingOutDto = service.confirmBookingByOwner(owner.getId(), booking.getId(), false);
        assertNotNull(bookingOutDto);
        assertEquals(booking.getId(), bookingOutDto.getId());
        assertEquals(Status.REJECTED, bookingOutDto.getStatus());
    }

    @Test
    void confirmBookingByOwnerFailByBookingNotFound() {
        when(userRepository.existsById(anyLong())).thenReturn(true);
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> service.confirmBookingByOwner(owner.getId(), booking.getId(), true)
        );
        assertEquals("Бронирование с указанным id не найдено.", exception.getMessage());
        verify(bookingRepository, times(0)).save(any());
    }

    @Test
    void confirmBookingByOwnerFailByNotValidParameter() {
        when(userRepository.existsById(anyLong())).thenReturn(true);
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));
        when(itemRepository.existsById(anyLong())).thenReturn(true);
        when(itemRepository.getReferenceById(anyLong())).thenReturn(item);
        booking.setStatus(Status.REJECTED);
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> service.confirmBookingByOwner(owner.getId(), booking.getId(), false)
        );
        assertEquals("Ошибка валидации", exception.getMessage());
        verify(bookingRepository, times(0)).save(any());
        booking.setStatus(Status.APPROVED);
        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> service.confirmBookingByOwner(owner.getId(), booking.getId(), true)
        );
        assertEquals("Ошибка валидации", ex.getMessage());
        verify(bookingRepository, times(0)).save(any());
    }

    @Test
    void succeedFindBookingById() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));
        BookingOutputDto bookingOutDto = service.findBookingById(owner.getId(), booker.getId());
        assertNotNull(bookingOutDto);
        assertEquals(booking.getId(), bookingOutDto.getId());
    }

    @Test
    void findBookingByIdFailByBookingNotFound() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.empty());
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> service.findBookingById(owner.getId(), booker.getId())
        );
        assertEquals("Бронирование с указанным id не найдено.", exception.getMessage());
    }

    @Test
    void findBookingByIdFailByItemNotAvailable() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.ofNullable(booking));
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> service.findBookingById(0L, booker.getId())
        );
        assertEquals("Получение данных доступно автору бронирования или владельцу вещи",
                exception.getMessage());
    }

    @Test
    void succeedFindAllUsersBooking() {
        int from = 0;
        int size = 1;
        long userId = booker.getId();
        when(userRepository.existsById(anyLong())).thenReturn(true);
        when(bookingRepository.findByBookerIdOrderByStartDesc(anyLong(), any())).thenReturn(
                new PageImpl<>(List.of(booking)));
        List<BookingOutputDto> bookingOutDto = service.findAllUsersBooking(userId, "ALL", from, size);
        assertNotNull(bookingOutDto);
        assertEquals(1, bookingOutDto.size());
        assertEquals(booking.getId(), bookingOutDto.get(0).getId());
        booking.setEnd(NOW.plusSeconds(120));
        when(bookingRepository.findByBookerIdAndEndIsAfterAndStartIsBeforeOrderByStartDesc(anyLong(), any(), any(),
                any())).thenReturn(new PageImpl<>(List.of(booking)));
        bookingOutDto = service.findAllUsersBooking(userId, "CURRENT", from, size);
        assertNotNull(bookingOutDto);
        assertEquals(1, bookingOutDto.size());
        when(bookingRepository.findByBookerIdAndEndIsBeforeOrderByStartDesc(anyLong(), any(),
                any())).thenReturn(new PageImpl<>(List.of(booking)));
        bookingOutDto = service.findAllUsersBooking(userId, "PAST", from, size);
        assertNotNull(bookingOutDto);
        assertEquals(1, bookingOutDto.size());
        booking.setStart(NOW.plusSeconds(60));
        when(bookingRepository.findByBookerIdAndStartIsAfterOrderByStartDesc(anyLong(), any(),
                any())).thenReturn(new PageImpl<>(List.of(booking)));
        bookingOutDto = service.findAllUsersBooking(userId, "FUTURE", from, size);
        assertNotNull(bookingOutDto);
        assertEquals(1, bookingOutDto.size());
        booking.setStatus(Status.WAITING);
        when(bookingRepository.findByBookerIdAndStatusOrderByStartDesc(anyLong(), any(),
                any())).thenReturn(new PageImpl<>(List.of(booking)));
        bookingOutDto = service.findAllUsersBooking(userId, "WAITING", from, size);
        assertNotNull(bookingOutDto);
        assertEquals(1, bookingOutDto.size());
        booking.setStatus(Status.REJECTED);
        when(bookingRepository.findByBookerIdAndStatusOrderByStartDesc(anyLong(), any(),
                any())).thenReturn(new PageImpl<>(List.of(booking)));
        bookingOutDto = service.findAllUsersBooking(userId, "REJECTED", from, size);
        assertNotNull(bookingOutDto);
        assertEquals(1, bookingOutDto.size());
    }

    @Test
    void findAllUsersBookingFailByUnsupportedStatus() {
        when(userRepository.existsById(anyLong())).thenReturn(true);
        UnsupportedStateException exception = assertThrows(
                UnsupportedStateException.class,
                () -> service.findAllUsersBooking(booker.getId(), "REJECTING", 0, 1)
        );
        assertEquals("Unknown state: UNSUPPORTED_STATUS",
                exception.getMessage());
    }

    @Test
    void succeedFindAllBookingsForItems() {
        int from = 0;
        int size = 1;
        long userId = booker.getId();
        when(userRepository.existsById(anyLong())).thenReturn(true);
        when(itemRepository.findItemsByOwnerId(anyLong())).thenReturn(List.of(item));
        when(bookingRepository.findByItemOwnerIdOrderByStartDesc(anyLong(), any())).thenReturn(new PageImpl<>(List.of(booking)));
        List<BookingOutputDto> bookingOutDto = service.findAllBookingsForItems(userId, "ALL", from, size);
        assertNotNull(bookingOutDto);
        assertEquals(1, bookingOutDto.size());
        assertEquals(booking.getId(), bookingOutDto.get(0).getId());
        booking.setEnd(NOW.plusSeconds(120));
        when(bookingRepository.findByItemOwnerIdAndEndIsAfterAndStartIsBeforeOrderByStartDesc(anyLong(), any(), any(),
                any())).thenReturn(new PageImpl<>(List.of(booking)));
        bookingOutDto = service.findAllBookingsForItems(userId, "CURRENT", from, size);
        assertNotNull(bookingOutDto);
        assertEquals(1, bookingOutDto.size());
        when(bookingRepository.findByItemOwnerIdAndEndIsBeforeOrderByStartDesc(anyLong(), any(),
                any())).thenReturn(new PageImpl<>(List.of(booking)));
        bookingOutDto = service.findAllBookingsForItems(userId, "PAST", from, size);
        assertNotNull(bookingOutDto);
        assertEquals(1, bookingOutDto.size());
        booking.setStart(NOW.plusSeconds(60));
        when(bookingRepository.findByItemOwnerIdAndStartIsAfterOrderByStartDesc(anyLong(), any(),
                any())).thenReturn(new PageImpl<>(List.of(booking)));
        bookingOutDto = service.findAllBookingsForItems(userId, "FUTURE", from, size);
        assertNotNull(bookingOutDto);
        assertEquals(1, bookingOutDto.size());
        booking.setStatus(Status.WAITING);
        when(bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(anyLong(), any(),
                any())).thenReturn(new PageImpl<>(List.of(booking)));
        bookingOutDto = service.findAllBookingsForItems(userId, "WAITING", from, size);
        assertNotNull(bookingOutDto);
        assertEquals(1, bookingOutDto.size());
        booking.setStatus(Status.REJECTED);
        when(bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(anyLong(), any(),
                any())).thenReturn(new PageImpl<>(List.of(booking)));
        bookingOutDto = service.findAllBookingsForItems(userId, "REJECTED", from, size);
        assertNotNull(bookingOutDto);
        assertEquals(1, bookingOutDto.size());
    }

    @Test
    void findAllBookingsForItemsFailByUserWithoutItems() {
        String error = "У пользователя нет вещей.";
        when(userRepository.existsById(anyLong())).thenReturn(true);
        when(itemRepository.findItemsByOwnerId(anyLong())).thenThrow(new NotFoundException(error));
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> service.findAllBookingsForItems(owner.getId(), "ALL", 0, 1)
        );
        assertEquals(error, exception.getMessage());
    }

    @Test
    void findAllBookingsForItemsFailByUnsupportedStatus() {
        when(userRepository.existsById(anyLong())).thenReturn(true);
        when(itemRepository.findItemsByOwnerId(anyLong())).thenReturn(List.of(item));
        UnsupportedStateException exception = assertThrows(
                UnsupportedStateException.class,
                () -> service.findAllBookingsForItems(booker.getId(), "REJECTING", 0, 1)
        );
        assertEquals("Unknown state: UNSUPPORTED_STATUS",
                exception.getMessage());
    }
}