# Informe Técnico

- **Proyecto:** Devops-app
- **Integrantes:** Lucas Bonaffini,Nayla Zárate
- **Materia:** DevOps

---

## Índice

1. [Arquitectura del Pipeline](#1-arquitectura-del-pipeline)
2. [Justificación Técnica y Decisiones de Diseño](#2-justificación-técnica-y-decisiones-de-diseño)
3. [Aplicación de la Filosofía DevOps](#3-aplicación-de-la-filosofía-devops)
4. [Principios Lean (Reducción de Desperdicio)](#4-principios-lean-reducción-de-desperdicio)
5. [Conclusiones](#5-conclusiones)

---

## 1. Arquitectura del Pipeline

### 1.1 Diagrama de flujo

Ciclo de vida del código desde el `git push` hasta la publicación del artefacto o el despliegue.

```mermaid
flowchart LR
    A[Developer<br/>git push / PR] --> B[GitHub<br/>repositorio]
    B --> C[GitHub Actions<br/>runner ubuntu-latest]
    C --> D[Compilación Maven<br/>Java 25 · Temurin]
    D --> E[Linter<br/>Checkstyle · Google checks]
    E --> F[Tests<br/>JUnit · mvnw verify]
    F -->|solo rama Main o tags| G[Build de imagen Docker]
    G --> H[(Docker Hub<br/>tag = SHA del commit ó semVer del tag en github)]
    H --> I[Ejecución de la imagen<br/>Docker Compose]
    I --> J[Monitoreo<br/>Grafana Alloy + Grafana Cloud<br/>Prometheus + Loki]
    J --> K[Alertas<br/>notificación por correo]
    D -. falla .-> X[Pipeline cortado<br/>notificación al equipo]
    E -. falla .-> X
    F -. falla .-> X
    G -. falla .-> X
```

### 1.2 Componentes del flujo

| Etapa | Herramienta | Función |
|---|---|---|
| Control de versiones | GitHub | Almacena el código y dispara el pipeline (push ó PR a `main`/`develop`, ejecución manual y cuando se crea un tag) |
| Build | Maven (`mvnw`) + Java 25 (Temurin) | Compila el proyecto Spring Boot 4.1.1 |
| Linter | Checkstyle 14.1.0 (reglas `google_checks.xml`) | Verifica estilo; `failOnViolation=true` corta el build si hay violaciones |
| Tests | JUnit / starters de test de Spring Boot | Valida el comportamiento de la aplicación (`mvn verify`) |
| Runner de CI | GitHub Actions (`ubuntu-latest`) | Ejecuta las etapas del pipeline en un entorno efímero |
| Construcción de imagen | Docker + `docker/build-push-action` | Genera la imagen del contenedor |
| Registro de imágenes | Docker Hub | Almacena y versiona los artefactos (tag = SHA del commit ó semVer del tag en github) |
| Ejecución | Docker Compose | Levanta la imagen construida en Docker junto con sus servicios (MongoDB) y Grafana Alloy |
| Métricas | Spring Boot Actuator + Micrometer (Prometheus) | Expone métricas y salud de la API en `/actuator/prometheus` y `/actuator/health` |
| Recolección | Grafana Alloy | Recolecta las métricas y los logs JSON de la API y los envía a Grafana Cloud |
| Monitoreo | Grafana Cloud (Prometheus + Loki + Grafana) | Almacena métricas y logs, y los muestra en dashboards |
| Alertas | Grafana Alerting (contact point por correo electrónico) | Avisa al equipo cuando aumentan los errores 5xx o la latencia P95 |

### 1.3 El workflow 'ci-cd'

Se puede consultar el archivo yaml con todos los pasos [acá](https://github.com/Devops-G5/tp-devops/blob/develop/.github/workflows/ci-cd.yaml)

**Puntos clave del workflow:**

- `./mvnw verify` ejecuta en una sola orden la compilación, Checkstyle (fase `verify`) y los tests.
- `needs: build-and-test` garantiza que **no se publica ninguna imagen si falló el lint o un test**.
- `if: github.ref == 'refs/heads/main'` limita la publicación a la rama principal.
- Las credenciales de Docker Hub se guardan como **secrets** (`DOCKERHUB_USERNAME`, `DOCKERHUB_TOKEN`), nunca en el código.
- La imagen se etiqueta con el **SHA del commit**, lo que da trazabilidad exacta entre código y artefacto.
- Se usa caché de Maven (`cache: maven`) para acelerar las ejecuciones (feedback más rápido).

>Pipeline ejecutado (rama main)
![Pipeline ejecutado en CI](./img/pipeline-ci-cd-main.png)

vs.

>Pipeline ejecutado (rama develop)
![Pipeline ejecutado en CI](./img/pipeline-ci-cd-develop.png)

---


### 1.4 El workflow 'tagged'


Se puede consultar el archivo yaml con todos los pasos [acá](https://github.com/Devops-G5/tp-devops/blob/develop/.github/workflows/tagged.yaml)

Este workflow se encarga de **publicar versiones**: se dispara únicamente cuando se sube un tag con formato SemVer (por ejemplo `1.1.0`) y publica en Docker Hub una imagen con esa versión exacta.

```bash
git tag 1.1.0
git push origin 1.1.0
```

**Puntos clave del workflow:**

- `on.push.tags: '[0-9]+.[0-9]+.[0-9]+'` hace que el workflow solo se ejecute con tags en formato `X.Y.Z`. Cualquier otro tag (`hola`, `1.1`) ni siquiera lo dispara.
- Antes de publicar vuelve a ejecutar `./mvnw verify` (compilación, Checkstyle y tests), y `needs: build-and-test` garantiza que **no se publica ninguna versión si falló el lint o un test**.
- `docker/metadata-action` con `type=semver,pattern={{version}}` genera el tag de la imagen a partir del tag de Git: el tag `1.1.0` produce la imagen `usuario/imagen:1.1.0`.
- `flavor: latest=false` evita el tag `latest`, de modo que cada versión es un tag **fijo e inmutable**, útil para despliegues y rollbacks.
- El paso **Validar version** funciona como una segunda barrera (Andon Cord): si no se generó ningún tag de imagen o el nombre del tag no cumple `X.Y.Z`, el workflow falla con `exit 1` y los pasos siguientes (login, build y push) **no se ejecutan**.
- La versión se pasa al Dockerfile mediante `APP_VERSION`, que queda registrada en la etiqueta `org.opencontainers.image.version` de la imagen, lo que da trazabilidad entre el código, el tag y el artefacto.
- Las credenciales de Docker Hub se guardan como **secrets** (`DOCKERHUB_USERNAME`, `DOCKERHUB_TOKEN`), nunca en el código.
- `docker/setup-buildx-action` y `cache-from/cache-to: type=gha` reutilizan las capas de Docker entre ejecuciones, lo que acelera el build.

> Pipeline ejecutado (tag SemVer)

![Pipeline ejecutado al publicar un tag](./img/pipeline-ci-cd-tagged.png)

**Diferencia con `ci-cd`:**

| | `ci-cd` | `tagged` |
|---|---|---|
| Disparador | push y PR a `main` / `develop` | push de un tag `X.Y.Z` |
| Tag de la imagen | SHA del commit | Versión SemVer (`1.1.0`) |
| Uso | Integración continua y artefacto de cada cambio en `main` | Versiones liberadas e identificables |


> **Aclaración sobre alcance de la entrega:** el pipeline llega hasta la publicación del artefacto (imagen versionada en Docker Hub), lo que corresponde a **Entrega Continua** (*Continuous Delivery*). El despliegue automático a una plataforma de hosting (*Continuous Deployment*) es un desafío opcional que no implementamos en la entrega de este informe; el artefacto queda listo para desplegarse con un Deploy Hook como mejora futura.

---

## 2. Justificación Técnica y Decisiones de Diseño

### 2.1 Optimización de contenedores

#### Dockerfile utilizado

```dockerfile
ARG TAG_VERSION_BUILD=3.9-eclipse-temurin-25-alpine
ARG TAG_VERSION=25-jre-alpine

# STAGE 1 - BUILD
FROM maven:${TAG_VERSION_BUILD} AS build

WORKDIR /app

COPY pom.xml .


RUN mvn dependency:go-offline

COPY src ./src

RUN mvn clean package -DskipTests

FROM eclipse-temurin:${TAG_VERSION} AS prd

ARG APP_VERSION
ARG BUILD_DATE

LABEL org.opencontainers.image.created=$BUILD_DATE \
      org.opencontainers.image.version=$APP_VERSION \
      maintainer="Nayesz;Lucasb" \
      description="DevOps Project 2026 - G5"

WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup  # Se crea usuario no-root
USER appuser

COPY --from=build --chown=appuser:appuser /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
```

#### Justificación de decisiones

| Decisión | Elección | Justificación |
|---|---|---|
| **Imagen base** | Build: `maven:3.9-eclipse-temurin-25-alpine`. Runtime: `eclipse-temurin:25-jre-alpine` | Basadas en **Alpine**: mucho más livianas que las imágenes Debian/Ubuntu, con menor superficie de ataque. En runtime se usa solo el **JRE** (sin JDK ni Maven), ya que para ejecutar el `.jar` no hace falta compilar. Temurin es una distribución OpenJDK abierta y mantenida. |
| **Versiones parametrizadas** | `ARG TAG_VERSION_BUILD` y `ARG TAG_VERSION` | Los tags de las imágenes base se definen en un solo lugar al inicio, lo que facilita actualizar la versión de Java sin tocar el resto del archivo. |
| **Estrategia de capas** | `COPY pom.xml` → `mvn dependency:go-offline` → `COPY src` → `mvn package` | Se ordena de lo que menos cambia a lo que más cambia. Si solo se modifica el código, Docker reutiliza la capa de dependencias (la más lenta) y no las vuelve a descargar. Acelera el build. |
| **Multi-stage build** | Etapa `build` (JDK + Maven) y etapa `prd` (JRE) | La imagen final contiene solo el JRE y el `.jar` (`COPY --from=build`); no incluye Maven, el código fuente ni el caché de dependencias. Resultado: imagen más chica y más segura. |
| **Usuario no-root** | `addgroup -S appgroup && adduser -S appuser -G appgroup` + `USER appuser` | Usuario y grupo de sistema sin privilegios para ejecutar la aplicación. Si el contenedor es vulnerado, el atacante no tiene privilegios de root (principio de mínimo privilegio). El `.jar` se copia con `--chown=appuser:appuser`. |
| **`-DskipTests` en el build** | `mvn clean package -DskipTests` | Los tests ya se ejecutaron en el job de CI (`mvnw verify`) antes de construir la imagen; repetirlos en el Dockerfile solo alargaría el build. |
| **Metadatos OCI** | `LABEL org.opencontainers.image.created / version`, `maintainer`, `description` | Trazabilidad: la imagen publicada indica cuándo y desde qué versión se construyó (valores recibidos por `build-args` desde el workflow). |

#### Comparación de tamaño de imagen

| Versión del Dockerfile | Tamaño de la imagen |
|---|---|
| Sin optimizar (una sola etapa, imagen `maven` con JDK, sin Alpine) |  287MB|
| Optimizada (Alpine + JRE + multi-stage) | 111MB |

Para tener el dato real: construimos ambas versiones localmente `api:optimizada` y una variante de una sola etapa `api:simple` con el siguiente Dockerfile:

```dockerfile
ARG TAG_VERSION_BUILD=3.9-eclipse-temurin-25-alpine

FROM maven:${TAG_VERSION_BUILD}

ARG APP_VERSION
ARG BUILD_DATE

LABEL org.opencontainers.image.created=$BUILD_DATE \
      org.opencontainers.image.version=$APP_VERSION \
      maintainer="Nayesz;Lucasb" \
      description="DevOps Project 2026 - G5"

WORKDIR /app

# Se crea un usuario no-root
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Primero copiamos el POM para aprovechar la caché de dependencias
COPY pom.xml .

RUN mvn dependency:go-offline

# Copiamos el código fuente
COPY src ./src

# Compilamos y generamos el JAR
RUN mvn clean package -DskipTests

# Ejecutamos la aplicación como usuario no-root
USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/target/devops-0.0.1-SNAPSHOT.jar"]

```

Esta es la comparacion de las imagenes donde podemos ver la diferencia del peso en la imagen final:
> ![Comparación de tamaños de imagen](./img/docker-images.png)

### 2.2 Estrategia de integración

#### Modelo de branching elegido: GitFlow (`main` + `develop` + ramas de trabajo)

**Reglas adoptadas:**

- `main`: rama estable; desde ella se publica una imagen en Docker Hub tagueada con el hash del commit.
- `develop`: rama de integración donde se juntan las funcionalidades.
- Ramas de trabajo: `feature/*`, `fix/*` de vida corta, que parten de `develop`.
- Todo cambio entra a `develop` o `main` mediante Pull Request.
- Convención de commits: Conventional Commits.
- Estrategia de merge:  merge commit, mantenemos todos los commits del PR y se agrega un commit de merge.

**Justificación:** Elegimos GitFlow en primer lugar porque es lo conocido. Además, creemos que GitFlow tiene más armonia con el código en sus distintas etapas,
'lo estable' -> main, 'lo que se integro de distintas ramas de trabajo y esta estable pero todavía no fue a prd' -> develop , multiples ramas según de que se trate feature/, fix/,
etc. Además hace más fácil definir las reglas para el workflow de github actions, ya que podes tener distintas acciones para c/u.

#### Protección de ramas (`main`)

Configuración aplicada en **Settings → Rules → Rulesets**:

- Requerir Pull Request antes de hacer merge:
![Ejemplo](./img/requires_pr.png)
- Requerir al menos 1 aprobación de revisión:
![Ejemplo](./img/requires_revision.png)
- Requerir que los checks de CI pasen (lint, tests, build) antes del merge:
![Ejemplo](./img/test_fail.png)
- Bloquear push directo y force-push a `main`


Configuración de protección de ramas sobre main y develop:

![Reglas de protección de rama](img/branch-protection.png)

---

## 3. Aplicación de la Filosofía DevOps

### 3.1 Primera Forma: Flujo y Consistencia


#### ¿Qué tareas manuales quedaron 100% automatizadas?

| Tarea manual (antes) | Automatización (ahora) |
|---|---|
| Ejecutar el linter localmente "cuando uno se acuerda" | El linter corre en cada push/PR |
| Correr los tests a mano antes de entregar | Los tests se ejecutan automáticamente en el runner |
| Construir la imagen Docker en la máquina del desarrollador | El build ocurre en CI con un entorno limpio |
| Etiquetar y subir la imagen al registro manualmente | El pipeline la etiqueta (`sha` / versión) y la publica |

#### ¿Cómo se asegura que el entorno sea idéntico en desarrollo y CI/CD?

- **Mismo Dockerfile y versiones fijadas:** la imagen se construye siempre con el mismo Dockerfile, con tags de imagen base explícitos (`maven:3.9-eclipse-temurin-25-alpine` y `eclipse-temurin:25-jre-alpine`).
- **Java alineado en todos los entornos:** Java 25 (Temurin) en el `pom.xml` (`java.version`), en el runner de CI (`setup-java`) y en la imagen base; Spring Boot 4.1.1 y plugins con versión explícita.
- **Docker Compose** para desarrollo local: la app usa `spring-boot-docker-compose`, que levanta los servicios (MongoDB) definidos en `compose.yaml` al iniciar.
- **Configuración por variables de entorno** (12-Factor App): el código no cambia entre entornos, solo la configuración.
- **Runner efímero:** cada ejecución de CI parte de un entorno limpio, sin estado residual.

### 3.2 Segunda Forma: Feedback Rápido y Andon Cord

#### Interrupción del pipeline: puntos donde se "corta el cable"

| Etapa | Condición de falla | Efecto |
|---|---|---|
| **Compilación (Maven)** | Error de compilación | `mvn verify` falla; el job termina en rojo |
| **Linter (Checkstyle)** | Cualquier violación de las reglas de Google (`failOnViolation=true`) | Falla el job `build-and-test`; no se publica imagen |
| **Tests (JUnit)** | Al menos un test falla | Falla el job `build-and-test` y se bloquea el merge del PR |
| **Dependencia entre jobs** | `needs: build-and-test` no se cumplió | El job de Docker Hub **no se ejecuta** |
| **Build de imagen Docker** | Error de construcción | No se publica el artefacto |
| **Protección de rama** | Checks requeridos en rojo | GitHub impide el merge a `main` |

**Principio:** un artefacto defectuoso nunca llega al registro ni a producción. El equipo recibe la notificación por email y la prioridad es **arreglar el pipeline antes de seguir con código nuevo**.

![Pipeline detenido por fallo](./img/test_fail.png)

#### Visibilidad de fallos: errores HTTP 5xx y latencia alta

La observabilidad se resuelve con **Grafana Cloud**, alimentado por **Grafana Alloy**, un agente que corre junto a la API:

- **Métricas:** Alloy consulta `/actuator/prometheus` cada 15 segundos y las envía a Prometheus (Grafana Cloud).
- **Logs:** Alloy lee los logs JSON del contenedor y los envía a Loki, etiquetados por nivel (`level`).

![Dashboard de monitoreo](img/dashboard-grafana.png)


| Aspecto | Detalle |
|---|---|
| **Herramienta de monitoreo** | Grafana Cloud (Prometheus + Loki), con Grafana Alloy como recolector |
| **Señal para 5xx** | **Métrica:** panel de errores 5xx (`http_server_requests_seconds_count{status=~"5.."}`). **Log:** entradas con `level=ERROR` en Loki. |
| **Señal para latencia** | **Métrica:** paneles P50 y P95 de `http_server_requests_seconds`. |
| **Disponibilidad** | Endpoint de salud `/actuator/health`. |
| **Canal de notificación** | Correo electrónico, mediante un *contact point* de Grafana Alerting |


**Reglas de alerta** (se evalúan cada 1 minuto):

| Alerta | Consulta (PromQL) | Condición |
|---|---|---|
| **Errores 5xx** | `sum(increase(http_server_requests_seconds_count{job="devops-api", uri=~"/api/.*", status=~"5.."}[5m]))` | Más de 5 errores en 5 minutos |
| **Latencia P95** | `histogram_quantile(0.95, sum by (le) (rate(http_server_requests_seconds_bucket{job="devops-api", uri=~"/api/.*"}[5m])))` | P95 mayor a 1 seg |

Reglas en Grafana:

![Reglas de alerta en Grafana](img/alerta-regla.png)


Además de las métricas, los logs de la API llegan a Loki en formato JSON, con el nivel (`level`) como etiqueta. Esto permite filtrar los `ERROR` y los `WARN` y ubicar la causa de un fallo:

![Logs de la API en Grafana (Loki)](img/logs.png)



### 3.3 Tercera Forma: Aprendizaje y Simulación de Fallos

#### Experimento de falla controlada: errores 5xx

**Escenario:** se generaron errores HTTP 5xx en la API (Se bajó el servicio 'mongodb') hasta superar el umbral de la regla `API - Errores 5xx`.

**Hipótesis:** al superar los 5 errores en una ventana de 5 minutos, Grafana disparará la alerta y enviará un correo al equipo, sin intervención manual.

**Resultado observado:**

El panel *Errores HTTP 5XX* mostró el aumento y la regla pasó a estado *Firing*. Llegó un correo de alerta con ~15,7 errores medidos, frente a un umbral de 5.

![Panel de errores 5xx](img/experimento-5xx.png)

![Correo de alerta recibido](img/experimento-mail-firing.png)

Una vez que se estabilizó esa alerta, Grafana la marcó como resuelta y también notificó:

![Correo de alerta recibido](img/experimento-mail-resolved.png)


#### Experimento de falla controlada: Fallan los tests

**Escenario:** 

- Se modificó un test unitario para que fallara intencionalmente.

**Hipótesis:** el pipeline debería detener el flujo en la etapa de tests y bloquear el merge.

**Procedimiento:**

1. Se creó la rama `feature/test-fail`.
2. Se rompió uno de los test a propósito
![Cambios en test](img/cambios-test.png)

3. Se hizo push y se abrió un Pull Request hacia `develop`.
![Cambios en test](img/test_fail.png)

4. Se envió la notificación a nuestros mails

![Cambios en test](img/notificacion-mail.png)

**Resultado observado:**

El PR quedó bloqueado; no sé incorporó el código a develop.

---

## 4. Principios Lean (Reducción de Desperdicio)

| # | Desperdicio común | Cómo se manifiesta | Cómo lo evita la solución implementada |
|---|---|---|---|
| 1 | **Esperas** (compilación y empaquetado) | El equipo tiene que esperar a que alguien construya y suba la versión | El pipeline construye, prueba y publica automáticamente en cada cambio |
| 2 | **Defectos detectados tarde** (bugs en producción) | Un error llega a los usuarios y su corrección es costosa | Linter y tests en CI detectan el error en minutos; la protección de ramas impide integrar código roto |
| 3 | **Reprocesos y configuración manual** ("en mi máquina funciona") | Se pierde tiempo replicando entornos y corrigiendo diferencias | Contenedor idéntico en todos los entornos; infraestructura y configuración como código |

---

## 5. Conclusiones

Como conclusión final, queremos remarcar que esta práctica nos permitió aplicar de punta a punta el ciclo de vida de una aplicación, y no solo programarla, tal vez interviniendo en lugares donde en nuestro día a día no tenemos la oportunidad de hacerlo.

**Dificultades y cómo las resolvimos**

- Configurar las métricas y el monitoreo fue un gran desafio porque ninguno de nosotros contaba con experiencia en la configuración.

**Alcance y mejoras futuras**

- El pipeline llega hasta la publicación del artefacto (Entrega Continua). Como mejora, se puede automatizar el despliegue mediante un Deploy Hook.
- Agregar un análisis de vulnerabilidades de la imagen.
