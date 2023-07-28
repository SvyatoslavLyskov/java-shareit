package ru.practicum.shareit.item;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoByOwner;
import ru.practicum.shareit.ObjectMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;

@SpringBootTest
@Transactional
public class ItemIntegrationTest {
    @Autowired
    private ItemService itemService;
    @Autowired
    private EntityManager entityManager;
    private User owner;
    private User booker;
    private Item item;
    private Item item2;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();
        owner = User.builder()
                .name("owner")
                .email("owner@mail.ru")
                .build();
        entityManager.persist(owner);
        booker = User.builder()
                .name("booker")
                .email("booker@mail.ru")
                .build();
        entityManager.persist(booker);
        User booker2 = User.builder()
                .name("booker2")
                .email("booker2@mail.ru")
                .build();
        entityManager.persist(booker2);
        item = Item.builder()
                .name("table")
                .description("green table")
                .available(true)
                .owner(owner)
                .build();
        entityManager.persist(item);
        item2 = Item.builder()
                .name("chair")
                .description("green chair")
                .available(true)
                .owner(owner)
                .build();
        entityManager.persist(item2);
        Booking booking = Booking.builder()
                .item(item)
                .booker(booker)
                .status(Status.APPROVED)
                .start(now.minusDays(5))
                .end(now.minusDays(4))
                .build();
        entityManager.persist(booking);
        Booking booking2 = Booking.builder()
                .item(item)
                .booker(booker2)
                .status(Status.APPROVED)
                .start(now.minusDays(3))
                .end(now.minusDays(2))
                .build();
        entityManager.persist(booking2);
        Comment comment2 = Comment.builder()
                .text("cute chair")
                .item(item)
                .created(now.minusDays(3))
                .author(booker2)
                .build();
        entityManager.persist(comment2);
    }

    @Test
    void saveItemTest() {
        Long userId = owner.getId();
        ItemDto newItem = ItemDto.builder()
                .name("bed")
                .description("white")
                .available(true)
                .build();
        ItemDto saved = itemService.saveItem(newItem, userId);
        Assertions.assertThat(saved).isNotNull()
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(newItem);
    }

    @Test
    void updateItemTest() {
        ItemDto updater = ObjectMapper.toItemDto(item);
        updater.setName("new table");
        updater.setDescription("new green table");
        updater.setAvailable(false);
        ItemDto updated = itemService.updateItem(updater, item.getId(), owner.getId());
        Assertions.assertThat(updated).isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(updater);
    }

    @Test
    void findByOwnerIdTest() {
        Long ownerId = owner.getId();
        Long bookerId = booker.getId();
        List<ItemDtoByOwner> returnedList = itemService.findByOwnerId(ownerId, 0, 2);
        Assertions.assertThat(returnedList)
                .isNotEmpty()
                .hasSize(2);
        Assertions.assertThat(returnedList.get(0).getName()).isEqualTo(item.getName());
        Assertions.assertThat(returnedList.get(1).getName()).isEqualTo(item2.getName());
        List<ItemDtoByOwner> returnedList2 = itemService.findByOwnerId(bookerId, 0, 1);
        Assertions.assertThat(returnedList2).isEmpty();
    }

    @Test
    void getUserItemByTextTest() {
        String search = "green";
        List<ItemDto> list = itemService.getUserItemByText(search);
        Assertions.assertThat(list).isNotEmpty()
                .hasSize(2)
                .extracting(ItemDto::getName)
                .containsExactlyInAnyOrder(item.getName(), item2.getName());
    }

    @Test
    void getUserItemByEmptyTextTest() {
        String search = "";
        List<ItemDto> list = itemService.getUserItemByText(search);
        Assertions.assertThat(list).isEmpty();
    }

    @Test
    void addCommentTest() {
        CommentDto commentNewDto = CommentDto.builder()
                .text("perfect table")
                .build();
        CommentDto addedComment = itemService.addComment(commentNewDto, booker.getId(), item.getId());
        Assertions.assertThat(addedComment).isNotNull()
                .hasFieldOrPropertyWithValue("text", commentNewDto.getText())
                .hasFieldOrPropertyWithValue("authorName", booker.getName());
    }
}