package ru.practicum.shareit.booking.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.booking.Status;

import java.time.LocalDateTime;

@AllArgsConstructor
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@Builder
public class BookingDto {
    Long id;
    Long itemId;
    LocalDateTime start;
    LocalDateTime end;
    Long bookerId;
    Status status;
}
