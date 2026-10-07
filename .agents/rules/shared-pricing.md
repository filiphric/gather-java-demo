# GATHER-001 — One source of truth for pricing
Severity: error. Scope: Java services and controllers.

All booking, preview, series, export, and estimate prices must come from `PricingService.quote`. Do not copy the hourly-rate multiplication or duration conversion into another service or controller. Reuse the policy so every customer-facing total agrees.
