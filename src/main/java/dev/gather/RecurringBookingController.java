package dev.gather;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bookings/recurring")
public class RecurringBookingController {
    private final RecurringBookingService recurring;

    public RecurringBookingController(RecurringBookingService recurring) {
        this.recurring = recurring;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RecurringBookingResult create(@Valid @RequestBody RecurringBookingRequest request) {
        return recurring.create(request);
    }
}
