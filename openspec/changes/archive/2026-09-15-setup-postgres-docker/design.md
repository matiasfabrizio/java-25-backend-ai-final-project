## Context

See proposal.md - Why. `pom.xml` already depends on the `postgresql` JDBC driver and `spring-boot-starter-data-jpa`, but no `spring.datasource.*` is configured anywhere, so there is currently no way to run the app against a real database. `pom.xml` already has a precedent for a dev-only, classpath-excluded-from-prod dependency: `spring-boot-devtools` is declared with `<scope>runtime</scope><optional>true</optional>`, which `spring-boot-maven-plugin` excludes from the repackaged fat jar.

## Goals / Non-Goals

**Goals:**
- Running `mvnw spring-boot:run` (or an IDE run) against a clean checkout gives a working Postgres with zero manual steps beyond "have Docker running."
- No datasource credentials/URL hand-maintained in `application.yaml` or a new profile file — Spring Boot reads them from the running container.

**Non-Goals:**
- Automated test datasource (Testcontainers) — separate change, per explicit decision.
- Production datasource configuration or deployment topology.
- Multiple environments/profiles (staging, CI) — dev only.

## Decisions

**`spring-boot-docker-compose` over a hand-maintained `application-local.yaml`.**
Spring Boot's Docker Compose support (`org.springframework.boot:spring-boot-docker-compose`) auto-starts the services in a `compose.yaml` at the project root when the app boots in dev, waits for container health, reads the actual container connection details (host/port/credentials) via the Docker API, and auto-configures the `DataSource` from them — then stops the containers on shutdown. This means no `spring.datasource.*` properties, no new Spring profile, and no credentials duplicated between `compose.yaml` and a config file to keep in sync. Alternative considered (manual `docker-compose.yml` + `application-local.yaml` + `local` profile) was rejected per your choice: more files, more to keep in sync, no benefit here since there's nothing environment-specific to vary yet.

**Declared `<scope>runtime</scope><optional>true</optional>`, mirroring `devtools`.**
`spring-boot-maven-plugin` excludes `optional` dependencies from the repackaged executable jar (same mechanism already relied on for `devtools`). This keeps Docker Compose auto-start out of any packaged/deployed jar without needing a Maven profile or manual exclusion — it only activates when running via `mvnw spring-boot:run` or an IDE, from source, with `compose.yaml` on disk.

**`compose.yaml` — single `postgres` service:**
- Image: `postgres:17-alpine` (current stable major, small image).
- Fixed dev credentials (`academia`/`academia`, db `academia`) — plaintext in a committed file is fine here: local-dev-only, not used for anything else, not a real secret. No `.env` file needed for one hardcoded dev password.
- `ports: ["5432:5432"]` — fixed host port for convenience connecting a GUI client (pgAdmin/DBeaver) manually. See Risks if this conflicts with a locally-installed Postgres.
- A named volume (`academia_pgdata`) so data survives `docker compose down` / container restarts, but not committed to the repo.
- A `pg_isready` healthcheck, so Spring Boot's readiness wait (which uses container health status when present) doesn't race the app's first connection attempt against Postgres still initializing.

**No `application.yaml` changes.** `spring-boot-docker-compose` looks for `compose.yaml`/`docker-compose.yml` at the project root by default — already satisfied by the new file's location, so no `spring.docker.compose.*` properties are needed.

## Risks / Trade-offs

- **Port 5432 already in use locally** (e.g. a native Postgres install) → `docker compose` fails to bind. Mitigation: documented in `CLAUDE.md`; fix is changing the host-side port in `compose.yaml` (e.g. `"5433:5432"`) — Spring Boot still auto-detects the actual bound port from Docker either way, so only the host-side mapping needs changing, nothing in application config.
- **Docker not running** → `spring-boot:run` fails fast with a clear Docker-connection error from `spring-boot-docker-compose`. Mitigation: documented as a prerequisite in `CLAUDE.md`.
- **First run pulls the `postgres:17-alpine` image** (one-time network delay). No mitigation needed, one-time cost.

## Migration Plan

Purely additive (new file + new optional dependency); nothing existing changes behavior. No rollback concerns — removing the dependency and `compose.yaml` fully reverts.
