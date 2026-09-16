## 1. Service

- [x] 1.1 Add `service/IEstudianteService.java` extending `ICRUD<Estudiante, Integer>`
- [x] 1.2 Add `service/impl/EstudianteServiceImpl.java` extending `CRUDImpl<Estudiante, Integer>`, implementing only `getRepo()` (returning the existing `IEstudianteRepo`), and verify it wires up (Spring context starts)

## 2. DTOs and Mapper

- [x] 2.1 Add `dto/EstudianteRequestDto.java` (`@NotBlank nombres`, `@NotBlank apellidos`, `@NotBlank dni`, `int edad`) and `dto/EstudianteResponseDto.java` (`id`, `nombres`, `apellidos`, `dni`, `edad`), matching `CursoRequestDto`/`CursoResponseDto`'s shape
- [x] 2.2 Add `dto/EstudianteMapper.java` as a plain MapStruct interface (`@Mapper(componentModel = "spring")`, `id` ignored on `toEntity`), matching `CursoMapper` exactly — no injected repos needed since `Estudiante` has no relationships

## 3. Controller

- [x] 3.1 Add `controller/EstudianteController.java` under `/v1/estudiantes` implementing `POST`, `GET`, `GET /{id}`, `PUT /{id}`, `DELETE /{id}`, following `CursoController` exactly (inject `IEstudianteService` + `EstudianteMapper`, `@Valid @RequestBody` on create/update)

## 4. Verification

- [x] 4.1 Run `.\mvnw.cmd test` and confirm it passes, or confirm it fails identically to the known pre-existing `mvnw test` gap documented in `CLAUDE.md` (unrelated to this change) — confirmed: same `contextLoads` / "Failed to determine a suitable driver class" failure, unrelated to this change
- [x] 4.2 Manually exercise the full CRUD flow end to end (create, get by id, list, update, delete, plus 400 on missing required fields and 404 on unknown id) against the local Postgres via `spring-boot:run`, confirming each scenario in `specs/estudiante-api/spec.md` — all scenarios verified via curl
