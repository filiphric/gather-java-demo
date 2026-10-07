package dev.gather;

import java.util.List;

public record RecurringBookingResult(List<Booking> bookings, double total) {}
