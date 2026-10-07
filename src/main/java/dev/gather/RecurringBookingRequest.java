package dev.gather;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record RecurringBookingRequest(
    @NotNull @Valid BookingRequest booking,
    @Min(1) @Max(12) int occurrences) {}
