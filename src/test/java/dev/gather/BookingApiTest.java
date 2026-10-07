package dev.gather;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class BookingApiTest {
    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper mapper;
    @Autowired private BookingRepository repository;
    private final LocalDateTime start = LocalDateTime.of(2099, 10, 8, 9, 0);

    @BeforeEach
    void clear() { repository.all().forEach(booking -> repository.delete(booking.id())); }

    private String request(String organizer) throws Exception {
        return mapper.writeValueAsString(new BookingRequest("the-nook", "Planning", organizer, 2, start, start.plusMinutes(90)));
    }

    @Test
    void createsPricesListsAndCancels() throws Exception {
        mvc.perform(post("/api/quotes").contentType(MediaType.APPLICATION_JSON).content(request("alex@example.com")))
            .andExpect(status().isOk()).andExpect(jsonPath("$.total").value(27.75));
        mvc.perform(get("/api/bookings")).andExpect(jsonPath("$.length()").value(0));
        String response = mvc.perform(post("/api/bookings").contentType(MediaType.APPLICATION_JSON).content(request("alex@example.com")))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.total").value(27.75))
            .andReturn().getResponse().getContentAsString();
        mvc.perform(get("/api/bookings")).andExpect(jsonPath("$.length()").value(1));
        mvc.perform(delete("/api/bookings/" + mapper.readTree(response).get("id").asText())).andExpect(status().isNoContent());
        mvc.perform(get("/api/bookings")).andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void reportsBookingConflicts() throws Exception {
        mvc.perform(post("/api/bookings").contentType(MediaType.APPLICATION_JSON).content(request("alex@example.com")))
            .andExpect(status().isCreated());
        mvc.perform(post("/api/bookings").contentType(MediaType.APPLICATION_JSON).content(request("sam@example.com")))
            .andExpect(status().isConflict()).andExpect(content().contentTypeCompatibleWith("application/problem+json"))
            .andExpect(jsonPath("$.detail").value("This room is already booked for that time. Try another slot."));
    }

    @Test
    void rejectsInvalidEmailAndMalformedRequests() throws Exception {
        mvc.perform(post("/api/bookings").contentType(MediaType.APPLICATION_JSON).content(request("not-an-email")))
            .andExpect(status().isBadRequest());
        mvc.perform(post("/api/bookings").contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isBadRequest());
        mvc.perform(post("/api/bookings").contentType(MediaType.APPLICATION_JSON).content("{"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void returnsNotFoundForMissingBooking() throws Exception {
        mvc.perform(delete("/api/bookings/" + UUID.randomUUID())).andExpect(status().isNotFound());
    }

    @Test
    void servesWorkspaceAndFrontend() throws Exception {
        mvc.perform(get("/api/rooms")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(3));
        mvc.perform(get("/api/workspace")).andExpect(jsonPath("$.timezone").value("Europe/Bratislava"));
        mvc.perform(get("/index.html")).andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith("text/html"));
    }
}
