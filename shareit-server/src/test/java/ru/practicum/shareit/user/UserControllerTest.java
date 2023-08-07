package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.practicum.shareit.user.dto.UserDto;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
class UserControllerTest {
    @Autowired
    ObjectMapper mapper;
    @Autowired
    MockMvc mockMvc;
    @MockBean
    private UserServiceImpl userService;
    private final UserDto userDto = new UserDto(1L, "test", "test@mail.ru");

    @AfterEach
    void deleteUser() {
        userService.deleteUser(anyLong());
    }

    @Test
    void succeedCreateUser() throws Exception {
        when(userService.saveUser(any())).thenReturn(userDto);
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", Matchers.is(userDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", Matchers.is(userDto.getName())))
                .andExpect(jsonPath("$.email", Matchers.is(userDto.getEmail())));
    }

    @Test
    void succeedUpdateUserNameAndEmail() throws Exception {
        when(userService.updateUser(any(), anyLong())).thenReturn(userDto);
        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", Matchers.is(userDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", Matchers.is(userDto.getName())))
                .andExpect(jsonPath("$.email", Matchers.is(userDto.getEmail())));
    }

    @Test
    void succeedUpdateUsersName() throws Exception {
        when(userService.updateUser(any(), anyLong())).thenReturn(userDto);
        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"test\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", Matchers.is("test")));
    }

    @Test
    void succeedUpdateUsersEmail() throws Exception {
        when(userService.updateUser(any(), anyLong())).thenReturn(userDto);

        mockMvc.perform(patch("/users/1")
                        .content("{" +
                                "    \"email\": \"test@mail.ru\"" +
                                " }")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isOk(),
                        jsonPath("$.id", Matchers.is(userDto.getId()), Long.class),
                        jsonPath("$.name", Matchers.is(userDto.getName())),
                        jsonPath("$.email", Matchers.is(userDto.getEmail()))
                );
    }

    @Test
    void succeedFindById() throws Exception {
        when(userService.getUserById(anyLong())).thenReturn(userDto);
        mockMvc.perform(get("/users/1"))
                .andExpectAll(status().isOk(),
                        jsonPath("$.id", Matchers.is(userDto.getId()), Long.class),
                        jsonPath("$.name", Matchers.is(userDto.getName())),
                        jsonPath("$.email", Matchers.is(userDto.getEmail()))
                );
    }

    @Test
    void succeedDeleteUser() throws Exception {
        mockMvc.perform(delete("/users/{id}", userDto.getId())).andExpect(status().isOk());
        verify(userService, times(1)).deleteUser(userDto.getId());
    }

    @Test
    void findAllWithUsers() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of(userDto));
        mockMvc.perform(get("/users"))
                .andExpectAll(status().isOk(),
                        jsonPath("$[0].id", Matchers.is(userDto.getId()), Long.class),
                        jsonPath("$[0].name", Matchers.is(userDto.getName())),
                        jsonPath("$[0].email", Matchers.is(userDto.getEmail())));
    }

    @Test
    void findAllWhenUsersListIsEmpty() throws Exception {
        when(userService.getAllUsers()).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/users")).andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().json("[]"));
    }
}