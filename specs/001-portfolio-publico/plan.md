# Implementation Plan: Portfolio publico

**Branch**: `001-portfolio-publico` | **Date**: 2026-09-21 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/001-portfolio-publico/spec.md`

## Summary

Mostrar el portfolio publico del estudio (home + secciones con galeria responsive de fotos), sirviendo el contenido desde `Section`/`Work` persistidos en PostgreSQL y las imagenes desde MinIO, sin depender del panel de administracion (Epic 4) — el contenido de demo se carga via seed de Flyway. Backend expone endpoints REST de solo lectura (`sections`, `works`) consumidos por el frontend Angular ya scaffoldeado en Epic 0 (`public/home`, `public/sections`).

## Technical Context

**Language/Version**: Java 21 (backend) / TypeScript ~6.0 con Angular 22 (frontend)

**Primary Dependencies**: Spring Boot 3.5.3 (web, data-jpa, validation, actuator), Flyway, springdoc-openapi (ya en `backend/pom.xml`); Angular 22 + Tailwind CSS 4 (ya en `frontend/package.json`)

**Storage**: PostgreSQL 16 (metadata de `Section`/`Work`), MinIO (binarios de las fotos, ya provisionado en Epic 0)

**Testing**: JUnit 5 + Mockito (unitarios backend) y Testcontainers contra Postgres real (integracion backend, patron ya establecido en Epic 0 con `@ServiceConnection`); `@angular/build:unit-test` (Vitest) para el frontend

**Target Platform**: Web — backend como contenedor Linux (Docker, Epic 0), frontend servido por `ng serve`/build estatico

**Project Type**: Web application (backend + frontend), estructura ya definida en Epic 0

**Performance Goals**: Primeras fotos de una seccion visibles en pantalla en <2s en banda ancha estandar (SC-002)

**Constraints**: Layout responsive sin scroll horizontal entre 360px y 1920px (SC-003); imagenes servidas desde almacenamiento de objetos, nunca embebidas en el backend (FR-008); galeria usable con volumen grande de fotos via paginado/carga incremental (FR-009)

**Scale/Scope**: Alcance de un estudio fotografico individual — pocas secciones (4-10) pero cada una puede tener decenas/cientos de fotos (de ahi el paginado de FR-009)

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- **I. Spec-Driven**: OK — este plan sigue a `spec.md` ya validado; `tasks.md` se genera despues con `/speckit-tasks`.
- **II. Scrum de una persona**: OK — feature mapea a Milestone "Epic 1 - Portfolio publico" e Issues #5/#6 (label `epic:1`) ya cargados.
- **III. Backend por feature, entidades ricas**: OK — `Section` y `Work` viven en los paquetes por-feature `sections`/`works` ya scaffoldeados en Epic 0 (`backend/src/main/java/com/fotos/{sections,works}/package-info.java`); la logica de "publicado/no publicado" y orden vive en las entidades de dominio, no en un service anemico.
- **IV. Sin checkout ni pago**: OK — no aplica, esta feature es solo contenido/galeria.
- **V. Testing real desde que haya logica**: OK — se agregan tests unitarios (dominio `Section`/`Work`) y de integracion con Testcontainers (repositorios/endpoints) siguiendo el patron ya validado en Epic 0.

Sin violaciones. No hace falta completar Complexity Tracking.

## Project Structure

### Documentation (this feature)

```text
specs/001-portfolio-publico/
├── plan.md              # This file (/speckit-plan command output)
├── research.md          # Phase 0 output (/speckit-plan command)
├── data-model.md        # Phase 1 output (/speckit-plan command)
├── quickstart.md        # Phase 1 output (/speckit-plan command)
├── contracts/           # Phase 1 output (/speckit-plan command)
└── tasks.md             # Phase 2 output (/speckit-tasks command - NOT created by /speckit-plan)
```

### Source Code (repository root)

```text
backend/src/main/java/com/fotos/
├── sections/
│   ├── Section.java              # entidad de dominio (nombre, slug, orden, publicado)
│   ├── SectionRepository.java
│   ├── SectionController.java    # GET /api/sections, GET /api/sections/{slug}
│   └── package-info.java         # ya existe (Epic 0)
└── works/
    ├── Work.java                 # entidad de dominio (section, minioObjectKey, orden, publicado)
    ├── WorkRepository.java
    ├── WorkController.java       # GET /api/sections/{slug}/works (paginado)
    └── package-info.java         # ya existe (Epic 0)

backend/src/main/resources/db/migration/
└── V2__portfolio_publico.sql     # tablas section/work + seed de demo

backend/src/test/java/com/fotos/
├── sections/SectionTest.java             # unitario (dominio)
├── works/WorkTest.java                   # unitario (dominio)
└── portfolio/PortfolioApiIntegrationTest.java  # Testcontainers, contra los endpoints

frontend/src/app/public/
├── home/
│   ├── home.ts            # ya scaffoldeado (Epic 0), se completa: lista secciones
│   ├── home.html
│   └── home.spec.ts
└── sections/
    ├── sections.ts         # ya scaffoldeado (Epic 0), se completa: galeria de una seccion
    ├── sections.html
    └── sections.spec.ts

frontend/src/app/public/
└── sections/section-detail/   # nuevo: vista ampliada de una foto (User Story 3)
    ├── section-detail.ts
    ├── section-detail.html
    └── section-detail.spec.ts

frontend/src/app/core/
└── portfolio.service.ts   # nuevo: cliente HTTP contra /api/sections, /api/sections/{slug}/works
```

**Structure Decision**: Web application ya establecida en Epic 0 (`backend/` Spring Boot por feature + `frontend/` Angular). Esta feature agrega dos features nuevas al backend (`sections`, `works`, ya con sus paquetes vacios scaffoldeados) y completa los componentes `public/home` y `public/sections` que Epic 0 dejo como stubs, sumando `public/sections/section-detail` para la vista ampliada.

## Complexity Tracking

*(sin violaciones — seccion no aplica)*
