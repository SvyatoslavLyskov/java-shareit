package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByBookerIdOrderByStartDesc(Long bookerId);

    List<Booking> findByBookerIdAndStatusOrderByStartDesc(Long bookerId, Status status);

    List<Booking> findByBookerIdAndStartIsAfterOrderByStartDesc(Long bookerId, LocalDateTime start);

    List<Booking> findByBookerIdAndEndIsBeforeOrderByStartDesc(Long bookerId, LocalDateTime start);

    List<Booking> findByBookerIdAndEndIsAfterAndStartIsBeforeOrderByStartDesc(Long bookerId, LocalDateTime start,
                                                                              LocalDateTime end);

    List<Booking> findByItemOwnerIdOrderByStartDesc(Long bookerId);

    List<Booking> findByItemOwnerIdAndStatusOrderByStartDesc(Long bookerId, Status status);

    List<Booking> findByItemOwnerIdAndStartIsAfterOrderByStartDesc(Long bookerId, LocalDateTime start);

    List<Booking> findByItemOwnerIdAndEndIsBeforeOrderByStartDesc(Long bookerId, LocalDateTime start);

    List<Booking> findByItemOwnerIdAndEndIsAfterAndStartIsBeforeOrderByStartDesc(Long bookerId, LocalDateTime start,
                                                                                 LocalDateTime end);

    Booking findTopByStatusNotLikeAndBookerIdAndItemIdOrderByEndAsc(Status status, Long authorId, Long itemId);

    List<Booking> findByItemIdAndItemOwnerIdAndStartIsAfterAndStatusIsNot(Long itemId, Long userId,
                                                                          LocalDateTime start, Status status);

    List<Booking> findByItemIdAndItemOwnerIdAndStartIsBeforeAndStatusIsNot(Long itemId, Long userId,
                                                                           LocalDateTime start, Status status);
}