package dev.gather;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class RoomCatalog {
    private final List<Room> rooms = List.of(
        new Room("the-nook", "The Nook", "Small team. Big thinking.", 4,
            new BigDecimal("18.50"), "sage", List.of("Whiteboard", "Coffee")),
        new Room("the-studio", "The Studio", "A fresh perspective, together.", 8,
            new BigDecimal("32.00"), "peach", List.of("Screen", "Whiteboard")),
        new Room("the-loft", "The Loft", "Room for your next big idea.", 12,
            new BigDecimal("48.00"), "lavender", List.of("Screen", "Video call"))
    );

    public List<Room> all() { return rooms; }

    public Room get(String id) {
        return rooms.stream().filter(room -> room.id().equals(id)).findFirst()
            .orElseThrow(() -> new BookingException(404, "Room not found."));
    }
}
