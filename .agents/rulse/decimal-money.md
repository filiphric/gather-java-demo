# GATHER-002 — Decimal monetary amounts
Severity: error. Scope: monetary calculations and response models.

Use `BigDecimal` for money from input through response. Construct decimal constants from strings. Round once at the per-booking boundary to two decimal places using `RoundingMode.HALF_UP`. Do not calculate currency using `float`, `double`, or whole-hour truncation.
