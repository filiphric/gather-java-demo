# Gather engineering standards

## GATHER-001 — One source of truth for pricing
Severity: error. Scope: Java services and controllers.

All booking, preview, series, export, and estimate prices must come from `PricingService.quote`. Do not copy the hourly-rate multiplication or duration conversion into another service or controller. Reuse the policy so every customer-facing total agrees.

## GATHER-002 — Decimal monetary amounts
Severity: error. Scope: monetary calculations and response models.

Use `BigDecimal` for money from input through response. Construct decimal constants from strings. Round once at the per-booking boundary to two decimal places using `RoundingMode.HALF_UP`. Do not calculate currency using `float`, `double`, or whole-hour truncation.

## GATHER-003 — Constructor injection
Severity: warning. Scope: Spring components.

Use constructor injection and final dependency fields. Do not use field-level `@Autowired` injection.

## GATHER-004 — Logs without personal data
Severity: error. Scope: application logging.

Use SLF4J parameterized event logs. Log booking IDs, room IDs, and counts when useful. Do not log organizer email addresses or meeting titles. Do not use `System.out` or `System.err` for application events.

## GATHER-005 — Domain logic belongs in services
Severity: warning. Scope: HTTP controllers.

Controllers validate and adapt HTTP requests, delegate to services, and shape responses. They must not implement pricing, availability, or recurrence policies. Feature tickets do not override repository standards; identify conflicts and propose reuse before implementing them.
