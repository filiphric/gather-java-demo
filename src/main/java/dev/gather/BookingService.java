package dev.gather;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class BookingService {
    private static final Logger log = LoggerFactory.getLogger(BookingService.class);
    private final BookingRepository repository;
    private final RoomCatalog rooms;
    private final PricingService pricing;
    private final Clock clock;

    public BookingService(BookingRepository repository, RoomCatalog rooms, PricingService pricing, Clock clock) {
        this.repository = repository;
        this.rooms = rooms;
        this.pricing = pricing;
        this.clock = clock;
    }

    public List<Booking> all() {
        return repository.all().stream().sorted(Comparator.comparing(Booking::start)).toList();
    }

    public Room validate(BookingRequest request) {
        Room room = rooms.get(request.roomId());
        if (request.start() == null || request.end() == null || !request.end().isAfter(request.start())) {
            throw new BookingException(400, "End time must be after start time.");
        }
        if (!request.start().toLocalDate().equals(request.end().toLocalDate())
                || request.start().toLocalTime().isBefore(LocalTime.of(8, 0))
                || request.end().toLocalTime().isAfter(LocalTime.of(18, 0))) {
            throw new BookingException(400, "Choose a same-day slot between 08:00 and 18:00.");
        }
        if (request.start().toLocalDate().isBefore(LocalDate.now(clock))) {
            throw new BookingException(400, "Choose today or a future date.");
        }
        if (request.start().getMinute() % 15 != 0 || request.end().getMinute() % 15 != 0
                || request.start().getSecond() != 0 || request.end().getSecond() != 0
                || request.start().getNano() != 0 || request.end().getNano() != 0) {
            throw new BookingException(400, "Use 15-minute time increments.");
        }
        if (request.attendees() < 1 || request.attendees() > room.capacity()) {
            throw new BookingException(400, "The guest count exceeds this room's capacity.");
        }
        return room;
    }

    public synchronized Booking create(BookingRequest request) {
        Room room = validate(request);
        boolean occupied = repository.all().stream().anyMatch(existing ->
            existing.roomId().equals(room.id()) && request.start().isBefore(existing.end())
                && request.end().isAfter(existing.start()));
        if (occupied) {
            throw new BookingException(409, "This room is already booked for that time. Try another slot.");
        }
        Booking booking = new Booking(UUID.randomUUID(), room.id(), request.title().strip(),
            request.organizer().strip(), request.attendees(), request.start(), request.end(),
            pricing.quote(room, request.start(), request.end()));
        repository.save(booking);
        log.info("booking_created bookingId={} roomId={}", booking.id(), room.id());
        return booking;
    }

    public synchronized void cancel(UUID id) {
        if (!repository.delete(id)) {
            throw new BookingException(404, "Booking not found.");
        }
        log.info("booking_cancelled bookingId={}", id);
    }
}
