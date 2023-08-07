package ru.practicum.shareit.booking.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingDto {
    long id;
    @NotNull(message = "Элемент бронирования отсутствует.")
    Long itemId;
    @NotNull(message = "Дата начала бронирования не указана.")
    @FutureOrPresent(message = "Дата начала бронирования указана в прошлом.")
    LocalDateTime start;
    @NotNull(message = "Дата окончания бронирования не указана.")
    @FutureOrPresent(message = "Дата окончания бронирования указана в прошлом.")
    LocalDateTime end;
    Long bookerId;
    Status status;
}
