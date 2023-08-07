package ru.practicum.shareit.booking;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    Page<Booking> findByBookerIdOrderByStartDesc(Long bookerId, Pageable page);

    Page<Booking> findByBookerIdAndStatusOrderByStartDesc(Long bookerId, Status status, Pageable page);

    Page<Booking> findByBookerIdAndStartIsAfterOrderByStartDesc(Long bookerId, LocalDateTime start, Pageable page);

    Page<Booking> findByBookerIdAndEndIsBeforeOrderByStartDesc(Long bookerId, LocalDateTime start, Pageable page);

    Page<Booking> findByBookerIdAndEndIsAfterAndStartIsBeforeOrderByStartDesc(Long bookerId, LocalDateTime start,
                                                                              LocalDateTime end, Pageable page);

    Page<Booking> findByItemOwnerIdOrderByStartDesc(Long bookerId, Pageable page);

    Page<Booking> findByItemOwnerIdAndStatusOrderByStartDesc(Long bookerId, Status status, Pageable page);

    Page<Booking> findByItemOwnerIdAndStartIsAfterOrderByStartDesc(Long bookerId, LocalDateTime start, Pageable page);

    Page<Booking> findByItemOwnerIdAndEndIsBeforeOrderByStartDesc(Long bookerId, LocalDateTime start, Pageable page);

    Page<Booking> findByItemOwnerIdAndEndIsAfterAndStartIsBeforeOrderByStartDesc(Long bookerId, LocalDateTime start,
                                                                                 LocalDateTime end, Pageable page);

    Booking findTopByStatusNotLikeAndBookerIdAndItemIdOrderByEndAsc(Status status, Long authorId, Long itemId);

    List<Booking> findByItemIdAndItemOwnerIdAndStartIsAfterAndStatusIsNot(Long itemId, Long userId,
                                                                          LocalDateTime start, Status status);

    Page<Booking> findByItemIdAndItemOwnerIdAndStartIsAfterAndStatusIsNot(Long itemId, Long userId,
                                                                          LocalDateTime start, Status status,
                                                                          Pageable page);

    List<Booking> findByItemIdAndItemOwnerIdAndStartIsBeforeAndStatusIsNot(Long itemId, Long userId,
                                                                           LocalDateTime start, Status status);

    Page<Booking> findByItemIdAndItemOwnerIdAndStartIsBeforeAndStatusIsNot(Long itemId, Long userId,
                                                                           LocalDateTime start, Status status,
                                                                           Pageable page);
}