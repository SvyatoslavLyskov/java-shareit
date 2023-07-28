package ru.practicum.shareit.item;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
class ItemRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private ItemRepository itemRepository;
    private User user;
    private ItemRequest itemRequest;
    private Item item1;
    private Item item2;
    private Item item3;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .name("test")
                .email("test@mail.ru")
                .build();
        entityManager.persist(user);
        itemRequest = ItemRequest.builder()
                .requester(user)
                .description("table")
                .created(LocalDateTime.of(2029, 6, 6, 6, 6))
                .build();
        entityManager.persist(itemRequest);
        item1 = Item.builder()
                .name("table")
                .description("green")
                .owner(user)
                .available(true)
                .request(itemRequest)
                .build();
        entityManager.persist(item1);
        item2 = Item.builder()
                .name("chair")
                .description("cute table")
                .owner(user)
                .available(true)
                .request(itemRequest)
                .build();
        entityManager.persist(item2);
        item3 = Item.builder()
                .name("chair")
                .description("red")
                .owner(user)
                .available(false)
                .build();
        entityManager.persist(item3);
    }

    @Test
    void contextLoads() {
        Assertions.assertThat(entityManager).isNotNull();
    }

    @Test
    void succeedFindItemsByOwnerId() {
        List<Item> result = itemRepository.findItemsByOwnerId(user.getId());
        Assertions.assertThat(result).isNotNull().hasSize(3);
    }

    @Test
    void findItemsByWrongOwnerId() {
        List<Item> result = itemRepository.findItemsByOwnerId(8L);
        Assertions.assertThat(result).hasSize(0);
    }

    @Test
    void succeedFindItemsByOwnerIdPageable() {
        int pageNum = 0;
        int size = 1;
        Pageable page = PageRequest.of(pageNum, size);
        List<Item> items = itemRepository.findItemsByOwnerId(user.getId(), page);
        assertNotNull(items);
        assertEquals(1, items.size());
        assertEquals(item1.getId(), items.get(0).getId());
        pageNum = 1;
        page = PageRequest.of(pageNum, size);
        items = itemRepository.findItemsByOwnerId(user.getId(), page);
        assertNotNull(items);
        assertEquals(1, items.size());
    }

    @Test
    void succeedFindByRequestId() {
        List<Item> result = itemRepository.findByRequestId(itemRequest.getId());
        Assertions.assertThat(result).isNotNull().hasSize(2);
        Assertions.assertThat(result)
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(List.of(item1, item2));
    }

    @Test
    void succeedSearchItemByText() {
        List<Item> result =
                itemRepository.findByAvailableTrueAndDescriptionContainingIgnoreCaseOrNameContainingIgnoreCase(
                        "table", "table", Pageable.unpaged());
        Assertions.assertThat(result).isNotNull().hasSize(2);
        Assertions.assertThat(result)
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(List.of(item1, item2));
    }
}