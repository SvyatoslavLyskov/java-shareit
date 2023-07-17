package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.ObjectMapper;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(ObjectMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException((String.format("Пользователь с id=%d не найден", userId))));
        return ObjectMapper.toUserDto(user);

    }

    @Override
    public UserDto saveUser(UserDto userDto) {
        User user = ObjectMapper.toUser(userDto);
        return ObjectMapper.toUserDto(userRepository.save(user));
    }

    @Override
    public UserDto updateUser(UserDto userDto, Long userId) {
        User oldUser = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден."));
        if (StringUtils.isNotBlank(userDto.getName())) {
            oldUser.setName(userDto.getName());
        }
        if (StringUtils.isNotBlank(userDto.getEmail())) {
            oldUser.setEmail(userDto.getEmail());
        }
        User savedUser = userRepository.save(oldUser);
        return ObjectMapper.toUserDto(savedUser);
    }

    @Override
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    public void checkUserAvailability(UserRepository userRepository, long id) {
        if (!userRepository.existsById(id)) {
            throw new NotFoundException("Пользователь с запрашиваемым айди не зарегистрирован.");
        }
    }
}