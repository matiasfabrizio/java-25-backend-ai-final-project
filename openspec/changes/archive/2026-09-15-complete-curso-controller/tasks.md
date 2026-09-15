## 1. Dependencies

- [x] 1.1 Add `mapstruct` and `mapstruct-processor` to `pom.xml` (runtime dependency + annotation processor path entries in `default-compile` and `default-testCompile`, alongside the existing Lombok processor path) and verify `.\mvnw.cmd compile` succeeds
- [x] 1.2 Verify MapStruct's annotation processor ran by confirming `target/generated-sources/annotations` contains a generated mapper impl class after task 3 is done

## 2. DTOs

- [x] 2.1 Add `CursoRequestDto` record (`nombre`, `siglas`, `estado`) with `@NotBlank` on `nombre` and `siglas`, and verify it compiles
- [x] 2.2 Add `CursoResponseDto` record (`id`, `nombre`, `siglas`, `estado`) and verify it compiles

## 3. Mapper

- [x] 3.1 Add `CursoMapper` interface (`@Mapper(componentModel = "spring")`) with `toEntity(CursoRequestDto)` and `toResponseDto(Curso)`, and verify `.\mvnw.cmd compile` generates `CursoMapperImpl` with no unmapped-property warnings

## 4. CRUDImpl fix

- [x] 4.1 Change `CRUDImpl.update()`'s reflective lookup from `"setId" + t.getClass().getSimpleName()` to `"setId"`, and verify by reading the generated `Curso`/`Estudiante` Lombok setters (`getMethod("setId", id.getClass())` matches `setId(int)` on both)
- [x] 4.2 Change `CRUDImpl.findById()` to throw `ModelNotFoundException` (instead of the bare `.orElseThrow()`'s default `NoSuchElementException`) so it's routed to `GlobalErrorHandler.handleModelNotFoundException` (now scoped to `ModelNotFoundException.class`) and produces a 404, and verify by reading the updated method

## 5. Controller

- [x] 5.1 Implement `POST /v1/cursos` (create): `@Valid @RequestBody CursoRequestDto` in, map to entity, `cursoService.save(...)`, map to `CursoResponseDto`, return 201
- [x] 5.2 Implement `GET /v1/cursos` (list): map `cursoService.findAll()` to a list of `CursoResponseDto`, return 200
- [x] 5.3 Implement `GET /v1/cursos/{id}`: map `cursoService.findById(id)` to `CursoResponseDto`, return 200 (relies on existing `GlobalErrorHandler` for the 404 path)
- [x] 5.4 Implement `PUT /v1/cursos/{id}`: `@Valid @RequestBody CursoRequestDto` in, map to entity, `cursoService.update(id, ...)`, map to `CursoResponseDto`, return 200
- [x] 5.5 Implement `DELETE /v1/cursos/{id}`: `cursoService.delete(id)`, return 204
- [x] 5.6 Verify `.\mvnw.cmd compile` succeeds with all five endpoints in place and the `//mapper` TODO comment removed
