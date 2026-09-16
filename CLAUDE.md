# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Spring Boot 4.1.1 REST API (`academia`) for managing a school/academy domain (`Curso`, `Estudiante`, `RegistrarMatricula`/`DetalleMatricula`). Java 25, Maven, PostgreSQL, Lombok. `Curso`, `Estudiante`, and `RegistrarMatricula` all have full REST CRUD.

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

**Local database**: a `compose.yaml` at the project root defines a single `postgres:17-alpine` service (db/user/password `academia`, host port `5433` — remapped from Postgres's default `5432` because that's commonly already taken locally). `spring-boot-docker-compose` (optional/runtime dependency, excluded from the packaged jar like `devtools`) auto-starts it whenever the app runs from source (`spring-boot:run` / IDE run) and auto-configures the datasource from the running container — no manual `spring.datasource.*` properties, no credentials to maintain. **Prerequisite: Docker must be running.** `spring.jpa.hibernate.ddl-auto: update` in `application.yaml` creates/updates tables from the `@Entity` classes on boot (dev-only convenience; there's no migration tool like Flyway/Liquibase yet and no production datasource configured).

**Known gap — `.\mvnw.cmd test` currently fails**: Spring Boot's docker-compose support skips itself during tests by default (`spring.docker.compose.skip.in-tests=true`), and no test-scoped datasource is configured, so `AcademiaApplicationTests.contextLoads` fails with "Failed to determine a suitable driver class" even on a clean checkout — this is pre-existing and not caused by any specific feature branch. Until it's fixed (e.g. by setting `spring.docker.compose.skip.in-tests: false`), verify changes manually against the running app (`spring-boot:run` + `curl`/Postman) instead of relying on `mvnw test`.

## Workflow

For any task that changes code (feature, bug fix, refactor, text/doc update), work this way without needing to be asked each time:

1. Create a new branch off `main` before editing (e.g. `feat/<short-name>` or `fix/<short-name>`) — every task gets its own branch, however small.
2. Make the change.
3. Run `.\mvnw.cmd test`. If it fails, stop and report the failure instead of continuing or committing.
4. If tests pass, commit locally with a descriptive message.

Do **not** push to the remote or merge/switch back to `main` without explicit request — those remain confirm-first since they affect shared state. This is a plain sequential workflow (each step depends on the last), so do it directly rather than delegating steps to subagents.

## Architecture

Standard layered package structure under `com.mfpr.academia`:

- `model` — JPA entities (`Curso`, `Estudiante`, `RegistrarMatricula`, `DetalleMatricula`), all `@Entity` + Lombok `@Data @AllArgsConstructor @NoArgsConstructor @EqualsAndHashCode`, with an `int id` `@GeneratedValue(IDENTITY)`. `RegistrarMatricula` (a student's enrollment: `fechaInscripcion`, `estudiante`, `estado`) owns a `@OneToMany(mappedBy = "registrarMatricula", cascade = CascadeType.ALL, orphanRemoval = true) List<DetalleMatricula> detalles`; each `DetalleMatricula` (`curso`, `aula`) holds the owning `@ManyToOne RegistrarMatricula registrarMatricula` FK. **Any bidirectional relationship field must be annotated `@EqualsAndHashCode.Exclude` and `@ToString.Exclude` on both sides of the back-reference** (see `DetalleMatricula.registrarMatricula` / `RegistrarMatricula.detalles`) — otherwise Lombok's `@Data`-generated `equals`/`hashCode`/`toString` recurse into each other and stack-overflow; entities with no relationships (`Curso`, `Estudiante`) don't need this.
- `repository` — `IGenericRepo<T, ID> extends JpaRepository<T, ID>` is the base, annotated `@NoRepositoryBean` (required — without it Spring Data JPA scans it as a concrete repository with unbound generics and fails to boot against any real datasource); each entity gets its own marker interface (`ICursoRepo`, `IEstudianteRepo`, `IRegistrarMatriculaRepo`) that just extends it with no added methods yet. `DetalleMatricula` has no repo of its own — it's only ever reached through its parent's cascade (see `model` above).
- `service` / `service.impl` — a generic CRUD layer sits above the repositories:
  - `ICRUD<T, ID>` declares `save`, `update`, `findAll`, `findById`, `delete`.
  - `CRUDImpl<T, ID>` is an **abstract** base implementing `ICRUD` against a repo obtained from the abstract `getRepo()` hook. Per-entity service interfaces (`ICursoService`, `IRegistrarMatriculaService`) extend `ICRUD<Entity, ID>`, and per-entity impls (`CursoServiceImpl`, `RegistrarMatriculaServiceImpl`) extend `CRUDImpl<Entity, ID>` and just implement `getRepo()`, returning the entity's `@Autowired`/constructor-injected repo. `update()` looks up the entity's `setId(int)` setter via reflection to assign the ID before saving — hardcoded to `int.class` rather than `id.getClass()`, since the generic `ID` type parameter is always boxed (`Integer`) at runtime but entities use primitive `int id` (see `model` above), so `Integer.class` could never match Lombok's generated `setId(int)`. `findById()` and `delete()` both throw `ModelNotFoundException` (see `exception` below) when no row matches — `delete()` checks existence via `findById()` before calling the repo, so deleting an already-gone/unknown id 404s instead of silently succeeding. For entities with owned child collections (`RegistrarMatricula.detalles`), `update()`'s plain `repo.save()` on a detached graph fully replaces the child collection for free: incoming children never carry an id (the request DTO omits it), so Hibernate's merge treats every one as new (insert) and every previously-managed child absent from the incoming list as an orphan (delete) — no service-level override needed as long as the parent's `@OneToMany` has `cascade = ALL, orphanRemoval = true`.
  - When adding a new entity, follow this same four-file pattern: `model/X`, `repository/IXRepo`, `service/IXService`, `service/impl/XServiceImpl`.
- `dto` — per-entity request/response DTOs as Java records (e.g. `CursoRequestDto`, `CursoResponseDto`, `EstudianteRequestDto`, `EstudianteResponseDto`; the request DTO omits the server-generated `id`), plus a MapStruct `@Mapper(componentModel = "spring")` mapper per entity mapping DTO ↔ entity. Validation constraints (`@NotBlank`, `@NotNull`, `@NotEmpty`, `@Valid` on nested lists) live on the request DTO. `CursoMapper` and `EstudianteMapper` are plain interfaces (no relationships to resolve). `RegistrarMatriculaMapper` is instead an **abstract class** with `IEstudianteRepo`/`ICursoRepo` field-injected (`@Autowired`), because it must resolve `estudianteId`/`cursoId` (ints in the DTO, kept flat rather than nesting full related objects) to actual entities — throwing `ModelNotFoundException` (→ 404) when an id doesn't exist — and uses `@AfterMapping` to set each mapped `DetalleMatricula`'s back-reference to its parent (JPA never populates the inverse side of a `mappedBy` relationship automatically). Use this abstract-class-with-injected-repos pattern whenever a new entity's DTO references another entity by id; use a plain interface otherwise.
- `controller` — `@RestController`s under `/v1/...` injecting the matching `I<Entity>Service` and mapper. `CursoController` (`/v1/cursos`), `EstudianteController` (`/v1/estudiantes`), and `RegistrarMatriculaController` (`/v1/registrar-matriculas`) all implement full CRUD (`POST`, `GET`, `GET /{id}`, `PUT /{id}`, `DELETE /{id}`) following this DTO/mapper pattern — use any of them as the template for new entity controllers.
- `exception` — global error handling via `@RestControllerAdvice GlobalErrorHandler` (extends `ResponseEntityExceptionHandler`), returning a `CustomErrorResponse(datetime, message, path)` record for all handled cases. `ModelNotFoundException` (a `RuntimeException`) → 404 via `handleModelNotFoundException`; any other unhandled exception → 500 via `handleDefaultExceptions`.
