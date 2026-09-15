## Why

`Curso` and `Estudiante` exist independently with no way to record which student enrolled in which courses. The academy needs to register a student's enrollment (`RegistrarMatricula`) and the courses/classrooms that make it up (`DetalleMatricula`), and manage those enrollments through the same REST CRUD pattern already established for `Curso`.

## What Changes

- Turn the currently uncommitted, non-persistable `RegistrarMatricula`/`DetalleMatricula` model scaffolding into a working JPA relationship: `RegistrarMatricula` (id, `fechaInscripcion`, `estudiante`, `estado`) has many `DetalleMatricula` (id, `curso`, `aula`), each `DetalleMatricula` belonging to exactly one `RegistrarMatricula` (bidirectional one-to-many / many-to-one, parent-owned lifecycle).
- Add `IRegistrarMatriculaService` / `RegistrarMatriculaServiceImpl` following the generic `ICRUD`/`CRUDImpl` pattern used by `CursoServiceImpl` (no entity-specific override needed).
- Add request/response DTOs and a MapStruct mapper for `RegistrarMatricula`, referencing related entities by id (`estudianteId` on the parent, `cursoId` per detalle line) rather than embedding full nested objects, matching how the rest of the API keeps payloads flat.
- Add `RegistrarMatriculaController` under `/v1/registrar-matriculas` implementing full CRUD (`POST`, `GET`, `GET /{id}`, `PUT /{id}`, `DELETE /{id}`), following `CursoController` as the template.

## Capabilities

### New Capabilities
- `registrar-matricula-api`: REST CRUD for registering and managing student enrollments (`RegistrarMatricula`) and their per-course line items (`DetalleMatricula`).

### Modified Capabilities
(none — `Curso` and `Estudiante` behavior is unchanged; `RegistrarMatricula`/`DetalleMatricula` reference them by id only)

## Impact

- **New files**: `service/IRegistrarMatriculaService`, `service/impl/RegistrarMatriculaServiceImpl`, `dto/RegistrarMatriculaRequestDto`, `dto/RegistrarMatriculaResponseDto`, `dto/DetalleMatriculaRequestDto`, `dto/DetalleMatriculaResponseDto`, `dto/RegistrarMatriculaMapper`, `controller/RegistrarMatriculaController`.
- **Modified files**: `model/RegistrarMatricula.java`, `model/DetalleMatricula.java` (currently staged, uncommitted — reworked into a valid JPA relationship), `repository/IRegistrarMatriculaRepo.java` (already staged, unchanged).
- **New file**: `repository/IDetalleMatriculaRepo.java` (needed only if `DetalleMatricula` requires direct repo access; otherwise it is persisted solely via cascade from `RegistrarMatricula` — decided in design.md).
- **Database**: adds `registrar_matricula` and `detalle_matricula` tables via `ddl-auto: update`, plus FKs to `curso` and `estudiante`.
- **No changes** to `Curso`, `Estudiante`, or existing endpoints.
