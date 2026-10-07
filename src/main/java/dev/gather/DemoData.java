package dev.gather;

import java.time.Clock;
import java.time.LocalDate;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class DemoData implements ApplicationRunner {
    private final BookingService bookings;
    private final Clock clock;

    public DemoData(BookingService bookings, Clock clock) {
        this.bookings = bookings;
        this.clock = clock;
    }

    @Override
    public void run(ApplicationArguments args) {
        LocalDate day = LocalDate.now(clock);
        bookings.create(new BookingRequest("the-nook", "Make room for a fresh idea", "alex@example.com", 3,
            day.atTime(10, 0), day.atTime(11, 0)));
        bookings.create(new BookingRequest("the-studio", "A little product thinking", "sam@example.com", 6,
            day.atTime(13, 0), day.atTime(14, 30)));
        bookings.create(new BookingRequest("the-loft", "The next big thing", "jo@example.com", 8,
            day.atTime(15, 0), day.atTime(16, 0)));
    }
}
