## Context

See proposal.md - Why/What Changes. Relevant existing state:

- `model/RegistrarMatricula.java` and `model/DetalleMatricula.java` are already staged (uncommitted) but not usable as-is: `DetalleMatricula` isn't an `@Entity`, and `RegistrarMatricula.detalleMatriculas` has no relationship mapping.
- The generic CRUD stack (`ICRUD`/`CRUDImpl`, `IGenericRepo`) is untouched by every entity so far (`CursoServiceImpl` adds nothing beyond `getRepo()`), and `CRUDImpl.update()` works by reflection-setting the id on a fresh entity graph, then `repo.save()`. This is the first entity with relationships, so this design has to confirm that generic path still produces correct behavior rather than assume it.
- Only `Curso` has a full DTO/mapper/controller stack; `Estudiante` has model + repo only. This change reuses `Estudiante` and `Curso` by id lookup, it does not add CRUD for `Estudiante`.

## Goals / Non-Goals

**Goals:**
- A valid, persistable one-to-many `RegistrarMatricula` -> `DetalleMatricula` mapping, owned by the parent (create/update/delete of the parent manages the children; no independent detalle lifecycle).
- Full CRUD for `RegistrarMatricula` via the same layered pattern as `Curso`, with no changes to `ICRUD`/`CRUDImpl`.
- FK references (`estudianteId`, `cursoId`) resolved to existing rows at the DTO->entity boundary, 404ing through the existing `ModelNotFoundException` path when missing.

**Non-Goals:**
- CRUD endpoints for `DetalleMatricula` on its own (no `/v1/detalle-matriculas`) - it's only ever created/read/updated/deleted as part of its parent.
- CRUD endpoints for `Estudiante` (out of scope of this change; already absent from the codebase).
- Partial/`PATCH` update of a single detalle - `PUT` on the parent replaces the whole detalles set, matching how `CursoController.update` replaces the whole `Curso`.

## Decisions

**Entity mapping**: `DetalleMatricula` becomes a full `@Entity` (own `id`, own table), not an `@Embeddable`/`@ElementCollection`, because it participates in a real one-to-many query/identity story (returned with its own generated id) rather than being a value object. `RegistrarMatricula` owns the relationship declaratively but `DetalleMatricula` is the FK owner in the schema (`@ManyToOne` holds `registrar_matricula_id`), matching standard JPA bidirectional one-to-many:

```java
// RegistrarMatricula
@OneToMany(mappedBy = "registrarMatricula", cascade = CascadeType.ALL, orphanRemoval = true)
private List<DetalleMatricula> detalles;

// DetalleMatricula
@ManyToOne
@JoinColumn(name = "registrar_matricula_id", nullable = false)
private RegistrarMatricula registrarMatricula;

@ManyToOne
@JoinColumn(name = "curso_id", nullable = false)
private Curso curso;
```

Alternative considered: `@ElementCollection` of an `@Embeddable DetalleMatricula` (curso + aula only, no own id/table). Rejected because the spec requires each detalle to come back with its own generated id (useful for future direct reference), and `@ElementCollection` rows have no identity of their own, only composite (parent id + values).

**No separate `IDetalleMatriculaRepo`**: children are only ever reached through their parent (`cascade = ALL`, `orphanRemoval = true`), so a standalone repo would be dead surface area. Not created.

**Update replaces the full detalles list, no CRUDImpl changes needed**: `CRUDImpl.update()` builds a detached entity graph (mapper output, id reflected in) and calls `repo.save()`, which resolves to `entityManager.merge()` since the id is non-default. With `cascade = ALL, orphanRemoval = true`, Hibernate's merge synchronizes the managed children collection against the incoming one: incoming `DetalleMatricula` objects from a request always have `id == 0` (the request DTO never carries a detalle id, matching how `CursoRequestDto` omits the entity id), so merge treats every incoming detalle as new (insert) and every previously-managed detalle not present in the incoming list as an orphan (delete). Net effect: update always replaces the full detalles set, which matches the spec's "detalles omitted are removed; detalles present are (re)created" requirement with zero service-layer code. `RegistrarMatriculaServiceImpl` therefore only implements `getRepo()`, same as `CursoServiceImpl`.

**Mapper sets the child back-reference and resolves FKs**: `mappedBy` means `DetalleMatricula.registrarMatricula` is the actual FK-owning field, but JPA never auto-populates the inverse side. `RegistrarMatriculaMapper` becomes an abstract class (MapStruct supports this the same as an interface) with `IEstudianteRepo`/`ICursoRepo` field-injected, so it can:
- resolve `estudianteId` -> `Estudiante` and each detalle's `cursoId` -> `Curso`, throwing `ModelNotFoundException` (already mapped to 404 by `GlobalErrorHandler`) when the id doesn't exist;
- use `@AfterMapping` to set `detalle.setRegistrarMatricula(parent)` on every mapped detalle, so the FK column is populated on insert.

This keeps `RegistrarMatriculaController` identical in shape to `CursoController` (call `mapper.toEntity(request)`, pass to `service.save`/`update`), with all relationship wiring inside the mapper rather than the controller or service.

**DTOs stay flat (ids, not nested objects)**: `RegistrarMatriculaRequestDto`/`ResponseDto` reference `estudianteId` (int) rather than embedding `Estudiante`; `DetalleMatriculaRequestDto`/`ResponseDto` reference `cursoId` (int) rather than embedding `Curso`. Matches the existing flat-DTO style (`CursoRequestDto`/`ResponseDto` carry no nested objects) and avoids over-fetching related entities the client didn't ask for. A future change can add an enriched response (e.g., course name) if a caller needs it.

**URL**: `/v1/registrar-matriculas`, plural of the entity name, matching `/v1/cursos`.

## Risks / Trade-offs

- [Every `PUT` reinserts all detalles rather than diffing/updating in place] -> Acceptable: ids are not client-stable across updates anyway (the request never carries a detalle id), and this comes for free from cascade config rather than added code; revisit only if per-detalle history/id-stability is ever required.
- [`RegistrarMatriculaMapper` needs repo access, unlike `CursoMapper` which is a pure interface] -> Contained to the mapper; controller/service stay unchanged from the `Curso` pattern.
- [Deleting a `RegistrarMatricula` cascades to delete its `DetalleMatricula` rows] -> Intentional per proposal (enrollment line items have no meaning without their parent); matches `orphanRemoval = true`.
