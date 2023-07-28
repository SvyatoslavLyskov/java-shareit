package ru.practicum.shareit.user;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

@Transactional
@SpringBootTest
class UserIntegrationTest {
    @Autowired
    private EntityManager em;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;
    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        user1 = User.builder().name("test1").email("test1@mail.ru").build();
        userRepository.save(user1);
        user2 = User.builder().name("test2").email("test2@mail.ru").build();
        userRepository.save(user2);
        em.flush();
    }

    @Test
    void createUser() {
        UserDto userDto = UserDto.builder().name("owner").email("owner@mail.ru").build();
        UserDto saved = userService.saveUser(userDto);
        Assertions.assertThat(saved).isNotNull()
                .hasFieldOrPropertyWithValue("name", "owner")
                .hasFieldOrPropertyWithValue("email", "owner@mail.ru")
                .hasFieldOrProperty("id")
                .hasNoNullFieldsOrProperties();
    }

    @Test
    void findUserById() {
        Long userId = user1.getId();
        UserDto returned = userService.getUserById(userId);
        Assertions.assertThat(returned).isNotNull()
                .hasFieldOrPropertyWithValue("name", "test1")
                .hasFieldOrPropertyWithValue("email", "test1@mail.ru")
                .hasFieldOrPropertyWithValue("id", userId)
                .hasNoNullFieldsOrProperties();
    }

    @Test
    void deleteUser() {
        Long userId = user1.getId();
        userService.deleteUser(userId);
        Optional<User> user = userRepository.findById(userId);
        Assertions.assertThat(user).isNotPresent();
    }

    @Test
    void findAll() {
        List<UserDto> expectedList = List.of(
                UserDto.builder().id(user1.getId()).name(user1.getName()).email(user1.getEmail()).build(),
                UserDto.builder().id(user2.getId()).name(user2.getName()).email(user2.getEmail()).build());
        List<UserDto> actualList = userService.getAllUsers();
        Assertions.assertThat(actualList).usingRecursiveComparison().isEqualTo(expectedList);
    }
}
