package ru.practicum.shareit.user;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exceptions.UserAlreadyExistsException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.user.model.User;

import java.util.*;

@Repository
public class UserRepositoryImpl implements UserRepository {
    private final Map<Long, User> users = new HashMap<>();
    private Long userIdGenerator = 1L;

    @Override
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public Optional<User> getUserById(Long userId) {
        return Optional.ofNullable(users.get(userId));
    }

    @Override
    public User save(User user) {
        checkSameEmail(user);
        user.setId(userIdGenerator++);
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(User user, Long id) {
        checkUserExists(id);
        User oldUser = users.get(id);
        if (!Objects.equals(user.getEmail(), oldUser.getEmail())) {
            checkSameEmail(user);
        }
        if (user.getName() != null && !user.getName().isBlank()) {
            oldUser.setName(user.getName());
        }
        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            oldUser.setEmail(user.getEmail());
        }
        users.remove(id);
        users.put(id, oldUser);
        return oldUser;
    }

    private void checkUserExists(Long id) {
        if (!users.containsKey(id)) {
            throw new NotFoundException("Пользователь с запрашиваемым айди не зарегистрирован.");
        }
    }

    private void checkSameEmail(User user) {
        String email = user.getEmail();
        boolean match = users.values().stream().map(User::getEmail).anyMatch(mail -> Objects.equals(mail, email));
        if (match) {
            throw new UserAlreadyExistsException("Пользователь c такой почтой уже существует");
        }
    }

    @Override
    public void delete(Long userId) {
        users.remove(userId);
    }
}