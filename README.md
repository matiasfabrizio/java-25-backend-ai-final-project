# Academia

API REST para gestionar una academia: cursos, estudiantes y sus matrículas. Proyecto final del curso **Java 25, Spring Boot y AI** de MitoCode.

## ¿Qué hace la aplicación?

Permite administrar tres recursos principales, cada uno con CRUD completo (crear, listar, obtener por id, actualizar y eliminar):

| Recurso | Ruta base | Descripción |
|---|---|---|
| Curso | `/v1/cursos` | Cursos ofrecidos (`nombre`, `siglas`, `estado`). |
| Estudiante | `/v1/estudiantes` | Estudiantes (`nombres`, `apellidos`, `dni`, `edad`). |
| Matrícula | `/v1/registrar-matriculas` | Inscripción de un estudiante en uno o más cursos. |

Una **matrícula** (`RegistrarMatricula`) pertenece a un estudiante y contiene una lista de **detalles** (`DetalleMatricula`), cada uno con el curso y el aula. La fecha de inscripción la asigna el servidor al crear la matrícula y no cambia al actualizarla.

Además del CRUD, la API expone dos consultas construidas con **programación funcional** (Streams de Java):

- `GET /v1/estudiantes/edad-desc`: estudiantes ordenados por edad.
- `GET /v1/registrar-matriculas/get-relation`: relación de cursos y estudiantes matriculados.

## Requerimientos

### 1. Listado de estudiantes ordenado por edad

- **Endpoint:** `GET /v1/estudiantes/edad-desc`
- Devuelve **todos** los estudiantes registrados, sin filtrar ninguno.
- El orden es **descendente por edad**: primero el estudiante de mayor edad.
- El ordenamiento se hace en la capa de servicio con Streams, no con una consulta a la base de datos:

```java
return estudianteRepo.findAll()
        .stream()
        .sorted(Comparator.comparing(Estudiante::getEdad).reversed())
        .toList();
```

Ejemplo de respuesta:

```json
[
  { "id": 2, 
    "nombres": "Ana", 
    "apellidos": "Torres", 
    "dni": "87654321", 
    "edad": 30
  },
  { "id": 1, 
    "nombres": "Luis",
    "apellidos": "Pérez", 
    "dni": "12345678", 
    "edad": 21
  }
]
```

### 2. Relación de cursos y estudiantes matriculados

- **Endpoint:** `GET /v1/registrar-matriculas/get-relation`
- Devuelve un mapa donde la **clave** es el nombre del curso y el **valor** es la lista de estudiantes matriculados en él (nombres y apellidos).
- Se recorren todas las matrículas y sus detalles. Cada detalle genera un par (curso, estudiante), y luego los pares se agrupan por curso:

```java
return registrarMatriculaRepo.findAll()
        .stream()
        .flatMap(matricula -> {
            String alumno = matricula.getEstudiante().getNombres() + " " + matricula.getEstudiante().getApellidos();
            return matricula.getDetalles().stream()
                    .map(detalle -> new CursoEstudiante(detalle.getCurso().getNombre(), alumno));
        })
        .collect(Collectors.groupingBy(
                CursoEstudiante::curso,
                Collectors.mapping(CursoEstudiante::estudiante, Collectors.toList())
        ));
```

Ejemplo de respuesta:

```json
{
  "Java": [
    "Luis Pérez", 
    "Ana Torres"
  ],
  "Spring Boot": [
    "Ana Torres"
  ]
}
```

## Tecnologías

- Java 25
- Spring Boot 4.1.1 (Web MVC, Data JPA, Validation)
- PostgreSQL 17 (vía Docker Compose)
- MapStruct (conversión entre DTOs y entidades)
- Lombok
- Maven (wrapper incluido)

## Arquitectura

Organizada en capas bajo `com.mfpr.academia`:

- `controller`: endpoints REST. Reciben y devuelven DTOs.
- `dto`: records de request/response con validaciones (`@NotBlank`, `@NotNull`, `@Valid`) y mappers de MapStruct.
- `service`: lógica de negocio sobre una base CRUD genérica (`ICRUD` / `CRUDImpl`).
- `repository`: repositorios de Spring Data JPA.
- `model`: entidades JPA.
- `exception`: manejo global de errores. Devuelve `404` si un recurso no existe, `400` si la validación falla y `500` en cualquier otro caso.

## Cómo ejecutar

**Requisito:** tener Docker en ejecución.

```bash
./mvnw spring-boot:run      # Linux / macOS
.\mvnw.cmd spring-boot:run  # Windows
```

Al iniciar, Spring Boot levanta automáticamente el contenedor de PostgreSQL definido en `compose.yaml` (puerto `5433`) y crea las tablas a partir de las entidades. La API queda disponible en `http://localhost:8080`.

Ejemplo para crear una matrícula:

```bash
curl -X POST http://localhost:8080/v1/registrar-matriculas \
  -H "Content-Type: application/json" \
  -d '{"estudianteId": 1, "estado": true, "detalles": [{"cursoId": 1, "aula": "A-101"}]}'
```
