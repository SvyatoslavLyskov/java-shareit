package ru.practicum.shareit.request;

import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoByOwner;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;

import static lombok.AccessLevel.PRIVATE;
import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest
@FieldDefaults(level = PRIVATE)
public class ItemRequestIntegrationTest {
    @Autowired
    EntityManager em;
    @Autowired
    ItemRequestService itemRequestService;
    User owner;
    User user;
    ItemRequest request;
    final LocalDateTime now = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        user = User.builder()
                .name("test1")
                .email("test1@mail.ru")
                .build();
        em.persist(user);
        owner = User.builder()
                .name("owner")
                .email("owner@mail.ru")
                .build();
        em.persist(owner);
        request = ItemRequest.builder()
                .description("table")
                .created(now.minusHours(3))
                .requester(user)
                .build();
        em.persist(request);
        ItemRequest request2 = ItemRequest.builder()
                .description("door")
                .created(now.minusHours(6))
                .requester(user)
                .build();
        em.persist(request2);
    }

    @AfterEach
    void clean() {
        em.clear();
    }

    @Test
    void createRequest() {
        ItemRequestDto requestDto = ItemRequestDto.builder()
                .description("door")
                .build();
        ItemRequestDto saved = itemRequestService.addRequest(requestDto, user.getId());
        ItemRequest result = em.find(ItemRequest.class, saved.getId());
        assertThat(result).isNotNull();
        assertThat(result.getRequester().getName()).isEqualTo(user.getName());
        assertThat(result.getRequester().getEmail()).isEqualTo(user.getEmail());
        assertThat(result.getDescription()).isEqualTo(requestDto.getDescription());
        assertThat(result.getId()).isEqualTo(saved.getId());
    }

    @Test
    void findAllUsersRequestsWithReplies() {
        List<ItemRequestDtoByOwner> list = itemRequestService.findAllUsersRequestsWithReplies(user.getId());
        assertThat(list).hasSize(2).isNotEmpty();
    }

    @Test
    void getAllRequests() {
        Long userId = owner.getId();
        List<ItemRequestDtoByOwner> list = itemRequestService.getAllRequests(userId, 0, 20);
        assertThat(list).hasSize(2).isNotEmpty();
    }

    @Test
    void getAllRequestsWithEmptyList() {
        Long userId = user.getId();
        List<ItemRequestDtoByOwner> list = itemRequestService.getAllRequests(userId, 0, 20);
        assertThat(list).isEmpty();
    }

    @Test
    void findById() {
        ItemRequestDtoByOwner itemRequestDto = itemRequestService.getRequestById(user.getId(), request.getId());
        ItemRequest result = em.find(ItemRequest.class, itemRequestDto.getId());
        assertThat(itemRequestDto.getCreated()).isEqualTo(now.minusHours(3));
        assertThat(itemRequestDto.getDescription()).isEqualTo(request.getDescription());
        assertThat(result.getRequester()).isEqualTo(user);
    }
}