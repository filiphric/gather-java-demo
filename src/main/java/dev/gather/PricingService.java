package dev.gather;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

@Service
public class PricingService {
    public BigDecimal quote(Room room, LocalDateTime start, LocalDateTime end) {
        long minutes = Duration.between(start, end).toMinutes();
        if (minutes <= 0) {
            throw new BookingException(400, "End time must be after start time.");
        }
        return room.hourlyRate().multiply(BigDecimal.valueOf(minutes))
            .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
    }
}
