package ru.practicum.shareit.request;

import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoByOwner;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static lombok.AccessLevel.PRIVATE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static ru.practicum.shareit.SortType.CREATED;

@ExtendWith(MockitoExtension.class)
@FieldDefaults(level = PRIVATE)
class ItemRequestServiceTest {
    static final Sort SORT = CREATED.getSortValue();
    @Mock
    ItemRequestRepository requestRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    ItemRepository itemRepository;
    @InjectMocks
    ItemRequestServiceImpl service;
    User requester;
    Item item;
    ItemRequest request;

    @BeforeEach
    void setUp() {
        User owner = User.builder()
                .id(1L)
                .name("test1")
                .email("test1@mail.ru")
                .build();
        requester = User.builder()
                .id(2L)
                .name("test2")
                .email("test2@mail.ru")
                .build();
        request = ItemRequest.builder()
                .id(1L)
                .requester(requester)
                .created(LocalDateTime.now())
                .build();
        item = Item.builder()
                .id(4L)
                .name("table")
                .description("red")
                .available(true)
                .owner(owner)
                .request(request)
                .build();
    }

    @Test
    void succeedCreateRequest() {
        when(requestRepository.save(any())).thenReturn(request);
        when(userRepository.findById(requester.getId())).thenReturn(Optional.of(requester));
        ItemRequestDto requestDto = service.addRequest(ItemRequestDto.builder().description("red").build(),
                requester.getId());
        assertNotNull(requestDto);
        assertEquals(request.getId(), requestDto.getId());
        verify(requestRepository, times(1)).save(any());
    }

    @Test
    void createRequestFailByUserNotFound() {
        Long userNotFoundId = 0L;
        String error = "Пользователь не найден.";
        when(userRepository.findById(userNotFoundId)).thenThrow(new NotFoundException(error));
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> service.addRequest(ItemRequestDto.builder().description("red").build(), userNotFoundId));
        assertEquals(error, exception.getMessage());
        verify(requestRepository, never()).save(any());
    }

    @Test
    void succeedFindAllByRequesterId() {
        long userId = requester.getId();
        when(userRepository.existsById(userId)).thenReturn(true);
        when(requestRepository.findAllByRequesterId(userId)).thenReturn(List.of(request));
        List<ItemRequestDtoByOwner> requests = service.findAllUsersRequestsWithReplies(userId);
        assertNotNull(requests);
        assertEquals(1, requests.size());
        verify(requestRepository, times(1)).findAllByRequesterId(userId);
    }

    @Test
    void succeedFindAllTest() {
        long userId = requester.getId();
        int from = 0;
        int size = 1;
        PageRequest pageRequest = PageRequest.of(from / size, size, SORT);
        when(requestRepository.findAllByRequesterIdNot(userId, pageRequest)).thenReturn(
                new PageImpl<>(List.of(request)));
        List<ItemRequestDtoByOwner> requestDto = service.getAllRequests(userId, from, size);
        assertNotNull(requestDto);
        assertEquals(1, requestDto.size());
    }

    @Test
    void findAllReturnEmptyList() {
        long userId = requester.getId();
        int from = 0;
        int size = 1;
        PageRequest pageRequest = PageRequest.of(from / size, size, SORT);
        when(requestRepository.findAllByRequesterIdNot(userId, pageRequest)).thenReturn(Page.empty());
        List<ItemRequestDtoByOwner> requestDto = service.getAllRequests(userId, from, size);
        assertNotNull(requestDto);
        assertTrue(requestDto.isEmpty());
    }

    @Test
    void findByIdTest() {
        long userId = requester.getId();
        when(userRepository.existsById(userId)).thenReturn(true);
        long requestId = request.getId();
        when(requestRepository.findById(requestId)).thenReturn(Optional.of(request));
        when(itemRepository.findByRequestId(requestId)).thenReturn(List.of(item));
        ItemRequestDtoByOwner requestDto = service.getRequestById(userId, requestId);
        assertNotNull(requestDto);
        assertEquals(requestId, requestDto.getId());
        assertEquals(1, requestDto.getItems().size());
        assertEquals(item.getId(), requestDto.getItems().get(0).getId());
    }
}