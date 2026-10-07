package dev.gather;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class BookingRepository {
    private final Map<UUID, Booking> bookings = new LinkedHashMap<>();

    public synchronized List<Booking> all() { return new ArrayList<>(bookings.values()); }

    public synchronized Booking save(Booking booking) {
        bookings.put(booking.id(), booking);
        return booking;
    }

    public synchronized boolean delete(UUID id) { return bookings.remove(id) != null; }
}
