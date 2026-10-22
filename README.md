# DevOps — API de tareas y usuarios

API REST con Java 25, Spring Boot y MongoDB. Flujo de dependencias:
`Controller → Service → UseCase → ITaskRepository → TaskRepository → ITaskDataSource → TaskDataSource → MongoDB`.
El dominio no depende de MongoDB; `TaskDocument` representa la colección `tasks`.

## Indice
1. [Como probar el proyecto de forma local](./docs/instructivo.md)
2. [Descripción de endpoints](./docs/instructivo.md)
3. [Informe Técnico](./docs/informe_tecnico.md)