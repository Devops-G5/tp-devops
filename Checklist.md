# Checklist Técnico — Trabajo Práctico Integrador

> Ciclo de Vida y Despliegue Continuo de una API

---

## Fase 1: Desarrollo Base y Documentación

- [x] API REST desarrollada (lenguaje/framework a elección)
- [x] Suite de pruebas unitarias básicas implementada
- [ ] Documentación interactiva de la API expuesta y accesible (Swagger / OpenAPI o equivalente nativo del framework) — **requisito obligatorio de validación**

---

## Fase 2: Gestión de Cambios y Versionado

- [x] Historial de commits siguiendo **Conventional Commits** (`feat:`, `fix:`, `docs:`, `ci:`, etc.)
- [x] Rama principal (`main`/`master`) protegida — sin pushes directos
- [] Todos los cambios integrados vía **Pull Requests**, documentados con:
  - [ ] Descripción del cambio
  - [ ] Evidencia de pruebas ejecutadas
- [ ] Estrategia de versionado/etiquetado definida (elegir una):
  - [ ] Semantic Versioning (SemVer) — `MAJOR.MINOR.PATCH` (ej. `v1.0.0`)
  - [ ] Commit SHA / Git Hash (ej. `sha-a1b2c3d`)
  - [ ] Timestamp / Build Number (ej. `2026.08.29-build.42`)

---

## Fase 3: Empaquetado y Entorno de Desarrollo (Docker)

- [x] Dockerfile con **multi-stage build** (compilación separada de ejecución)
- [x] Imagen base ligera y específica (`alpine`, `slim`, `distroless`) — **prohibido el tag `latest`**
- [x] Usuario **non-root** configurado para ejecutar la app
- [x] Capas del Dockerfile ordenadas para maximizar caché (dependencias separadas del código fuente)
- [x] `docker-compose.yml` funcional — levanta el entorno completo con un solo comando

---

## Fase 4: Automatización CI/CD (GitHub Actions)

**Integración Continua (CI)**
- [x] Workflow modular en GitHub Actions activado en eventos de Pull Request
- [x] Etapa de linter automatizada
- [x] Etapa de tests unitarios automatizada (Andon Cord: si falla, el PR no se integra)

**Publicación de Artefactos**
- [ ] Build de imagen Docker automatizado en el pipeline
- [ ] Publicación automática a Docker Hub
- [ ] Imagen etiquetada automáticamente con la versión SemVer de la release

**Despliegue Continuo (CD) — opcional/adicional**
- [ ] Despliegue automático configurado (ej. Render u otra plataforma gratuita)
- [ ] Deploy lanzado mediante Deploy Hook
- [ ] Deploy Hook recibe como parámetro la versión/tag exacta de la imagen generada en CI

---

## Fase 5: Observabilidad y Monitoreo

- [ ] Logs estructurados en formato JSON, incluyendo al menos:
  - [ ] `timestamp`
  - [ ] `level`
  - [ ] `path`
  - [ ] `status_code`
- [ ] Aplicación conectada a plataforma de telemetría (Grafana Cloud, Datadog, New Relic o Sentry)
- [ ] Dashboard diseñado desde cero (sin plantillas preconfiguradas)
- [ ] Dashboard refleja las **Golden Signals** (con tráfico real generado sobre la API):
  - [ ] Tráfico — peticiones por segundo / RPM
  - [ ] Latencia — p50, p95
  - [ ] Errores — tasa/conteo de HTTP 4xx y 5xx

---

## Referencia rápida — Ponderación de evaluación

| Componente | Peso |
|---|---|
| Documentación Abierta de la API | Obligatorio (validación) |
| Gestión de Cambios | 20% |
| Empaquetado y Docker | 25% |
| Automatización CI/CD | 30% |
| Observabilidad | 25% |
