## Context

`CursoController` currently has no endpoints and no DTOs — see proposal.md for why. `Curso` (entity) has three mutable fields (`nombre`, `siglas`, `estado`) plus a generated `id`. `ICursoService` already exposes the full `ICRUD` contract (`save`, `update`, `findAll`, `findById`, `delete`) via `CursoServiceImpl`, so the controller only needs to call through to it and translate between DTOs and the entity.

`CRUDImpl.update()` currently does `t.getClass().getMethod("setId" + t.getClass().getSimpleName(), id.getClass())` — a reflective lookup for a setter name (`setIdCurso`) that Lombok's `@Data` never generates (it generates `setId(int)`, named after the field, not the class).

## Goals / Non-Goals

**Goals:**
- Full CRUD surface on `/v1/cursos` backed by request/response DTOs, matching `specs/curso-api/spec.md`.
- Fix `CRUDImpl.update()` so it works for `Curso` (and transitively every other `CRUDImpl`-based entity) without requiring any per-entity naming convention.

**Non-Goals:**
- `EstudianteController` or any other entity's controller.
- Datasource configuration, the pre-existing ambiguous `@ExceptionHandler(Exception.class)` pair in `GlobalErrorHandler`, or any other pre-existing gap not directly required to complete `CursoController`.
- Automated tests and running `mvn test` — explicitly deferred to a follow-up step per user request; not part of this change's tasks.

## Decisions

**DTOs as Java records, not Lombok classes.**
`CursoRequestDto(String nombre, String siglas, boolean estado)` and `CursoResponseDto(int id, String nombre, String siglas, boolean estado)`. Records are immutable, need no Lombok annotations, and are a native Java 25 feature — the project already uses `record` for `CustomErrorResponse`, so this follows an established convention. `CursoRequestDto` excludes `id` (server-generated); `CursoResponseDto` includes it.

**MapStruct for entity <-> DTO mapping.**
Add `mapstruct` + `mapstruct-processor` to `pom.xml`, placed alongside the existing Lombok annotation-processor-path entries in the `maven-compiler-plugin` config (Lombok must run before/with MapStruct's processor; the standard combo is listing both processor paths in `default-compile` and `default-testCompile`). One `CursoMapper` interface:

```java
@Mapper(componentModel = "spring")
public interface CursoMapper {
    Curso toEntity(CursoRequestDto dto);
    CursoResponseDto toResponseDto(Curso curso);
}
```

`componentModel = "spring"` makes MapStruct generate `CursoMapperImpl` as a `@Component`, so it's constructor-injectable into `CursoController` the same way `ICursoService` already is (`@RequiredArgsConstructor`). Alternatives considered: hand-written mapper (rejected — user asked for a tool), ModelMapper (rejected — reflection-based, slower, less type-safe, no compile-time verification that all DTO fields are mapped).

**Fix `CRUDImpl.update()` by looking up the plain `setId` setter.**
Change the reflective lookup to `t.getClass().getMethod("setId", id.getClass())`. This is the setter Lombok's `@Data` already generates for the `id` field on every entity. One-line change in the shared base class; no entity needs to change. See proposal.md for why this is preferred over renaming entity id fields.

**Controller shape.**
Standard `@RestController` methods returning `ResponseEntity<CursoResponseDto>` / `ResponseEntity<List<CursoResponseDto>>`, using `@Valid @RequestBody CursoRequestDto` for input validation (via the existing `spring-boot-starter-validation` dependency and `GlobalErrorHandler.handleMethodArgumentNotValid`, both already present). `POST` returns 201, `GET`/`PUT` return 200, `DELETE` returns 204. `findById`'s existing `orElseThrow()` (→ `NoSuchElementException`) is left as-is and continues to rely on `GlobalErrorHandler` to surface it as 404 — no change needed there for this feature to compile and route correctly.

## Risks / Trade-offs

- **`GlobalErrorHandler` has two `@ExceptionHandler(Exception.class)` methods** (pre-existing, documented in `CLAUDE.md`). Spring MVC's ambiguous-mapping behavior for duplicate exception-type handlers means one of the two may not actually be reachable at runtime → 404 responses for "not found" scenarios in the spec could come back as 500 instead, until that pre-existing bug is fixed separately. Not fixed here (non-goal); flagging so it's not mistaken for a regression introduced by this change.
- **No automated tests in this change** → correctness of the new endpoints and the `CRUDImpl` fix is unverified by CI/build; deferred per explicit user request to a follow-up step.
- **MapStruct + Lombok annotation-processor ordering**: both processors need to be on the `annotationProcessorPaths` list for `default-compile`/`default-testCompile` in `pom.xml`, or Lombok-generated getters/setters on `Curso` won't be visible to MapStruct's processor at compile time.

## Migration Plan

Not applicable — additive feature (new endpoints, new files) plus a one-line bug fix in already-broken code (no working caller of `update()` exists yet, so no behavior regresses).
