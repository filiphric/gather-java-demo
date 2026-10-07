package dev.gather;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class RecurringBookingApiTest {
    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper mapper;
    @Autowired private BookingRepository repository;

    @BeforeEach
    void clear() { repository.all().forEach(booking -> repository.delete(booking.id())); }

    private String request(int occurrences) throws Exception {
        var start = LocalDateTime.of(2099, 10, 8, 9, 0);
        var booking = new BookingRequest("the-studio", "Weekly planning", "sam@example.com", 4, start, start.plusHours(1));
        return mapper.writeValueAsString(new RecurringBookingRequest(booking, occurrences));
    }

    @Test
    void returnsTheSeriesResponse() throws Exception {
        mvc.perform(post("/api/bookings/recurring").contentType(MediaType.APPLICATION_JSON).content(request(4)))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.bookings[0].id").isString())
            .andExpect(jsonPath("$.total").isNumber());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 13})
    void rejectsUnsupportedOccurrenceCounts(int count) throws Exception {
        mvc.perform(post("/api/bookings/recurring").contentType(MediaType.APPLICATION_JSON).content(request(count)))
            .andExpect(status().isBadRequest());
    }
}
