package ru.practicum.shareit.booking;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByBookerIdOrderByStartDesc(Long bookerId, Pageable page);

    List<Booking> findByBookerIdAndStatusOrderByStartDesc(Long bookerId, Status status, Pageable page);

    List<Booking> findByBookerIdAndStartIsAfterOrderByStartDesc(Long bookerId, LocalDateTime start, Pageable page);

    List<Booking> findByBookerIdAndEndIsBeforeOrderByStartDesc(Long bookerId, LocalDateTime start, Pageable page);

    List<Booking> findByBookerIdAndEndIsAfterAndStartIsBeforeOrderByStartDesc(Long bookerId, LocalDateTime start,
                                                                              LocalDateTime end, Pageable page);

    List<Booking> findByItemOwnerIdOrderByStartDesc(Long bookerId, Pageable page);

    List<Booking> findByItemOwnerIdAndStatusOrderByStartDesc(Long bookerId, Status status, Pageable page);

    List<Booking> findByItemOwnerIdAndStartIsAfterOrderByStartDesc(Long bookerId, LocalDateTime start, Pageable page);

    List<Booking> findByItemOwnerIdAndEndIsBeforeOrderByStartDesc(Long bookerId, LocalDateTime start, Pageable page);

    List<Booking> findByItemOwnerIdAndEndIsAfterAndStartIsBeforeOrderByStartDesc(Long bookerId, LocalDateTime start,
                                                                                 LocalDateTime end, Pageable page);

    Booking findTopByStatusNotLikeAndBookerIdAndItemIdOrderByEndAsc(Status status, Long authorId, Long itemId);

    List<Booking> findByItemIdAndItemOwnerIdAndStartIsAfterAndStatusIsNot(Long itemId, Long userId,
                                                                          LocalDateTime start, Status status);

    List<Booking> findByItemIdAndItemOwnerIdAndStartIsAfterAndStatusIsNot(Long itemId, Long userId,
                                                                          LocalDateTime start, Status status,
                                                                          Pageable page);

    List<Booking> findByItemIdAndItemOwnerIdAndStartIsBeforeAndStatusIsNot(Long itemId, Long userId,
                                                                           LocalDateTime start, Status status);

    List<Booking> findByItemIdAndItemOwnerIdAndStartIsBeforeAndStatusIsNot(Long itemId, Long userId,
                                                                           LocalDateTime start, Status status,
                                                                           Pageable page);
}