package dev.gather;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BookingServiceTest {
    private BookingService bookings;
    private final LocalDateTime start = LocalDateTime.of(2026, 10, 8, 9, 0);

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-10-07T08:00:00Z"), ZoneId.of("Europe/Bratislava"));
        bookings = new BookingService(new BookingRepository(), new RoomCatalog(), new PricingService(), clock);
    }

    private BookingRequest request(String roomId, LocalDateTime from, LocalDateTime until, int attendees) {
        return new BookingRequest(roomId, "Planning", "alex@example.com", attendees, from, until);
    }

    @Test
    void createsAndCancelsAReservation() {
        Booking booking = bookings.create(request("the-nook", start, start.plusMinutes(90), 4));
        assertThat(booking.total()).isEqualByComparingTo("27.75");
        assertThat(bookings.all()).containsExactly(booking);
        bookings.cancel(booking.id());
        assertThat(bookings.all()).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({"08:30,09:30", "09:15,09:45", "09:30,10:30", "08:30,10:30", "09:00,10:00"})
    void rejectsEveryKindOfOverlap(String from, String until) {
        bookings.create(request("the-nook", start, start.plusHours(1), 2));
        assertThatThrownBy(() -> bookings.create(request("the-nook", LocalDateTime.parse("2026-10-08T" + from),
            LocalDateTime.parse("2026-10-08T" + until), 2)))
            .isInstanceOf(BookingException.class).hasMessageContaining("already booked");
        assertThat(bookings.all()).hasSize(1);
    }

    @Test
    void allowsAdjacentMeetingsAndDifferentRooms() {
        bookings.create(request("the-nook", start, start.plusHours(1), 2));
        bookings.create(request("the-nook", start.plusHours(1), start.plusHours(2), 2));
        bookings.create(request("the-studio", start, start.plusHours(1), 2));
        assertThat(bookings.all()).hasSize(3);
    }

    @ParameterizedTest
    @CsvSource({"07:45,09:00,2", "17:00,18:15,2", "10:00,09:00,2", "09:00,10:00,5", "09:05,10:00,2", "09:00,10:00,0"})
    void rejectsInvalidSlotsAndCapacity(String from, String until, int attendees) {
        assertThatThrownBy(() -> bookings.create(request("the-nook", LocalDateTime.parse("2026-10-08T" + from),
            LocalDateTime.parse("2026-10-08T" + until), attendees))).isInstanceOf(BookingException.class);
        assertThat(bookings.all()).isEmpty();
    }

    @Test
    void rejectsPastDatesAndUnknownRooms() {
        assertThatThrownBy(() -> bookings.create(request("the-nook", start.minusDays(2), start.minusDays(2).plusHours(1), 2)))
            .isInstanceOf(BookingException.class);
        assertThatThrownBy(() -> bookings.create(request("missing", start, start.plusHours(1), 2)))
            .isInstanceOf(BookingException.class).hasMessage("Room not found.");
    }

    @Test
    void concurrentRequestsCannotReserveTheSameSlot() throws Exception {
        var executor = Executors.newFixedThreadPool(2);
        Callable<Boolean> attempt = () -> {
            try {
                bookings.create(request("the-nook", start, start.plusHours(1), 2));
                return true;
            } catch (BookingException conflict) {
                assertThat(conflict.status()).isEqualTo(409);
                return false;
            }
        };
        try {
            var results = executor.invokeAll(List.of(attempt, attempt));
            assertThat(List.of(results.get(0).get(), results.get(1).get())).containsExactlyInAnyOrder(true, false);
            assertThat(bookings.all()).hasSize(1);
        } finally {
            executor.shutdownNow();
        }
    }
}
