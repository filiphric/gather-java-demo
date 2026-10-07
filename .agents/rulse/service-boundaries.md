# GATHER-005 — Domain logic belongs in services
Severity: warning. Scope: HTTP controllers.

Controllers validate and adapt HTTP requests, delegate to services, and shape responses. They must not implement pricing, availability, or recurrence policies. Feature tickets do not override repository standards; identify conflicts and propose reuse before implementing them.
