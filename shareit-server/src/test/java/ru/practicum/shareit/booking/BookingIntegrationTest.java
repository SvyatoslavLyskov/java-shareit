package ru.practicum.shareit.booking;

import lombok.experimental.FieldDefaults;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingOutputDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;

import static lombok.AccessLevel.PRIVATE;

@Transactional
@SpringBootTest
@FieldDefaults(level = PRIVATE)
class BookingIntegrationTest {
    @Autowired
    EntityManager em;
    @Autowired
    BookingService bookingService;
    @Autowired
    BookingRepository bookingRepository;
    final LocalDateTime now = LocalDateTime.now();
    User owner;
    User booker;
    Item item1;
    Booking booking2;
    Booking booking3;

    @BeforeEach
    void setUp() {
        owner = User.builder().name("owner").email("owner@mail.ru").build();
        em.persist(owner);
        booker = User.builder().name("booker").email("booker@mail.ru").build();
        em.persist(booker);
        item1 = Item.builder()
                .name("table").description("green").available(true)
                .owner(owner).build();
        em.persist(item1);
        Item item2 = Item.builder()
                .name("chair").description("white").available(true)
                .owner(owner).build();
        em.persist(item2);
        Booking booking1 = Booking.builder()
                .item(item1).booker(booker).status(Status.APPROVED)
                .start(now.minusDays(1)).end(now.plusDays(1))
                .build();
        em.persist(booking1);
        booking2 = Booking.builder()
                .item(item1).booker(booker).status(Status.WAITING)
                .start(now.plusDays(1)).end(now.plusDays(2))
                .build();
        em.persist(booking2);
        booking3 = Booking.builder()
                .item(item2).booker(booker).status(Status.REJECTED)
                .start(now.minusDays(2)).end(now.minusDays(1))
                .build();
        em.persist(booking3);
        Booking booking4 = Booking.builder()
                .item(item2).booker(booker).status(Status.CANCELED)
                .start(now.minusDays(1)).end(now.plusDays(1))
                .build();
        em.persist(booking4);
    }

    @Test
    void createBookingTest() {
        Long userId = booker.getId();
        Long itemId = item1.getId();
        BookingDto newBooking = BookingDto.builder()
                .itemId(itemId)
                .start(now.plusDays(2)).end(now.plusDays(4))
                .build();
        BookingOutputDto created = bookingService.createBooking(newBooking, userId);
        Booking retrievedBooking = bookingRepository.findById(created.getId()).orElse(null);
        Assertions.assertThat(retrievedBooking).isNotNull();
        Assertions.assertThat(retrievedBooking.getStatus()).isEqualTo(Status.WAITING);
        Assertions.assertThat(retrievedBooking.getBooker().getId()).isEqualTo(userId);
        Assertions.assertThat(retrievedBooking.getItem().getId()).isEqualTo(itemId);
    }

    @Test
    void approveTest() {
        Long userId = owner.getId();
        Long bookingId = booking2.getId();
        Assertions.assertThat(bookingService.findBookingById(booker.getId(), bookingId))
                .hasFieldOrPropertyWithValue("status", Status.WAITING);
        BookingOutputDto approvedBooking = bookingService.confirmBookingByOwner(userId, bookingId, true);
        Assertions.assertThat(approvedBooking).isNotNull()
                .hasFieldOrPropertyWithValue("status", Status.APPROVED);
    }

    @Test
    void getBookingTest() {
        Long userId = booker.getId();
        Long bookingId = booking3.getId();
        BookingOutputDto finder = bookingService.findBookingById(userId, bookingId);
        Assertions.assertThat(finder).isNotNull()
                .hasFieldOrPropertyWithValue("id", bookingId);
        Assertions.assertThat(finder.getBooker())
                .hasFieldOrPropertyWithValue("id", userId);
    }

    @Test
    void getAllBookingsAllTest() {
        List<BookingOutputDto> list1 = bookingService.findAllUsersBooking(booker.getId(), "ALL", 0, 20);
        Assertions.assertThat(list1).isNotEmpty().hasSize(4);
    }

    @Test
    void getAllBookingsPastTest() {
        List<BookingOutputDto> list2 = bookingService.findAllUsersBooking(booker.getId(), "PAST", 0, 20);
        Assertions.assertThat(list2).isNotEmpty().hasSize(1);
    }

    @Test
    void getAllBookingsFutureTest() {
        List<BookingOutputDto> list3 = bookingService.findAllUsersBooking(booker.getId(), "FUTURE", 0, 20);
        Assertions.assertThat(list3).isNotEmpty().hasSize(1);
    }

    @Test
    void getAllBookingsCurrentTest() {
        List<BookingOutputDto> list4 = bookingService.findAllUsersBooking(booker.getId(),
                "CURRENT", 0, 20);
        Assertions.assertThat(list4).isNotEmpty().hasSize(2);
    }

    @Test
    void getAllBookingsRejectedTest() {
        List<BookingOutputDto> list5 = bookingService.findAllUsersBooking(booker.getId(),
                "REJECTED", 0, 20);
        Assertions.assertThat(list5).isNotEmpty().hasSize(1);
    }

    @Test
    void getAllBookingsWaitingTest() {
        List<BookingOutputDto> list6 = bookingService.findAllUsersBooking(booker.getId(),
                "WAITING", 0, 20);
        Assertions.assertThat(list6).isNotEmpty().hasSize(1);
    }

    @Test
    void getAllBookingsForOwnerAllTest() {
        List<BookingOutputDto> list1 = bookingService.findAllBookingsForItems(owner.getId(),
                "ALL", 0, 20);
        Assertions.assertThat(list1).isNotEmpty().hasSize(4);
    }

    @Test
    void getAllBookingsForOwnerPastTest() {
        List<BookingOutputDto> list2 = bookingService.findAllBookingsForItems(owner.getId(),
                "PAST", 0, 20);
        Assertions.assertThat(list2).isNotEmpty().hasSize(1);
    }

    @Test
    void getAllBookingsForOwnerFutureTest() {
        List<BookingOutputDto> list3 = bookingService.findAllBookingsForItems(owner.getId(),
                "FUTURE", 0, 20);
        Assertions.assertThat(list3).isNotEmpty().hasSize(1);
    }

    @Test
    void getAllBookingsForOwnerCurrentTest() {
        List<BookingOutputDto> list4 = bookingService.findAllBookingsForItems(owner.getId(),
                "CURRENT", 0, 20);
        Assertions.assertThat(list4).isNotEmpty().hasSize(2);
    }

    @Test
    void getAllBookingsForOwnerRejectedTest() {
        List<BookingOutputDto> list5 = bookingService.findAllBookingsForItems(owner.getId(),
                "REJECTED", 0, 20);
        Assertions.assertThat(list5).isNotEmpty().hasSize(1);
    }

    @Test
    void getAllBookingsForOwnerWaitingTest() {
        List<BookingOutputDto> list6 = bookingService.findAllBookingsForItems(owner.getId(),
                "WAITING", 0, 20);
        Assertions.assertThat(list6).isNotEmpty().hasSize(1);
    }
}