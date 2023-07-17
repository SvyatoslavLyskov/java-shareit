package ru.practicum.shareit.user;

import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserService {
    List<UserDto> getAllUsers();

    UserDto getUserById(Long userId);

    UserDto saveUser(UserDto userDto);

    UserDto updateUser(UserDto userDto, Long userId);

    void deleteUser(Long userId);

    static void checkUserAvailability(UserRepository userRepository, long id) {
        if (!userRepository.existsById(id)) {
            throw new NotFoundException("Пользователь с запрашиваемым айди не зарегистрирован.");
        }
    }
}