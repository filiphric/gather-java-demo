package dev.gather;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record Booking(UUID id, String roomId, String title, String organizer,
                      int attendees, LocalDateTime start, LocalDateTime end, BigDecimal total) {}
