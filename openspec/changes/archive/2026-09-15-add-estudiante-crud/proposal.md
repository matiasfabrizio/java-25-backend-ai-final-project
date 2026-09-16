## Why

`Estudiante` currently only has a `model`/`repository` pair — there's no way to manage students over HTTP. `RegistrarMatricula` already references students by id, so the academy needs a way to create and maintain `Estudiante` records directly, the same way it already can for `Curso`.

## What Changes

- Add `IEstudianteService` / `EstudianteServiceImpl` following the generic `ICRUD`/`CRUDImpl` pattern used by `CursoServiceImpl` (no entity-specific override needed — `Estudiante` has no relationships).
- Add `EstudianteRequestDto` / `EstudianteResponseDto` and a plain-interface MapStruct `EstudianteMapper` (no injected repos needed, matching `CursoMapper`, since `Estudiante` doesn't reference any other entity).
- Add `EstudianteController` under `/v1/estudiantes` implementing full CRUD (`POST`, `GET`, `GET /{id}`, `PUT /{id}`, `DELETE /{id}`), following `CursoController` as the template.

## Capabilities

### New Capabilities
- `estudiante-api`: REST CRUD for managing `Estudiante` (student) records.

### Modified Capabilities
(none — no existing capability's requirements change; `registrar-matricula-api` already references `Estudiante` by id only, and that isn't changing)

## Impact

- **New files**: `service/IEstudianteService`, `service/impl/EstudianteServiceImpl`, `dto/EstudianteRequestDto`, `dto/EstudianteResponseDto`, `dto/EstudianteMapper`, `controller/EstudianteController`.
- **No changes** to `model/Estudiante.java` or `repository/IEstudianteRepo.java` (already staged/committed, already fit for this).
- **No changes** to `Curso`, `RegistrarMatricula`, or existing endpoints.
