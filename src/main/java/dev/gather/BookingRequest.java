package dev.gather;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record BookingRequest(
    @NotBlank String roomId,
    @NotBlank @Size(max = 80) String title,
    @NotBlank @Email @Size(max = 120) String organizer,
    @Min(1) @Max(12) int attendees,
    @NotNull LocalDateTime start,
    @NotNull LocalDateTime end) {}
