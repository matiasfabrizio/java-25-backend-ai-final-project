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

## Architecture

Standard layered package structure under `com.mfpr.academia`:

- `model` — JPA entities (`Curso`, `Estudiante`), all `@Entity` + Lombok `@Data @AllArgsConstructor @NoArgsConstructor @EqualsAndHashCode`, with an `int id` `@GeneratedValue(IDENTITY)`.
- `repository` — `IGenericRepo<T, ID> extends JpaRepository<T, ID>` is the base; each entity gets its own marker interface (`ICursoRepo`, `IEstudianteRepo`) that just extends it with no added methods yet.
- `service` / `service.impl` — a generic CRUD layer sits above the repositories:
  - `ICRUD<T, ID>` declares `save`, `update`, `findAll`, `findById`, `delete`.
  - `CRUDImpl<T, ID>` is an **abstract** base implementing `ICRUD` against a repo obtained from the abstract `getRepo()` hook. Per-entity service interfaces (`ICursoService`) extend `ICRUD<Entity, ID>`, and per-entity impls (`CursoServiceImpl`) extend `CRUDImpl<Entity, ID>` and just implement `getRepo()`, returning the entity's `@Autowired`/constructor-injected repo.
  - **Known quirk**: `CRUDImpl.update()` uses reflection to look up a setter named `"setId" + <entity simple class name>` (e.g. `setIdCurso`) to assign the ID before saving. Lombok's `@Data` only generates a plain `setId(int)`, not that name, so `update()` will throw `NoSuchMethodException` for the current entities unless/until this is fixed or entities are given a matching setter.
  - When adding a new entity, follow this same four-file pattern: `model/X`, `repository/IXRepo`, `service/IXService`, `service/impl/XServiceImpl`.
- `controller` — `@RestController`s under `/v1/...` (e.g. `CursoController` at `/v1/cursos`), injecting the matching `I<Entity>Service`. There is a `//mapper` TODO comment marking where entity↔DTO mapping is expected to go — no DTOs, mappers, or endpoint methods exist yet.
- `exception` — global error handling via `@RestControllerAdvice GlobalErrorHandler` (extends `ResponseEntityExceptionHandler`), returning a `CustomErrorResponse(datetime, message, path)` record for all handled cases. **Known quirk**: there are two `@ExceptionHandler(Exception.class)` methods (`handleDefaultExceptions` → 500, `handleModelNotFoundException` → 404) mapped to the same exception type, which Spring MVC treats as an ambiguous mapping — only be aware of this if exception handling misbehaves or fails to register.
