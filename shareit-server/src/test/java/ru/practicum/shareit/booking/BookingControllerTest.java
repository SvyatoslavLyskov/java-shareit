package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.FieldDefaults;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingOutputDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static lombok.AccessLevel.PRIVATE;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
@FieldDefaults(level = PRIVATE)
public class BookingControllerTest {
    public static final String HEADER = "X-Sharer-User-Id";
    @Autowired
    ObjectMapper mapper;
    @Autowired
    MockMvc mockMvc;
    @MockBean
    BookingServiceImpl bookingService;
    BookingDto bookingDto;
    BookingOutputDto bookingOutputDto;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();
        ItemDto itemDto = ItemDto.builder()
                .id(1L)
                .name("table")
                .description("black")
                .available(true)
                .build();
        UserDto userDto = UserDto.builder()
                .id(1L)
                .name("Nick")
                .email("nick@mail.ru")
                .build();
        bookingDto = BookingDto.builder()
                .id(2L)
                .itemId(1L)
                .start(now.plusYears(1))
                .end(now.plusYears(2))
                .bookerId(1L)
                .status(Status.NEW)
                .build();
        bookingOutputDto = BookingOutputDto.builder()
                .id(2L)
                .item(itemDto)
                .start(now.plusYears(1))
                .end(now.plusYears(2))
                .booker(userDto)
                .status(Status.NEW)
                .build();
    }

    @Test
    void succeedCreateBooking() throws Exception {
        when(bookingService.createBooking(any(), anyLong())).thenReturn(bookingOutputDto);
        mockMvc.perform(post("/bookings")
                        .header(HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(bookingDto)))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.id", Matchers.is(bookingOutputDto.getId()), Long.class),
                        jsonPath("$.item.id", Matchers.is(bookingOutputDto.getItem().getId()), Long.class),
                        jsonPath("$.booker.id", Matchers.is(bookingOutputDto.getBooker().getId()), Long.class)
                );
    }

    @Test
    void succeedConfirmBookingByOwner() throws Exception {
        bookingOutputDto.setStatus(Status.APPROVED);
        when(bookingService.confirmBookingByOwner(anyLong(), anyLong(), anyBoolean())).thenReturn(bookingOutputDto);
        mockMvc.perform(patch("/bookings/2")
                        .header(HEADER, 1L)
                        .param("approved", String.valueOf(true)))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.id", Matchers.is(bookingOutputDto.getId()), Long.class),
                        jsonPath("$.item.id", Matchers.is(bookingOutputDto.getItem().getId()), Long.class),
                        jsonPath("$.status", Matchers.is(bookingOutputDto.getStatus().toString()))
                );
        bookingOutputDto.setStatus(Status.REJECTED);
        when(bookingService.confirmBookingByOwner(anyLong(), anyLong(), anyBoolean())).thenReturn(bookingOutputDto);
        mockMvc.perform(patch("/bookings/2")
                        .header(HEADER, 1L)
                        .param("approved", String.valueOf(false)))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.id", Matchers.is(bookingOutputDto.getId()), Long.class),
                        jsonPath("$.item.id", Matchers.is(bookingOutputDto.getItem().getId()), Long.class),
                        jsonPath("$.status", Matchers.is(bookingOutputDto.getStatus().toString()))
                );
    }

    @Test
    void succeedFindBookingById() throws Exception {
        when(bookingService.findBookingById(anyLong(), anyLong())).thenReturn(bookingOutputDto);
        mockMvc.perform(get("/bookings/2")
                        .header(HEADER, 1L))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.id", Matchers.is(bookingOutputDto.getId()), Long.class),
                        jsonPath("$.item.id", Matchers.is(bookingOutputDto.getItem().getId()), Long.class),
                        jsonPath("$.booker.id", Matchers.is(bookingOutputDto.getBooker().getId()), Long.class)
                );
    }

    @Test
    void succeedFindAllUsersBooking() throws Exception {
        when(bookingService.findAllUsersBooking(anyLong(), anyString(), anyInt(), anyInt()))
                .thenReturn(List.of(bookingOutputDto));
        mockMvc.perform(get("/bookings")
                        .header(HEADER, 1L))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$[0].id", Matchers.is(bookingOutputDto.getId()), Long.class),
                        jsonPath("$[0].item.id", Matchers.is(bookingOutputDto.getItem().getId()), Long.class),
                        jsonPath("$[0].booker.id", Matchers.is(bookingOutputDto.getBooker().getId()), Long.class)
                );
        when(bookingService.findAllUsersBooking(anyLong(), anyString(), anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());
        mockMvc.perform(get("http://localhost:8080/bookings")
                        .header(HEADER, 1L)
                        .param("state", "rejected"))
                .andExpectAll(
                        status().isOk(),
                        MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON),
                        MockMvcResultMatchers.content().json("[]")
                );
    }

    @Test
    void succeedFindAllBookingsForItems() throws Exception {
        when(bookingService.findAllBookingsForItems(anyLong(), anyString(), anyInt(), anyInt()))
                .thenReturn(List.of(bookingOutputDto));
        mockMvc.perform(get("/bookings/owner")
                        .header(HEADER, 1L))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$[0].id", Matchers.is(bookingOutputDto.getId()), Long.class),
                        jsonPath("$[0].item.id", Matchers.is(bookingOutputDto.getItem().getId()), Long.class),
                        jsonPath("$[0].booker.id", Matchers.is(bookingOutputDto.getBooker().getId()), Long.class)
                );
        when(bookingService.findAllBookingsForItems(anyLong(), anyString(), anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());
        mockMvc.perform(get("/bookings/owner")
                        .header(HEADER, 1L)
                        .param("state", "rejected"))
                .andExpectAll(
                        status().isOk(),
                        MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON),
                        MockMvcResultMatchers.content().json("[]")
                );
    }
}