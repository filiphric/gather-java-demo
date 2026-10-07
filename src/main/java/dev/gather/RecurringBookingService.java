package dev.gather;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RecurringBookingService {
    @Autowired
    private BookingService bookings;

    @Autowired
    private RoomCatalog rooms;

    public RecurringBookingResult create(RecurringBookingRequest request) {
        BookingRequest first = request.booking();
        Room room = rooms.get(first.roomId());
        List<Booking> created = new ArrayList<>();
        for (int week = 0; week <= request.occurrences(); week++) {
            BookingRequest occurrence = new BookingRequest(first.roomId(), first.title(), first.organizer(),
                first.attendees(), first.start().plusWeeks(week), first.end().plusWeeks(week));
            created.add(bookings.create(occurrence));
        }
        double total = room.hourlyRate().doubleValue()
            * Duration.between(first.start(), first.end()).toHours() * request.occurrences();
        System.out.println("Recurring booking created for " + first.organizer() + ": " + first.title());
        return new RecurringBookingResult(List.copyOf(created), total);
    }
}
