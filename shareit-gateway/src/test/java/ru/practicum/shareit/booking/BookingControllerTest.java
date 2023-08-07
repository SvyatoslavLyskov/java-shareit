package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.shareit.booking.BookingClient.USER_ID;

@WebMvcTest(controllers = BookingController.class)
class BookingControllerTest {
    private static final String URL = "http://localhost:8080/bookings";
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private BookingClient client;

    @Test
    void findAllBookingsForItemsFailByIncorrectFrom() throws Exception {
        mockMvc.perform(get(URL + "/owner")
                        .header(USER_ID, 1)
                        .param("from", "-1")
                        .param("size", "1"))
                .andExpectAll(status().isBadRequest(),
                        MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON),
                        MockMvcResultMatchers.content().json(
                                "{" +
                                        "      \"Некорректное значение\":" +
                                        "\"findAllBookingsForItems.from: must be greater than or equal to 0\"" +
                                        "   }"));
    }

    @Test
    void findAllBookingsForItemsFailByIncorrectSize() throws Exception {
        mockMvc.perform(get(URL + "/owner")
                        .header(USER_ID, 1)
                        .param("from", "1")
                        .param("size", "0"))
                .andExpectAll(status().isBadRequest(),
                        MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON),
                        MockMvcResultMatchers.content().json(
                                "{" +
                                        "      \"Некорректное значение\":" +
                                        "\"findAllBookingsForItems.size: must be greater than or equal to 1\"" +
                                        "   }"));
    }

    @Test
    void findAllUsersBookingFailByUnsupportedStatus() throws Exception {
        mockMvc.perform(get(URL)
                        .header(USER_ID, 1)
                        .param("from", "3")
                        .param("state", "REJECTING")
                        .param("size", "3"))
                .andExpectAll(
                        MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON),
                        MockMvcResultMatchers.content().json(
                                "{" +
                                        "      \"error\":" +
                                        "\"Unknown state: UNSUPPORTED_STATUS\"" +
                                        "   }"));
    }

    @Test
    void findAllBookingsForItemsFailByUnsupportedStatus() throws Exception {
        mockMvc.perform(get(URL + "/owner")
                        .header(USER_ID, 1)
                        .param("from", "3")
                        .param("state", "REJECTING")
                        .param("size", "3"))
                .andExpectAll(
                        MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON),
                        MockMvcResultMatchers.content().json(
                                "{" +
                                        "      \"error\":" +
                                        "\"Unknown state: UNSUPPORTED_STATUS\"" +
                                        "   }"));
    }

    @Test
    void findAllUsersBookingFailByIncorrectFrom() throws Exception {
        mockMvc.perform(get(URL)
                        .header(USER_ID, 1)
                        .param("from", "-1")
                        .param("size", "1"))
                .andExpectAll(status().isBadRequest(),
                        MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON),
                        MockMvcResultMatchers.content().json(
                                "{" +
                                        "      \"Некорректное значение\":" +
                                        "\"getBookings.from: must be greater than or equal to 0\"" +
                                        "   }"));
    }

    @Test
    void findAllUsersBookingFailByIncorrectSize() throws Exception {
        mockMvc.perform(get(URL)
                        .header(USER_ID, 1)
                        .param("from", "1")
                        .param("size", "0"))
                .andExpectAll(status().isBadRequest(),
                        MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON),
                        MockMvcResultMatchers.content().json(
                                "{" +
                                        "      \"Некорректное значение\":" +
                                        "\"getBookings.size: must be greater than or equal to 1\"" +
                                        "   }"));
    }
}