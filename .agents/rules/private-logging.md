# GATHER-004 — Logs without personal data
Severity: error. Scope: application logging.

Use SLF4J parameterized event logs. Log booking IDs, room IDs, and counts when useful. Do not log organizer email addresses or meeting titles. Do not use `System.out` or `System.err` for application events.
