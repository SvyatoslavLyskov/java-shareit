package ru.practicum.shareit;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingOutputDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoByOwner;
import ru.practicum.shareit.item.dto.ItemDtoForRequest;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoByOwner;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ObjectMapper {
    public static UserDto toUserDto(User user) {
        return new UserDto(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }

    public static User toUser(UserDto dto) {
        return new User(
                dto.getId(),
                dto.getName(),
                dto.getEmail()
        );
    }

    public static ItemDto toItemDto(Item item) {
        return new ItemDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                item.getRequest() != null ?
                        item.getRequest().getId() : null
        );
    }

    public static ItemDtoForRequest toItemDtoForRequest(Item item, User owner) {
        return new ItemDtoForRequest(
                item.getId(),
                item.getName(),
                owner
        );
    }

    public static Item toItem(ItemDto dto, ItemRequest requests) {
        Item item = new Item();
        item.setId(dto.getId());
        item.setName(dto.getName());
        item.setDescription(dto.getDescription());
        item.setAvailable(dto.getAvailable());
        item.setRequest(requests);
        return item;
    }

    public static ItemDtoByOwner toItemDtoByOwner(Item item, List<Booking> lastBookings, List<Booking> nextBookings,
                                                  List<Comment> comments) {
        List<CommentDto> commentDto = comments.stream().map(ObjectMapper::toCommentDto).collect(Collectors.toList());
        Booking nextBooking = nextBookings.stream()
                .min(Comparator.comparing(Booking::getStart)).orElse(null);
        Booking lastBooking = lastBookings.stream()
                .max(Comparator.comparing(Booking::getStart)).orElse(null);
        return new ItemDtoByOwner(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                item.getRequest() != null ?
                        item.getRequest().getId() : null,
                lastBooking != null ? ObjectMapper.toBookingDto(lastBooking) : null,
                nextBooking != null ? ObjectMapper.toBookingDto(nextBooking) : null,
                commentDto
        );
    }

    public static Comment toComment(CommentDto commentDto, User user, Item item) {
        return new Comment(
                commentDto.getId(),
                commentDto.getText(),
                user,
                item,
                LocalDateTime.now()
        );
    }

    public static CommentDto toCommentDto(Comment comment) {
        return new CommentDto(
                comment.getId(),
                comment.getText(),
                comment.getAuthor().getName(),
                comment.getItem().getId(),
                comment.getCreated()
        );
    }

    public static Booking toBooking(BookingDto dto, Item item, User booker) {
        return new Booking(
                dto.getId(),
                item,
                dto.getStart(),
                dto.getEnd(),
                booker,
                dto.getStatus()
        );
    }

    public static BookingDto toBookingDto(Booking booking) {
        return new BookingDto(
                booking.getId(),
                booking.getItem().getId(),
                booking.getStart(),
                booking.getEnd(),
                booking.getBooker().getId(),
                booking.getStatus()
        );
    }

    public static BookingOutputDto toBookingOutputDto(Booking booking) {
        ItemDto itemDto = ObjectMapper.toItemDto(booking.getItem());
        UserDto userDto = ObjectMapper.toUserDto(booking.getBooker());
        return new BookingOutputDto(
                booking.getId(),
                itemDto,
                booking.getStart(),
                booking.getEnd(),
                userDto,
                booking.getStatus()
        );
    }

    public static List<BookingOutputDto> toBookingsOutputList(List<Booking> bookings) {
        return bookings.stream().map(ObjectMapper::toBookingOutputDto).collect(Collectors.toList());
    }

    public static ItemRequest toItemRequest(ItemRequestDto dto) {
        return new ItemRequest(
                dto.getId(),
                dto.getDescription(),
                dto.getRequester(),
                dto.getCreated()
        );
    }

    public static ItemRequestDto toItemRequestDto(ItemRequest request) {
        return new ItemRequestDto(
                request.getId(),
                request.getDescription(),
                request.getRequester(),
                request.getCreated()
        );
    }

    public static ItemRequestDtoByOwner toItemRequestDtoByOwner(ItemRequest request, List<ItemDto> reply) {
        return new ItemRequestDtoByOwner(
                request.getId(),
                request.getDescription(),
                request.getRequester(),
                request.getCreated(),
                reply
        );
    }
}