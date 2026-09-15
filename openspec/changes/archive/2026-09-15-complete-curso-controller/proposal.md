## Why

`CursoController` currently only injects `ICursoService` — it has no endpoints, no request/response DTOs, and the `//mapper` comment marks where entity↔DTO mapping was left as a TODO. `Curso` is exposed nowhere over HTTP yet. Additionally, `CRUDImpl.update()` looks up a setter named `setId<EntityName>` (e.g. `setIdCurso`) via reflection, but Lombok's `@Data` generates `setId(int)` for the `id` field — so `update()` throws `NoSuchMethodException` at runtime for every entity, including the `PUT` endpoint this change adds.

## What Changes

- Add `CursoRequestDto` (create/update input, no `id`) and `CursoResponseDto` (output, includes `id`) as Java records.
- Add a MapStruct `CursoMapper` interface (`@Mapper(componentModel = "spring")`) to convert `CursoRequestDto` → `Curso` and `Curso` → `CursoResponseDto`.
- Add `mapstruct` and `mapstruct-processor` to `pom.xml` (dependency + annotation processor path, alongside the existing Lombok processor path).
- Implement `CursoController` endpoints under `/v1/cursos`: `POST` (create), `GET` (list), `GET /{id}`, `PUT /{id}` (update), `DELETE /{id}`, using `ICursoService` and `CursoMapper`.
- **BREAKING (pre-release, no external consumers yet)**: Fix `CRUDImpl.update()` to look up the standard `setId` setter instead of `setId<EntityName>`. This is a one-line change in the shared generic CRUD base class, so it also fixes `update()` for `Estudiante` and any future entity — not just `Curso`.

## Capabilities

### New Capabilities
- `curso-api`: HTTP API for managing `Curso` resources (`/v1/cursos`) — create, list, get by id, update, delete, backed by request/response DTOs.

### Modified Capabilities
_None — no existing specs cover this yet; the `CRUDImpl.update()` fix is an implementation detail needed to satisfy the update requirement introduced by `curso-api`, not a change to a previously specified behavior._

## Impact

- **New files**: `CursoRequestDto`, `CursoResponseDto`, `CursoMapper` (MapStruct interface).
- **Modified files**: `CursoController` (endpoints added), `CRUDImpl` (setter lookup fix), `pom.xml` (new MapStruct dependency + annotation processor config).
- **Dependencies**: adds `mapstruct` (runtime) and `mapstruct-processor` (annotation processor) to `pom.xml`.
- **Behavior**: `PUT /v1/cursos/{id}` becomes usable for the first time (previously would 500 via the generic exception handler, once reachable). Same fix applies transitively to any future `CRUDImpl`-based service's `update()`.
- **Out of scope**: `EstudianteController` (not requested), the pre-existing ambiguous `@ExceptionHandler(Exception.class)` mapping in `GlobalErrorHandler`, and datasource configuration.
