package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoByOwner;

import java.util.List;

import static lombok.AccessLevel.PRIVATE;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.shareit.booking.BookingControllerTest.HEADER;

@WebMvcTest(controllers = ItemRequestController.class)
@FieldDefaults(level = PRIVATE)
class ItemRequestControllerTest {
    @Autowired
    ObjectMapper mapper;
    @Autowired
    MockMvc mockMvc;
    @MockBean
    ItemRequestServiceImpl itemRequestService;
    final ItemRequestDto itemRequestDto = ItemRequestDto.builder()
            .id(1L)
            .description("доска")
            .build();
    final ItemRequestDtoByOwner itemRequestDtoByOwner = ItemRequestDtoByOwner.builder()
            .id(1L)
            .description("доска")
            .build();

    @Test
    void succeedCreateRequest() throws Exception {
        when(itemRequestService.addRequest(any(), anyLong())).thenReturn(itemRequestDto);
        mockMvc.perform(post("/requests")
                        .header(HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(itemRequestDto)))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.id").value(itemRequestDto.getId()),
                        jsonPath("$.description").value(itemRequestDto.getDescription())
                );
    }

    @Test
    void succeedFindAllWithDefaultParams() throws Exception {
        when(itemRequestService.getAllRequests(anyLong(), anyInt(),
                anyInt())).thenReturn(List.of(itemRequestDtoByOwner));
        mockMvc.perform(get("/requests/all")
                        .header(HEADER, 1L))
                .andExpect(status().isOk());
    }

    @Test
    void succeedFindAllWithReplies() throws Exception {
        when(itemRequestService.findAllUsersRequestsWithReplies(anyLong())).thenReturn(List.of(itemRequestDtoByOwner));
        mockMvc.perform(get("/requests")
                        .header(HEADER, 1L))
                .andExpect(status().isOk());
    }

    @Test
    void succeedFindById() throws Exception {
        when(itemRequestService.getRequestById(anyLong(), anyLong())).thenReturn(itemRequestDtoByOwner);
        mockMvc.perform(get("/requests/1")
                        .header(HEADER, 1L))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.id").value(itemRequestDto.getId()),
                        jsonPath("$.description").value(itemRequestDto.getDescription())
                );
    }
}