package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ItemRequestRepositoryTest {
    private static final Sort SORT = Sort.by(Sort.Direction.DESC, "created");
    @Autowired
    private TestEntityManager em;
    @Autowired
    private ItemRequestRepository itemRequestRepository;
    User user;
    ItemRequest itemRequest;
    User user2;
    ItemRequest itemRequest2;

    @BeforeEach
    void started() {
        user = User.builder()
                .name("test")
                .email("test1@mail.ru")
                .build();
        itemRequest = ItemRequest.builder()
                .requester(user)
                .description("table")
                .created(LocalDateTime.of(2029, 6, 6, 6, 6))
                .build();
        user2 = User.builder()
                .name("test2")
                .email("test2@mail.ru")
                .build();
        itemRequest2 = ItemRequest.builder()
                .requester(user2)
                .description("table")
                .created(LocalDateTime.of(2029, 6, 6, 6, 6))
                .build();
        em.persistAndFlush(user);
        em.persistAndFlush(itemRequest);
        em.persistAndFlush(user2);
        em.persistAndFlush(itemRequest2);
    }

    @Test
    @Transactional
    void succeedFindAllByRequesterId() {
        List<ItemRequest> result = itemRequestRepository.findAllByRequesterId(user.getId());
        assertThat(result).hasSize(1).usingRecursiveComparison().ignoringFields("id").isEqualTo(List.of(itemRequest));
    }

    @Test
    @Transactional
    void findAllByWrongRequesterIdEmptyList() {
        List<ItemRequest> result = itemRequestRepository.findAllByRequesterId(15L);
        assertThat(result).isEmpty();
    }

    @Test
    @Transactional
    void succeedFindAllByRequesterIdNot() {
        int pageNum = 0;
        int size = 1;
        PageRequest page = PageRequest.of(pageNum, size, SORT);
        List<ItemRequest> result = itemRequestRepository.findAllByRequesterIdNot(user.getId(), page);
        assertThat(result).hasSize(1).usingRecursiveComparison().ignoringFields("id").isEqualTo(List.of(itemRequest2));
    }
}