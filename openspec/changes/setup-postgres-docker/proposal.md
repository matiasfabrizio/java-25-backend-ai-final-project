## Why

`src/main/resources/application.yaml` only sets `spring.application.name` — there is no `spring.datasource.*` configured. The app cannot start against a real database today (confirmed: `mvnw.cmd test` currently fails at `AcademiaApplicationTests#contextLoads` because Spring can't build a `DataSource`), and `curso-api`'s endpoints can't be manually exercised (curl/Postman) without one. This change provides a local Postgres via Docker so the app can actually run and be manually tested end to end.

## What Changes

- Add a `compose.yaml` at the project root defining a single `postgres` service (pinned image version, fixed dev credentials, a named volume for data persistence across restarts).
- Add the `spring-boot-docker-compose` dependency (optional, matching the existing `devtools` pattern) so Spring Boot auto-starts `compose.yaml`'s services and auto-configures the datasource connection details whenever the app runs in dev (`mvnw spring-boot:run` / IDE run) — no manual `spring.datasource.*` properties or credentials to maintain by hand.
- Document the new local dev flow in `CLAUDE.md` (prerequisite: Docker running; first `spring-boot:run` pulls and starts Postgres automatically).

## Capabilities

No capability's requirements change — `curso-api`'s HTTP behavior is unaffected; this only makes a real datasource available in dev. This is infrastructure/tooling, not an application capability, so `skip_specs: true` is set on this change (`.openspec.yaml`) instead of declaring a spec delta.

### New Capabilities
_None._

### Modified Capabilities
_None._

## Impact

- **New files**: `compose.yaml` (project root).
- **Modified files**: `pom.xml` (new `spring-boot-docker-compose` dependency), `CLAUDE.md` (document the new local dev DB flow, remove the now-stale "no datasource configured" note).
- **Dependencies**: adds `spring-boot-docker-compose` (optional/runtime, like `devtools`) and requires Docker Desktop/Engine running locally.
- **Behavior**: running the app locally (`mvnw spring-boot:run`) now starts a real Postgres automatically; manual endpoint testing of `curso-api` becomes possible.
- **Out of scope**: Testcontainers / automated-test wiring (deferred to a separate change per explicit decision — `mvnw.cmd test` will still fail at `contextLoads` after this change, for the same pre-existing reason, until that follow-up lands), production datasource configuration, CI database setup.
