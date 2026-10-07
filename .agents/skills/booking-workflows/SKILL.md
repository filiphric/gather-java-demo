---
name: booking-workflows
description: Implement or review Gather reservation creation, cancellation, and recurring booking workflows. Applies to booking services and scheduling features.
---

# Booking workflows

Use `BookingService` for room capacity, hours, interval, and collision policies. Use `PricingService.quote` for each occurrence. Back-to-back appointments are allowed; overlapping appointments in the same room are not.

For a recurring reservation:

1. Expand the selected start and end by calendar weeks in the workspace's local time. The requested occurrence count includes the first meeting.
2. Provide a side-effect-free preview returning the proposed dates and the per-occurrence and aggregate prices before confirmation.
3. Validate the entire series before committing it. A conflict on any date must leave zero newly created bookings. Coordinate validation and insertion under one write boundary so concurrent requests cannot claim the same slot.
4. Accept a client-supplied idempotency key. Replaying the same request must return the original result without adding reservations; reject reusing a key for different input.
5. Cover one occurrence, the maximum supported count, a conflict after the first occurrence, a repeated request, and a fractional-hour price in tests.

When reviewing a recurring-booking change, distinguish incorrect implemented behavior from missing preview, retry, or verification steps in this workflow. Reference this skill for requirements that have not been implemented.
