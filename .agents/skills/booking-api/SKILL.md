---
name: booking-api
description: Add or change Gather HTTP booking endpoints, request records, error responses, and browser forms.
---

# Booking API

Follow the existing typed request records and `@Valid` boundary. Constrain strings, guest counts, and series counts on the server. Keep business policies in services.

Return 201 for creation, 204 for deletion, 400 for invalid input, 404 for unknown resources, and 409 for a booking conflict. Reuse `ApiExceptionHandler` and `ProblemDetail`; do not return exception messages or stack traces from generic exceptions.

For new endpoints, include HTTP tests for success, invalid input, and conflicts where applicable. For a bulk or recurring request, include the failure after an earlier valid item and verify stored state after the error.

In browser forms, label every control, render errors near the form, preserve user input after errors, prevent accidental double submission, and render server data as text rather than HTML.
