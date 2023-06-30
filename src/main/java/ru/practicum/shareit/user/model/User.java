package ru.practicum.shareit.user.model;

import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

@Builder
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {
    Long id;
    String name;
    @NotNull
    @Email
    String email;
}