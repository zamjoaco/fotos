# Estudio de Fotografía — Constitution

## Core Principles

### I. Spec-Driven (obligatorio)
Cada feature/Epic se desarrolla con spec-kit, en orden y sin saltear pasos:
`/specify` → `/plan` → `/tasks` → `/implement`. Todo queda persistido en
`/specs/<feature>/` (spec.md, plan.md, tasks.md). No se escribe código de
una feature sin que exista su spec, su plan y sus tasks.

### II. Scrum de una persona
El tablero es **GitHub Projects (Kanban)** + **Issues** + **Milestones**:
un Milestone por Epic (`Epic N - Nombre`), Issues redactados como historias
de usuario (`Como <rol> quiero <acción> para <beneficio>`) con label
`epic:N`. El plan de Epic 0 preveía cargar 1-2 Issues placeholder por
Epic; a la fecha de esta versión **todavía no se crearon** (0 Issues, 0
Milestones en el repo) — queda como deuda de Epic 0. Las historias finas
se redactan al hacer `/specify` de cada feature.

### III. Backend por feature, entidades ricas
El backend se organiza en paquetes **por feature** (`sections`, `works`,
`leads`, `campaigns`, `auth`). Las entidades de dominio son ricas (POO
real, la lógica vive en el dominio), nunca anémicas ni transaction-script.

### IV. Sin checkout ni pago
El sitio **no vende online**: el contacto real de venta es un botón a
WhatsApp (`wa.me`). Cualquier feature que asuma carrito/pago/checkout está
fuera de alcance salvo decisión explícita documentada.

### V. Testing real desde que haya lógica
Backend con **JUnit 5 + Mockito** (unitarios) y **Testcontainers**
(integración contra PostgreSQL real) — el smoke test de Epic 0 ya arranca
el contexto contra un `PostgreSQLContainer` real. A partir del Epic 1,
cada PR corre `mvn verify` completo en CI (hoy la CI sigue en
`-DskipTests`, ver `.github/workflows/ci.yml`). El frontend define su
estrategia de testing al llegar a los Epics que agreguen lógica de UI
relevante.

## Restricciones

Stack fijo: Spring Boot (Java 21, Maven, Spring Security, Spring Data JPA,
Flyway), Angular + Tailwind, PostgreSQL, Redis, MinIO, Mailhog (dev),
springdoc-openapi. Las variables de entorno van por `.env` (nunca se
commitea `.env`). Cada Epic/feature agrega sus tablas con una migración
Flyway propia (en Epic 0 no hay tablas).

## Workflow

Cada feature pasa por el ciclo spec-kit completo antes de entregar código.
Gate de calidad backend: `mvn verify` verde (unitarios + integración).
Scaffold e infraestructura (Epic 0): Docker, CI de solo build, Project
board. La infraestructura ya resuelta (docker-compose, Dockerfiles,
SecurityConfig) se trata con cuidado: no se rompe por ediciones aisladas.

## Governance

Esta constitution prevalece sobre prácticas ad-hoc. Las enmiendas requieren
documentación y actualización de este archivo. Cualquier feature debe poder
rastrearse a su spec en `/specs/<feature>/` y a su Issue/Milestone en
GitHub.

**Version**: 1.0.0 | **Ratified**: 2026-09-21 | **Last Amended**: 2026-09-21
