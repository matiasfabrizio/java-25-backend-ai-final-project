## Purpose

Exposes `Curso` resources over HTTP under `/v1/cursos`, letting clients create, list, retrieve, update, and delete courses through validated request/response payloads rather than the raw JPA entity.

## Requirements

### Requirement: Create Curso
The system SHALL accept `POST /v1/cursos` with a course payload (`nombre`, `siglas`, `estado`) and create a new `Curso`, returning the created resource including its generated id. The system SHALL reject a payload missing `nombre` or `siglas` with a 400 response.

#### Scenario: Valid course is created
- **WHEN** a client sends `POST /v1/cursos` with a valid `nombre`, `siglas`, and `estado`
- **THEN** the system creates the course and responds with the created resource, including its generated id

#### Scenario: Missing required field is rejected
- **WHEN** a client sends `POST /v1/cursos` without `nombre` or without `siglas`
- **THEN** the system responds 400 and does not create a course

### Requirement: List Cursos
The system SHALL respond to `GET /v1/cursos` with the full list of existing courses.

#### Scenario: List returns all courses
- **WHEN** a client sends `GET /v1/cursos`
- **THEN** the system responds with a list containing every existing course, including each course's id

### Requirement: Get Curso by id
The system SHALL respond to `GET /v1/cursos/{id}` with the matching course, or a 404 response when no course with that id exists.

#### Scenario: Existing course is returned
- **WHEN** a client sends `GET /v1/cursos/{id}` for a course that exists
- **THEN** the system responds with that course's data

#### Scenario: Unknown id returns 404
- **WHEN** a client sends `GET /v1/cursos/{id}` for an id with no matching course
- **THEN** the system responds 404

### Requirement: Update Curso
The system SHALL accept `PUT /v1/cursos/{id}` with a course payload and update the matching course's fields, returning the updated resource. The system SHALL respond 404 when no course with that id exists, and SHALL reject a payload missing `nombre` or `siglas` with a 400 response.

#### Scenario: Existing course is updated
- **WHEN** a client sends `PUT /v1/cursos/{id}` with a valid payload for a course that exists
- **THEN** the system updates the course and responds with the updated resource

#### Scenario: Update of unknown id returns 404
- **WHEN** a client sends `PUT /v1/cursos/{id}` for an id with no matching course
- **THEN** the system responds 404 and makes no change

### Requirement: Delete Curso
The system SHALL accept `DELETE /v1/cursos/{id}` and remove the matching course.

#### Scenario: Existing course is deleted
- **WHEN** a client sends `DELETE /v1/cursos/{id}` for a course that exists
- **THEN** the system deletes the course and it is no longer returned by `GET /v1/cursos/{id}` or `GET /v1/cursos`
