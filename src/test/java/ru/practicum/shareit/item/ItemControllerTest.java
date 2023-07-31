package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.FieldDefaults;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoByOwner;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static lombok.AccessLevel.PRIVATE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.shareit.booking.BookingControllerTest.HEADER;

@WebMvcTest(controllers = ItemController.class)
@FieldDefaults(level = PRIVATE)
class ItemControllerTest {
    @Autowired
    ObjectMapper mapper;
    @Autowired
    MockMvc mockMvc;
    @MockBean
    ItemServiceImpl itemService;
    final ItemDto itemDto = ItemDto.builder()
            .id(1L)
            .name("chair")
            .description("green")
            .available(true)
            .build();
    final BookingDto bookingDto = new BookingDto();
    final ItemDtoByOwner itemDtoByOwner = ItemDtoByOwner.builder()
            .id(2L)
            .name("chair")
            .description("green")
            .available(true)
            .lastBooking(bookingDto)
            .nextBooking(bookingDto)
            .comments(List.of(new CommentDto()))
            .build();
    final CommentDto commentDto = CommentDto.builder()
            .id(1L)
            .text("hi")
            .authorName("test")
            .itemId(2L)
            .build();

    @AfterEach
    void deleteUser() {
        itemService.deleteItem(anyLong(), anyLong());
    }

    @Test
    void succeedCreateItem() throws Exception {
        when(itemService.saveItem(any(), anyLong())).thenReturn(itemDto);

        mockMvc.perform(post("/items")
                        .header(HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(itemDto)))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.id", Matchers.is(itemDto.getId()), Long.class),
                        jsonPath("$.name", Matchers.is(itemDto.getName())),
                        jsonPath("$.description", Matchers.is(itemDto.getDescription())),
                        jsonPath("$.available", Matchers.is(itemDto.getAvailable()))
                );
    }

    @Test
    void createItemWithEmptyName() throws Exception {
        mockMvc.perform(post("/items")
                        .header(HEADER, "1")
                        .content("{\"description\": \"green\", \"available\": true, \"name\": \"\"}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createItemWithEmptyDescription() throws Exception {
        mockMvc.perform(post("/items")
                        .header(HEADER, 1L)
                        .content("{" +
                                "    \"name\": \"chair\"," +
                                "    \"available\": true" +
                                " }")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createItemWithEmptyAvailable() throws Exception {
        mockMvc.perform(post("/items")
                        .header(HEADER, 1L)
                        .content("{" +
                                "    \"description\": \"green\"," +
                                "    \"name\": \"chair\"" +
                                " }")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void succeedUpdateItem() throws Exception {
        when(itemService.updateItem(any(), anyLong(), anyLong())).thenReturn(itemDto);
        mockMvc.perform(patch("/items/1")
                        .header(HEADER, 1L)
                        .content("{" +
                                "    \"name\": \"chair\"," +
                                "    \"description\": \"green\"" +
                                " }")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.id", Matchers.is(itemDto.getId()), Long.class),
                        jsonPath("$.name", Matchers.is(itemDto.getName())),
                        jsonPath("$.description", Matchers.is(itemDto.getDescription())),
                        jsonPath("$.available", Matchers.is(itemDto.getAvailable()))
                );
    }

    @Test
    void succeedFindByIdItem() throws Exception {
        when(itemService.getItemById(anyLong(), anyLong())).thenReturn(itemDtoByOwner);
        mockMvc.perform(get("/items/2")
                        .header(HEADER, 1L))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.id", Matchers.is(itemDtoByOwner.getId()), Long.class),
                        jsonPath("$.name", Matchers.is(itemDtoByOwner.getName())),
                        jsonPath("$.description", Matchers.is(itemDtoByOwner.getDescription())),
                        jsonPath("$.available", Matchers.is(itemDtoByOwner.getAvailable()))
                );
    }

    @Test
    void findByIdItemWithoutSharerUserId() throws Exception {
        when(itemService.getItemById(anyLong(), anyLong())).thenReturn(itemDtoByOwner);
        mockMvc.perform(get("/items/2"))
                .andExpect(
                        status().isBadRequest());
    }

    @Test
    void succeedFindAllWithDefaultParam() throws Exception {
        List<ItemDtoByOwner> items = List.of(itemDtoByOwner, itemDtoByOwner, itemDtoByOwner);
        when(itemService.findByOwnerId(anyLong(), anyInt(), anyInt())).thenReturn(items);
        mockMvc.perform(get("/items")
                        .header(HEADER, 1L))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$[0].id", Matchers.is(itemDtoByOwner.getId()), Long.class),
                        jsonPath("$[0].name", Matchers.is(itemDtoByOwner.getName())),
                        jsonPath("$[0].description", Matchers.is(itemDtoByOwner.getDescription())),
                        jsonPath("$[0].available", Matchers.is(itemDtoByOwner.getAvailable())),
                        jsonPath("$.length()", Matchers.is(3))
                );
    }

    @Test
    void failFindAllWithWrongParam() throws Exception {
        mockMvc.perform(get("/items")
                        .header("HEADER", "1")
                        .param("from", "-1")
                        .param("size", "1"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/items")
                        .header("HEADER", "1")
                        .param("from", "5")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void succeedFindItemByDescriptionWithDefaultParams() throws Exception {
        when(itemService.getUserItemByText(any())).thenReturn(List.of(itemDto));
        mockMvc.perform(get("/items/search")
                        .header(HEADER, 1L))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$[0].id", Matchers.is(itemDto.getId()), Long.class),
                        jsonPath("$[0].name", Matchers.is(itemDto.getName())),
                        jsonPath("$[0].description", Matchers.is(itemDto.getDescription())),
                        jsonPath("$[0].available", Matchers.is(itemDto.getAvailable())),
                        jsonPath("$.length()", Matchers.is(1))
                );
    }

    @Test
    void succeedAddComment() throws Exception {
        when(itemService.addComment(any(), anyInt(), anyInt())).thenReturn(commentDto);
        mockMvc.perform(post("/items/1/comment")
                        .header(HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(commentDto)))
                .andExpect(status().isOk());
    }

    @Test
    void addCommentWithoutText() throws Exception {
        when(itemService.addComment(any(), anyInt(), anyInt())).thenReturn(commentDto);
        mockMvc.perform(post("/items/1/comment")
                        .header(HEADER, 1L)
                        .content("{" +
                                "    \"authorName\": \"test\"," +
                                "    \"itemId\": 2" +
                                " }")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addCommentWithoutSharerUserId() throws Exception {
        when(itemService.addComment(any(), anyInt(), anyInt())).thenReturn(commentDto);
        mockMvc.perform(post("/items/1/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(commentDto)))
                .andExpect(
                        status().isBadRequest()
                );
    }

    @Test
    void succeedDeleteItemById() throws Exception {
        mockMvc.perform(delete("/items/2")
                        .header(HEADER, 1L))
                .andExpect(status().isOk());
        verify(itemService, times(1))
                .deleteItem(anyLong(), anyLong());
    }

    @Test
    void deleteItemByIdWithoutSharerUserId() throws Exception {
        mockMvc.perform(delete("/items/2"))
                .andExpect(status().isBadRequest());
        verify(itemService, times(0))
                .deleteItem(anyLong(), anyLong());
    }
}