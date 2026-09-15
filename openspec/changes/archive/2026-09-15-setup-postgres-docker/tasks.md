## 1. Dependency

- [x] 1.1 Add `spring-boot-docker-compose` to `pom.xml` (`<scope>runtime</scope><optional>true</optional>`, matching the existing `devtools` entry) and verify `.\mvnw.cmd compile` succeeds

## 2. Compose file

- [x] 2.1 Create `compose.yaml` at the project root with a `postgres:17-alpine` service (db/user/password `academia`, `ports: ["5432:5432"]`, a named volume for data persistence, and a `pg_isready` healthcheck) and verify `docker compose config` validates it without errors
- [x] 2.2 Verify `docker compose up -d` starts the container and it reaches `healthy` status (`docker compose ps`), then `docker compose down` (host port remapped to 5433 — see note below; port 5432 on this machine is already bound by an unrelated `mito-sales-postgres` container, matching the port-conflict risk anticipated in design.md)

## 3. Discovered fixes (unblocking end-to-end verification)

- [x] 3.0a Add `@NoRepositoryBean` to `IGenericRepo` — without it, Spring Data JPA scans it as a concrete repository with unbound generics and fails to boot against any real datasource (`Not a managed type: class java.lang.Object`); unrelated to Docker, would have broken on first real-DB run regardless (fixed by user directly)
- [x] 3.0b Add `spring.jpa.hibernate.ddl-auto: update` to `application.yaml` so Hibernate creates/updates the `curso`/`estudiante` tables from the `@Entity` classes on boot — a fresh Postgres volume otherwise has no schema at all, and every DB-backed endpoint 500s with "relation does not exist"
- [x] 3.0c Fix `CRUDImpl.update()`'s reflective setter lookup to use `int.class` instead of `id.getClass()` — `id.getClass()` is always the boxed `Integer` (Java generics can't hold a primitive), but entities use primitive `int id` per convention, so Lombok generates `setId(int)`, which `getMethod("setId", Integer.class)` can never match (NoSuchMethodException); discovered via a live `PUT /v1/cursos/{id}` 500, fixed and re-verified with a live PUT returning 200

## 4. End-to-end verification

- [x] 4.1 Run `.\mvnw.cmd spring-boot:run`, confirm in the logs that Postgres was auto-started via Docker Compose and the app boots with no datasource errors, then stop it (`DockerComposeLifecycleManager` picked up `compose.yaml`, Hikari connected successfully; app run manually, later changes picked up live via devtools restart)
- [x] 4.2 With the app running, manually exercise `curso-api` (create, list, get by id, update, delete on `/v1/cursos` via curl/Postman) and confirm each response matches its scenario in `openspec/specs/curso-api/spec.md` (all 9 scenarios verified via curl: 201 create, 200 list/get, 404 get-unknown, 200 update, 404 update-unknown, 400 missing-field, 204 delete, 404 get-after-delete)

## 5. Docs

- [x] 5.1 Update `CLAUDE.md`: remove the stale "no datasource configured" note, document the Docker Compose dev flow and the Docker-running prerequisite
