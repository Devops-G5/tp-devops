## Como ejecutar el proyecto

### Con Docker Compose

Requisitos: Docker y Docker Compose.

Para levantar el proyecto, desde la carpeta raíz:

```sh
$ BUILD_DATE=$(date -u +'%Y-%m-%dT%H:%M:%SZ') docker compose up --build  --no-attach mongodb       
```
Para bajar los contenedores y volumenes:

```sh
docker compose down -v
```

### De forma local
Requisitos: JDK 25 y Docker con Docker Compose(para levantar la base).


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