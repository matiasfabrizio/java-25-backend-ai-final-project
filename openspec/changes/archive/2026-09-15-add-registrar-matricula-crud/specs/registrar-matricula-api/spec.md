## Purpose

Exposes `RegistrarMatricula` resources over HTTP under `/v1/registrar-matriculas`, letting clients register a student's enrollment together with its per-course line items (`DetalleMatricula`), and list, retrieve, update, or delete that enrollment afterward.

## ADDED Requirements

### Requirement: Create a RegistrarMatricula
The system SHALL accept `POST /v1/registrar-matriculas` with a payload of `fechaInscripcion`, `estudianteId`, `estado`, and a non-empty list of detalles (each with `cursoId` and `aula`), and create a new `RegistrarMatricula` together with its `DetalleMatricula` line items, returning the created resource including its generated id and the generated id of each detalle.

The system SHALL reject a payload missing `fechaInscripcion`, `estudianteId`, or an empty/missing detalles list with a 400 response. The system SHALL reject a payload where any detalle is missing `cursoId` or `aula` with a 400 response.

The system SHALL respond with a 404 when `estudianteId` does not match an existing `Estudiante`, or when any detalle's `cursoId` does not match an existing `Curso`.

#### Scenario: Successful enrollment with multiple courses
- **WHEN** a client posts a payload for an existing student with two detalle entries, each referencing an existing course and an aula
- **THEN** the system creates one `RegistrarMatricula` and two `DetalleMatricula` rows linked to it, and returns 201 with the created enrollment including both detalles

#### Scenario: Missing detalles list
- **WHEN** a client posts a payload with an empty detalles list
- **THEN** the system responds 400 and creates nothing

#### Scenario: Unknown student
- **WHEN** a client posts a payload whose `estudianteId` does not match any existing `Estudiante`
- **THEN** the system responds 404 and creates nothing

#### Scenario: Unknown course in a detalle
- **WHEN** a client posts a payload where one detalle's `cursoId` does not match any existing `Curso`
- **THEN** the system responds 404 and creates nothing

### Requirement: List RegistrarMatriculas
The system SHALL respond to `GET /v1/registrar-matriculas` with the full list of existing enrollments, each including its detalles.

#### Scenario: List existing enrollments
- **WHEN** a client requests `GET /v1/registrar-matriculas`
- **THEN** the system returns 200 with every existing `RegistrarMatricula` and its detalles

### Requirement: Retrieve a RegistrarMatricula by id
The system SHALL respond to `GET /v1/registrar-matriculas/{id}` with the matching enrollment and its detalles, or a 404 response when no enrollment with that id exists.

#### Scenario: Existing enrollment
- **WHEN** a client requests `GET /v1/registrar-matriculas/{id}` for an id that exists
- **THEN** the system returns 200 with that enrollment and its detalles

#### Scenario: Unknown enrollment
- **WHEN** a client requests `GET /v1/registrar-matriculas/{id}` for an id that does not exist
- **THEN** the system returns 404

### Requirement: Update a RegistrarMatricula
The system SHALL accept `PUT /v1/registrar-matriculas/{id}` with the same payload shape as create, replacing the matching enrollment's fields and its full set of detalles with the ones supplied, and returning the updated resource. Detalles omitted from the payload are removed; detalles present are (re)created to match the payload.

The system SHALL respond 404 when no enrollment with that id exists, or when `estudianteId` or any detalle's `cursoId` does not match an existing record. The system SHALL reject a payload missing `fechaInscripcion`, `estudianteId`, or an empty/missing detalles list with a 400 response.

#### Scenario: Replace detalles on update
- **WHEN** a client puts a payload for an existing enrollment that has one detalle in a new course and drops the previously enrolled course
- **THEN** the system updates the enrollment so it now has exactly the detalles supplied, and returns 200 with the updated resource

#### Scenario: Update unknown enrollment
- **WHEN** a client puts a payload for an id that does not exist
- **THEN** the system returns 404 and changes nothing

### Requirement: Delete a RegistrarMatricula
The system SHALL accept `DELETE /v1/registrar-matriculas/{id}` and remove the matching enrollment along with all of its detalles.

#### Scenario: Delete existing enrollment
- **WHEN** a client requests `DELETE /v1/registrar-matriculas/{id}` for an id that exists
- **THEN** the system removes that enrollment and its detalles, and returns 204

#### Scenario: Delete unknown enrollment
- **WHEN** a client requests `DELETE /v1/registrar-matriculas/{id}` for an id that does not exist
- **THEN** the system returns 404
