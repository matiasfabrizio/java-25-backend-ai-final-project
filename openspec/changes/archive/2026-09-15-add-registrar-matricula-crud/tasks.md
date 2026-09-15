## 1. Model

- [x] 1.1 Rework `model/DetalleMatricula.java` into a full `@Entity` (`id`, `@ManyToOne Curso curso`, `@ManyToOne RegistrarMatricula registrarMatricula` via `registrar_matricula_id`, `String aula`) with `@Data @AllArgsConstructor @NoArgsConstructor @EqualsAndHashCode`, matching `Curso`/`Estudiante` conventions
- [x] 1.2 Update `model/RegistrarMatricula.java`: add `@OneToMany(mappedBy = "registrarMatricula", cascade = CascadeType.ALL, orphanRemoval = true) List<DetalleMatricula> detalles`, `@ManyToOne Estudiante estudiante` (already present, keep), and verify the app boots (`.\mvnw.cmd spring-boot:run`) creating `registrar_matricula`/`detalle_matricula` tables via `ddl-auto: update`

## 2. Repository

- [x] 2.1 Verify `repository/IRegistrarMatriculaRepo.java` (already staged) compiles against the reworked entity; no `IDetalleMatriculaRepo` is added (see design.md)

## 3. Service

- [x] 3.1 Add `service/IRegistrarMatriculaService.java` extending `ICRUD<RegistrarMatricula, Integer>`
- [x] 3.2 Add `service/impl/RegistrarMatriculaServiceImpl.java` extending `CRUDImpl<RegistrarMatricula, Integer>`, implementing only `getRepo()`, and verify it wires up (Spring context starts)

## 4. DTOs and Mapper

- [x] 4.1 Add `dto/DetalleMatriculaRequestDto.java` (`@NotNull cursoId`, `@NotBlank aula`) and `dto/DetalleMatriculaResponseDto.java` (`id`, `cursoId`, `aula`)
- [x] 4.2 Add `dto/RegistrarMatriculaRequestDto.java` (`@NotNull fechaInscripcion`, `@NotNull estudianteId`, `boolean estado`, `@NotEmpty @Valid List<DetalleMatriculaRequestDto> detalles`) and `dto/RegistrarMatriculaResponseDto.java` (`id`, `fechaInscripcion`, `estudianteId`, `estado`, `List<DetalleMatriculaResponseDto> detalles`)
- [x] 4.3 Add `dto/RegistrarMatriculaMapper.java` as an abstract MapStruct class with `IEstudianteRepo`/`ICursoRepo` injected: resolve `estudianteId`/`cursoId` to entities via `findById(...).orElseThrow(ModelNotFoundException::new)`, wire each detalle's back-reference to the parent via `@AfterMapping`, and map entity -> response DTOs (ids only, no nested objects) per design.md
- [x] 4.4 Verify with a quick manual `POST` (via `curl`/Postman) that an unknown `estudianteId` or `cursoId` returns 404 through the existing `GlobalErrorHandler`

## 5. Controller

- [x] 5.1 Add `controller/RegistrarMatriculaController.java` under `/v1/registrar-matriculas` implementing `POST`, `GET`, `GET /{id}`, `PUT /{id}`, `DELETE /{id}`, following `CursoController` exactly (inject `IRegistrarMatriculaService` + `RegistrarMatriculaMapper`, `@Valid @RequestBody` on create/update)

## 6. Verification

- [ ] 6.1 Run `.\mvnw.cmd test` and confirm it passes — **blocked**: `AcademiaApplicationTests.contextLoads` fails identically on a clean `main` checkout (confirmed via a throwaway worktree), because Spring Boot's docker-compose auto-config skips itself during tests by default (`spring.docker.compose.skip.in-tests=true`) and no test datasource is configured. Pre-existing gap, unrelated to this change; user chose to defer fixing it and rely on manual verification (6.2) instead.
- [x] 6.2 Manually exercise the full CRUD flow end to end (create with 2 detalles, get by id, list, update replacing detalles, delete) against the local Postgres via `spring-boot:run`, confirming each scenario in `specs/registrar-matricula-api/spec.md` — all scenarios verified via curl, including 400/404 on create, 404 on get/delete of unknown id, and full detalle replacement on update (confirmed against the DB directly)
