package dev.gather;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PricingServiceTest {
    private final PricingService pricing = new PricingService();
    private final Room room = new RoomCatalog().get("the-nook");
    private final LocalDateTime start = LocalDateTime.of(2026, 10, 8, 9, 0);

    @ParameterizedTest
    @CsvSource({"15,4.63", "30,9.25", "60,18.50", "90,27.75", "150,46.25"})
    void pricesTheFullDurationAndRoundsHalfUp(int minutes, String expected) {
        assertThat(pricing.quote(room, start, start.plusMinutes(minutes))).isEqualByComparingTo(expected);
    }

    @Test
    void rejectsNonPositiveDuration() {
        assertThatThrownBy(() -> pricing.quote(room, start, start)).isInstanceOf(BookingException.class);
    }
}
