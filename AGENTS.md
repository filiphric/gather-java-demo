# Working on Gather

Gather is a small Java 17 / Spring Boot application. The UI is plain HTML, CSS, and JavaScript served by Spring. Storage is in memory; restart to restore the sample workspace.

- Run `mvn verify` before proposing changes.
- Keep domain behavior in services and HTTP adaptation in controllers.
- Follow the named standards in `.agents/rules/`, indexed by `best_practices.md`.
- For reservation changes, use `.agents/skills/booking-workflows/SKILL.md`.
- For HTTP contract changes, use `.agents/skills/booking-api/SKILL.md`.
- Dates are local to Europe/Bratislava. Opening hours are 08:00–18:00. Booking intervals are half-open: an appointment may begin exactly when another ends.
- Keep the browser interface dependency-free and usable with keyboard input.
