package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.ObjectMapper;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingOutputDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static ru.practicum.shareit.booking.State.checkEnumExist;
import static ru.practicum.shareit.item.ItemService.checkItemAccess;
import static ru.practicum.shareit.item.ItemService.checkItemExists;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    public BookingOutputDto createBooking(BookingDto dto, Long userId) {
        Long itemId = dto.getItemId();
        validationBookingPeriod(dto);
        checkUserAvailability(userRepository, userId);
        Item item = itemRepository.findById(itemId).orElseThrow(() ->
                new NotFoundException("Вещь с указанным id не найдена."));
        if (!item.getAvailable()) {
            throw new ValidationException("Вещь уже забронирована.");
        }
        if (userId.equals(item.getOwner().getId())) {
            throw new NotFoundException("Владелец вещи не может её забронировать.");
        }
        dto.setStatus(Status.WAITING);
        Booking booking = ObjectMapper.toBooking(dto, item, userRepository.getReferenceById(userId));
        log.info("Добавлено бронирование c id {}", booking.getId());
        return ObjectMapper.toBookingOutputDto(bookingRepository.save(booking));
    }

    @Override
    public BookingOutputDto confirmBookingByOwner(Long userId, Long bookingId, boolean approved) {
        checkUserAvailability(userRepository, userId);
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() ->
                new NotFoundException("Бронирование с указанным id не найдено."));
        Long itemId = booking.getItem().getId();
        checkItemExists(itemRepository, itemId);
        checkItemAccess(itemRepository, userId, itemId);
        if (approved && booking.getStatus() == Status.APPROVED) {
            throw new ValidationException("Бронирование уже подтверждено.");
        }
        if (!approved && booking.getStatus() == Status.REJECTED) {
            throw new ValidationException("Бронирование уже отклонено.");
        }
        if (approved) {
            booking.setStatus(Status.APPROVED);
        } else {
            booking.setStatus(Status.REJECTED);
        }
        log.info("Подтверждено бронирование c id {}", booking.getId());
        return ObjectMapper.toBookingOutputDto(bookingRepository.save(booking));
    }

    @Override
    @Transactional(readOnly = true)
    public BookingOutputDto findBookingById(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() ->
                new NotFoundException("Бронирование с указанным id не найдено."));
        Long ownerId = booking.getItem().getOwner().getId();
        Long bookerId = booking.getBooker().getId();
        boolean checkOwnerOrBooker = ownerId.equals(userId) || bookerId.equals(userId);
        if (!checkOwnerOrBooker) {
            throw new NotFoundException("Получение данных доступно автору бронирования или владельцу вещи");
        }
        log.info("Найдено бронирование c id {}", booking.getId());
        return ObjectMapper.toBookingOutputDto(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingOutputDto> findAllUsersBooking(Long userId, String state) {
        checkUserAvailability(userRepository, userId);
        LocalDateTime start = LocalDateTime.now();
        List<Booking> bookings = new ArrayList<>();
        checkEnumExist(state);
        State bookingStatus = State.valueOf(state.toUpperCase());
        switch (bookingStatus) {
            case ALL:
                bookings = bookingRepository.findByBookerIdOrderByStartDesc(userId);
                break;
            case CURRENT:
                bookings = bookingRepository.findByBookerIdAndEndIsAfterAndStartIsBeforeOrderByStartDesc(userId, start, start);
                break;
            case PAST:
                bookings = bookingRepository.findByBookerIdAndEndIsBeforeOrderByStartDesc(userId, start);
                break;
            case FUTURE:
                bookings = bookingRepository.findByBookerIdAndStartIsAfterOrderByStartDesc(userId, start);
                break;
            case WAITING:
                bookings = bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, Status.WAITING);
                break;
            case REJECTED:
                bookings = bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, Status.REJECTED);
                break;
        }
        log.info("Список бронирований пользователя {}", bookings);
        return ObjectMapper.toBookingsOutputList(bookings);
    }

    @Override
    public List<BookingOutputDto> findAllBookingsForItems(Long userId, String state) {
        checkUserAvailability(userRepository, userId);
        if (itemRepository.findItemsByOwnerId(userId).isEmpty()) {
            throw new NotFoundException("У пользователя c id " + userId + " нет вещей.");
        }
        LocalDateTime start = LocalDateTime.now();
        List<Booking> bookings = new ArrayList<>();
        checkEnumExist(state);
        State bookingStatus = State.valueOf(state.toUpperCase());
        switch (bookingStatus) {
            case ALL:
                bookings = bookingRepository.findByItemOwnerIdOrderByStartDesc(userId);
                break;
            case CURRENT:
                bookings = bookingRepository.findByItemOwnerIdAndEndIsAfterAndStartIsBeforeOrderByStartDesc(userId, start, start);
                break;
            case PAST:
                bookings = bookingRepository.findByItemOwnerIdAndEndIsBeforeOrderByStartDesc(userId, start);
                break;
            case FUTURE:
                bookings = bookingRepository.findByItemOwnerIdAndStartIsAfterOrderByStartDesc(userId, start);
                break;
            case WAITING:
                bookings = bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(userId, Status.WAITING);
                break;
            case REJECTED:
                bookings = bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(userId, Status.REJECTED);
                break;
        }
        log.info("Cписок бронирований вещи {}", bookings);
        return ObjectMapper.toBookingsOutputList(bookings);
    }

    private void validationBookingPeriod(BookingDto booking) {
        LocalDateTime end = booking.getEnd();
        LocalDateTime start = booking.getStart();
        if (!end.isAfter(start) || end.equals(start)) {
            throw new ValidationException("Дата окончания бронирования раньше даты начала или равна.");
        }
    }

    private void checkUserAvailability(UserRepository userRepository, long id) {
        if (!userRepository.existsById(id)) {
            throw new NotFoundException("Пользователь с запрашиваемым id не зарегистрирован.");
        }
    }
}
