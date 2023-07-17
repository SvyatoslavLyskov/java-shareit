package ru.practicum.shareit.item.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@AllArgsConstructor
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommentDto {
    Long id;
    @NotBlank(message = "Текст комментария отсутствует.")
    String text;
    String authorName;
    Long itemId;
    LocalDateTime created;
}