# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Spring Boot 4.1.1 REST API (`academia`) for managing a school/academy domain (`Curso`, `Estudiante`). Java 25, Maven, PostgreSQL, Lombok. Early scaffolding stage — most layers exist as generic building blocks with little business logic yet.

## Commands

Use the Maven wrapper (`mvnw.cmd` on Windows / `./mvnw` on Unix shells) — no local Maven install required.

```
.\mvnw.cmd clean package        # build
.\mvnw.cmd spring-boot:run      # run the app
.\mvnw.cmd test                 # run all tests
.\mvnw.cmd test -Dtest=AcademiaApplicationTests            # run one test class
.\mvnw.cmd test -Dtest=AcademiaApplicationTests#contextLoads  # run one test method
```

`spring-boot-devtools` is on the classpath, so `spring-boot:run` picks up recompiled classes automatically.

Note: `src/main/resources/application.yaml` only sets `spring.application.name`. There is no `spring.datasource.*` configured, even though the PostgreSQL driver and Spring Data JPA are dependencies — a datasource must be supplied (e.g. via env vars or an `application-local.yaml`/profile) before the app can start against a real database.

## Workflow

For any task that changes code (feature, bug fix, refactor, text/doc update), work this way without needing to be asked each time:

1. Create a new branch off `main` before editing (e.g. `feat/<short-name>` or `fix/<short-name>`) — every task gets its own branch, however small.
2. Make the change.
3. Run `.\mvnw.cmd test`. If it fails, stop and report the failure instead of continuing or committing.
4. If tests pass, commit locally with a descriptive message.

Do **not** push to the remote or merge/switch back to `main` without explicit request — those remain confirm-first since they affect shared state. This is a plain sequential workflow (each step depends on the last), so do it directly rather than delegating steps to subagents.

## Architecture

Standard layered package structure under `com.mfpr.academia`:

- `model` — JPA entities (`Curso`, `Estudiante`), all `@Entity` + Lombok `@Data @AllArgsConstructor @NoArgsConstructor @EqualsAndHashCode`, with an `int id` `@GeneratedValue(IDENTITY)`.
- `repository` — `IGenericRepo<T, ID> extends JpaRepository<T, ID>` is the base; each entity gets its own marker interface (`ICursoRepo`, `IEstudianteRepo`) that just extends it with no added methods yet.
- `service` / `service.impl` — a generic CRUD layer sits above the repositories:
  - `ICRUD<T, ID>` declares `save`, `update`, `findAll`, `findById`, `delete`.
  - `CRUDImpl<T, ID>` is an **abstract** base implementing `ICRUD` against a repo obtained from the abstract `getRepo()` hook. Per-entity service interfaces (`ICursoService`) extend `ICRUD<Entity, ID>`, and per-entity impls (`CursoServiceImpl`) extend `CRUDImpl<Entity, ID>` and just implement `getRepo()`, returning the entity's `@Autowired`/constructor-injected repo. `update()` looks up the entity's plain `setId(ID)` setter via reflection to assign the ID before saving; `findById()` throws `ModelNotFoundException` (see `exception` below) when no row matches.
  - When adding a new entity, follow this same four-file pattern: `model/X`, `repository/IXRepo`, `service/IXService`, `service/impl/XServiceImpl`.
- `dto` — per-entity request/response DTOs as Java records (e.g. `CursoRequestDto`, `CursoResponseDto`; the request DTO omits the server-generated `id`), plus a MapStruct `@Mapper(componentModel = "spring")` interface per entity (e.g. `CursoMapper`) mapping DTO ↔ entity. Validation constraints (`@NotBlank`, etc.) live on the request DTO.
- `controller` — `@RestController`s under `/v1/...` injecting the matching `I<Entity>Service` and `XMapper`. `CursoController` (`/v1/cursos`) implements full CRUD (`POST`, `GET`, `GET /{id}`, `PUT /{id}`, `DELETE /{id}`) following this DTO/mapper pattern — use it as the template for new entity controllers (e.g. `EstudianteController`, not yet implemented).
- `exception` — global error handling via `@RestControllerAdvice GlobalErrorHandler` (extends `ResponseEntityExceptionHandler`), returning a `CustomErrorResponse(datetime, message, path)` record for all handled cases. `ModelNotFoundException` (a `RuntimeException`) → 404 via `handleModelNotFoundException`; any other unhandled exception → 500 via `handleDefaultExceptions`.
