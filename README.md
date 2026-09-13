# DevOps — API de tareas y usuarios

API REST con Java 25, Spring Boot y MongoDB. Flujo de dependencias:
`Controller → Service → UseCase → ITaskRepository → TaskRepository → ITaskDataSource → TaskDataSource → MongoDB`.
El dominio no depende de MongoDB; `TaskDocument` representa la colección `tasks`.

## Ejecutar

Requisitos: JDK 25 y Docker con Docker Compose.

```sh
./mvnw spring-boot:run
```

Spring Boot inicia el MongoDB del `compose.yaml` existente y descubre su puerto
asignado automáticamente. La API escucha en `http://localhost:8080`.

Para usar un MongoDB externo, desactivar la integración con Compose y configurar la URI:

```sh
DOCKER_COMPOSE_ENABLED=false \
MONGODB_URI='mongodb://localhost:27017/devops' \
./mvnw spring-boot:run
```

## Endpoints

| Método | Ruta | Resultado |
| --- | --- | --- |
| POST | `/api/tasks` | Crea una tarea; 201 con cuerpo y cabecera Location |
| GET | `/api/tasks` | Lista todas las tareas; 200 (array vacío si no hay tareas) |
| GET | `/api/tasks/{id}` | Obtiene una tarea; 200 o 404 |
| PUT | `/api/tasks/{id}` | Reemplaza los campos editables; 200 o 404 |
| DELETE | `/api/tasks/{id}` | Elimina una tarea; 204 o 404 |

Crear una tarea:

```sh
curl -i -X POST http://localhost:8080/api/tasks \
  -H 'Content-Type: application/json' \
  -d '{"name":"Configurar CI","description":"Pipeline de pruebas","status":"pending","type":"devops","owner":"Lucas"}'
```

`name` es obligatorio y no puede estar vacío. Los otros campos son opcionales;
`status` y `type` son texto libre, sin valores predefinidos. La API genera `id`,
`created` y `updated`; no forman parte del cuerpo de escritura. PUT conserva `id`,
`created` y `assignee`, renueva `updated` y deja en null los campos editables opcionales omitidos.
JSON inválido o un nombre inválido producen 400. Un ID inexistente produce 404.

Usar el `id` recibido al crear:

```sh
curl http://localhost:8080/api/tasks
curl http://localhost:8080/api/tasks/ID
curl -X PUT http://localhost:8080/api/tasks/ID \
  -H 'Content-Type: application/json' \
  -d '{"name":"Configurar CI","status":"done"}'
curl -i -X DELETE http://localhost:8080/api/tasks/ID
```

## Usuarios

La feature `users` sigue las mismas capas que `tasks` y persiste en la colección
`users`, con un documento Mongo separado del dominio. Las dependencias se inyectan
por constructor mediante `@RequiredArgsConstructor`.

| Método | Ruta | Resultado |
| --- | --- | --- |
| POST | `/api/users` | Crea un usuario; 201 con cuerpo y Location |
| GET | `/api/users` | Lista usuarios; 200 (array vacío si no hay usuarios) |
| GET | `/api/users/{id}` | Obtiene un usuario; 200 o 404 |
| PUT | `/api/users/{id}` | Reemplaza sus campos editables; 200 o 404 |
| DELETE | `/api/users/{id}` | Elimina un usuario; 204 o 404 |

`name`, `lastname`, `username`, `email` y `rol` son obligatorios y no pueden estar
vacíos. `email` debe tener formato válido. `rol` es texto libre: por ejemplo,
`developer` o `designer`. El ID lo genera MongoDB y se conserva al actualizar.
PUT requiere todos los campos. Datos inválidos producen 400; IDs inexistentes, 404.
Este CRUD no impone unicidad de username/email ni incluye autenticación.

```sh
curl -i -X POST http://localhost:8080/api/users \
  -H 'Content-Type: application/json' \
  -d '{"name":"Lucas","lastname":"Bonaffini","username":"lucas","email":"lucas@example.com","rol":"developer"}'

curl http://localhost:8080/api/users
curl http://localhost:8080/api/users/ID
curl -X PUT http://localhost:8080/api/users/ID \
  -H 'Content-Type: application/json' \
  -d '{"name":"Lucas","lastname":"Bonaffini","username":"lucas","email":"lucas@example.com","rol":"designer"}'
curl -i -X DELETE http://localhost:8080/api/users/ID
```

## Asignar tareas

```sh
curl -X PATCH http://localhost:8080/api/tasks/TASK_ID/assignee \
  -H 'Content-Type: application/json' \
  -d '{"userId":"USER_ID"}'
```

`AssignTask` recibe los IDs, consulta la tarea y el usuario registrados y guarda el
ID del usuario en `assignee`. Devuelve la tarea actualizada (200), 404 si no existe
la tarea o el usuario, 409 si la tarea ya tiene una asignación, y 400 si falta
`userId` o está vacío.

La actualización MongoDB exige que `assignee` siga en null (o ausente) en la misma
operación que guarda `assignee` y `updated`. Dos asignaciones concurrentes no pueden
sobrescribirse. Si la tarea desaparece durante la operación, se devuelve 404.

Las tareas se crean sin asignación. `assignee` no es un campo editable en POST/PUT;
PUT conserva la asignación existente. La asignación se realiza con este endpoint.
No se implementa reasignación ni desasignación.

## Pruebas

```sh
./mvnw test
```

Las pruebas cubren los endpoints a través de las capas hasta un datasource simulado,
validación, errores y las opciones de actualización de MongoDB. También cubren
asignación, conflictos y eliminación concurrente simulados, y el filtro condicional
de asignación de MongoDB. No requieren Docker
ni un servidor MongoDB. No sustituyen una prueba de persistencia con MongoDB real.
