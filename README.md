# Gather

A compact meeting-room booking app built with Java 17, Spring Boot, and a dependency-free browser interface.

## Run

Install Java 17+ and Maven 3.6.3+, then:

```sh
mvn spring-boot:run
```

Open http://localhost:8080. Set `PORT=8081` if 8080 is occupied. Rooms and sample reservations are created at startup; restart the app to reset the workspace. No database, API keys, or frontend build is needed. This is a local demonstration app with shared in-memory state and no authentication.

## Check

```sh
mvn verify
```

GitHub Actions runs Java and availability checks for pushes to `main` and pull requests.

To run the dependency-free browser scheduling tests (Node.js 22+):

```sh
node --test src/test/js/*.test.mjs
```

Node is only needed for these tests; the app still starts with Java and Maven alone.

## Tour

- Choose a date and group size, then book directly from a room’s available times.
- Create and cancel reservations; overlapping bookings are rejected.
- Weekly recurring reservations, with a selectable number of meetings.
- Server-calculated prices for 15-minute time increments.
- Responsive dashboard and keyboard-accessible booking form.

All dates use Europe/Bratislava local time, with daily opening hours of 08:00–18:00. Prices are illustrative EUR amounts, with no tax or payment integration.

## Layout

- `src/main/java/dev/gather` — HTTP endpoints, domain services, in-memory repository.
- `src/main/resources/static` — HTML, CSS, and JavaScript.
- `src/test/java/dev/gather` — domain and HTTP tests.
- `.agents/rulse/` — named review standards, indexed by `best_practices.md`.
- `.agents/skills` — booking and API implementation workflows.
- `.pr_agent.toml` — Qodo draft-PR reviews, skill insights, and repository guidance.

Qodo review requires its GitHub App to have access to this repository. Repository configuration alone does not install or enable the app for an account.
