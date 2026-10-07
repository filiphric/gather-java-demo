package dev.gather;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class BookingController {
    private final RoomCatalog rooms;
    private final BookingService bookings;
    private final PricingService pricing;
    private final Clock clock;

    public BookingController(RoomCatalog rooms, BookingService bookings, PricingService pricing, Clock clock) {
        this.rooms = rooms;
        this.bookings = bookings;
        this.pricing = pricing;
        this.clock = clock;
    }

    @GetMapping("/rooms")
    public List<Room> rooms() { return rooms.all(); }

    @GetMapping("/bookings")
    public List<Booking> bookings() { return bookings.all(); }

    @GetMapping("/workspace")
    public Map<String, String> workspace() {
        return Map.of("today", LocalDate.now(clock).toString(), "timezone", clock.getZone().toString());
    }

    @PostMapping("/quotes")
    public Map<String, BigDecimal> quote(@Valid @RequestBody BookingRequest request) {
        Room room = bookings.validate(request);
        return Map.of("total", pricing.quote(room, request.start(), request.end()));
    }

    @PostMapping("/bookings")
    @ResponseStatus(HttpStatus.CREATED)
    public Booking create(@Valid @RequestBody BookingRequest request) { return bookings.create(request); }

    @DeleteMapping("/bookings/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@PathVariable UUID id) { bookings.cancel(id); }
}
