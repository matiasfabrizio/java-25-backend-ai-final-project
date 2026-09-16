## Purpose

Exposes `Estudiante` resources over HTTP under `/v1/estudiantes`, letting clients create, list, retrieve, update, and delete students through validated request/response payloads rather than the raw JPA entity.

## ADDED Requirements

### Requirement: Create Estudiante
The system SHALL accept `POST /v1/estudiantes` with a student payload (`nombres`, `apellidos`, `dni`, `edad`) and create a new `Estudiante`, returning the created resource including its generated id. The system SHALL reject a payload missing `nombres`, `apellidos`, or `dni` with a 400 response.

#### Scenario: Valid student is created
- **WHEN** a client sends `POST /v1/estudiantes` with valid `nombres`, `apellidos`, `dni`, and `edad`
- **THEN** the system creates the student and responds with the created resource, including its generated id

#### Scenario: Missing required field is rejected
- **WHEN** a client sends `POST /v1/estudiantes` without `nombres`, `apellidos`, or `dni`
- **THEN** the system responds 400 and does not create a student

### Requirement: List Estudiantes
The system SHALL respond to `GET /v1/estudiantes` with the full list of existing students.

#### Scenario: List returns all students
- **WHEN** a client sends `GET /v1/estudiantes`
- **THEN** the system responds with a list containing every existing student, including each student's id

### Requirement: Get Estudiante by id
The system SHALL respond to `GET /v1/estudiantes/{id}` with the matching student, or a 404 response when no student with that id exists.

#### Scenario: Existing student is returned
- **WHEN** a client sends `GET /v1/estudiantes/{id}` for a student that exists
- **THEN** the system responds with that student's data

#### Scenario: Unknown id returns 404
- **WHEN** a client sends `GET /v1/estudiantes/{id}` for an id with no matching student
- **THEN** the system responds 404

### Requirement: Update Estudiante
The system SHALL accept `PUT /v1/estudiantes/{id}` with a student payload and update the matching student's fields, returning the updated resource. The system SHALL respond 404 when no student with that id exists, and SHALL reject a payload missing `nombres`, `apellidos`, or `dni` with a 400 response.

#### Scenario: Existing student is updated
- **WHEN** a client sends `PUT /v1/estudiantes/{id}` with a valid payload for a student that exists
- **THEN** the system updates the student and responds with the updated resource

#### Scenario: Update of unknown id returns 404
- **WHEN** a client sends `PUT /v1/estudiantes/{id}` for an id with no matching student
- **THEN** the system responds 404 and makes no change

### Requirement: Delete Estudiante
The system SHALL accept `DELETE /v1/estudiantes/{id}` and remove the matching student, responding 404 when no student with that id exists.

#### Scenario: Existing student is deleted
- **WHEN** a client sends `DELETE /v1/estudiantes/{id}` for a student that exists
- **THEN** the system deletes the student and it is no longer returned by `GET /v1/estudiantes/{id}` or `GET /v1/estudiantes`

#### Scenario: Delete of unknown id returns 404
- **WHEN** a client sends `DELETE /v1/estudiantes/{id}` for an id with no matching student
- **THEN** the system responds 404
