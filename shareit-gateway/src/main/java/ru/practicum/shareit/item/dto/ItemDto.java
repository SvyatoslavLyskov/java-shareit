package ru.practicum.shareit.item.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@AllArgsConstructor
@Builder
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemDto {
    Long id;
    @NotBlank(message = "Наименование отсутствует.")
    String name;
    @NotBlank(message = "Описание пустое.")
    String description;
    @NotNull(message = "Доступность не указана.")
    Boolean available;
    Long requestId;
}
