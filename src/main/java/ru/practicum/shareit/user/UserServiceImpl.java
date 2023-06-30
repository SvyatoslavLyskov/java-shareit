package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.getAllUsers()
                .stream()
                .map(userMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto getUserById(Long userId) {
        User user = userRepository.getUserById(userId)
                .orElseThrow(() -> new NotFoundException((String.format("Пользователь с id=%d не найден", userId))));
        return userMapper.toUserDto(user);
    }

    @Override
    public UserDto saveUser(UserDto userDto) {
        User user = userMapper.toUser(userDto);
        return userMapper.toUserDto(userRepository.save(user));
    }

    @Override
    public UserDto updateUser(UserDto userDto, Long userId) {
        User user = userMapper.toUser(userDto);
        return userMapper.toUserDto(userRepository.update(user, userId));
    }

    @Override
    public void deleteUser(Long userId) {
        userRepository.delete(userId);
    }
}