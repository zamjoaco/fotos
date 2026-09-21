---

description: "Task list for Portfolio publico (Epic 1)"
---

# Tasks: Portfolio publico

**Input**: Design documents from `/specs/001-portfolio-publico/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/portfolio-api.md, quickstart.md

**Tests**: Incluidos (constitution Principio V exige JUnit5+Mockito+Testcontainers desde que haya logica).

**Organization**: Tareas agrupadas por historia de usuario (spec.md) para poder implementar y probar cada una de forma independiente.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Puede correr en paralelo (archivos distintos, sin dependencias pendientes)
- **[Story]**: A que historia de usuario pertenece (US1, US2, US3)

## Phase 1: Setup

**Purpose**: Habilitar la infraestructura que Epic 0 dejo scaffoldeada pero apagada.

- [X] T001 Habilitar el bean real de `MinioClient` (`storage.minio.enabled`, ya declarado en `backend/src/main/resources/application.yml` desde Epic 0) creando `backend/src/main/java/com/fotos/works/MinioConfig.java`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Entidades, repositorios y el servicio de URLs presignadas que las 3 historias necesitan.

**⚠️ CRITICAL**: Ninguna historia arranca antes de terminar esta fase.

- [X] T002 Crear migracion Flyway `backend/src/main/resources/db/migration/V2__portfolio_publico.sql`: tabla `section` (`id uuid pk`, `nombre text not null`, `slug text not null unique`, `orden int not null`, `publicado boolean not null default false`, `creado_en timestamptz not null default now()`) y tabla `work` (`id uuid pk`, `section_id uuid not null references section on delete cascade`, `minio_object_key text not null`, `orden int not null`, `publicado boolean not null default false`, `creado_en timestamptz not null default now()`) — ver data-model.md
- [X] T003 [P] Crear entidad de dominio `Section` en `backend/src/main/java/com/fotos/sections/Section.java`: metodos `publicar()`/`despublicar()`, valida `slug` con formato `[a-z0-9-]+` (data-model.md)
- [X] T004 [P] Crear entidad de dominio `Work` en `backend/src/main/java/com/fotos/works/Work.java`: valida `minioObjectKey` no vacio (data-model.md)
- [X] T005 [P] Crear `SectionRepository` en `backend/src/main/java/com/fotos/sections/SectionRepository.java` con `findByPublicadoTrueOrderByOrdenAsc()` y `findBySlugAndPublicadoTrue(String slug)`
- [X] T006 [P] Crear `WorkRepository` en `backend/src/main/java/com/fotos/works/WorkRepository.java` con query paginada `findBySection_SlugAndPublicadoTrueAndSection_PublicadoTrueOrderByOrdenAsc(String slug, Pageable pageable)` y helpers para el `Work` anterior/siguiente por `orden`
- [X] T007 Crear `MinioPresignedUrlService` en `backend/src/main/java/com/fotos/works/MinioPresignedUrlService.java`: genera URLs presignadas de corta duracion a partir de `minioObjectKey` (research.md #1), consumido por T017/T022
- [X] T008 [P] Unitario `SectionTest` en `backend/src/test/java/com/fotos/sections/SectionTest.java`: valida formato de slug y las transiciones `publicar()`/`despublicar()`
- [X] T009 [P] Unitario `WorkTest` en `backend/src/test/java/com/fotos/works/WorkTest.java`: valida que `minioObjectKey` vacio o null es rechazado

**Checkpoint**: entidades, repos y servicio de URLs listos — arranca cualquier historia.

---

## Phase 3: User Story 1 - Ver la home del estudio (Priority: P1) 🎯 MVP

**Goal**: un visitante ve la home con las secciones publicadas y un acceso claro a cada una.

**Independent Test**: `GET /api/sections` responde el listado correcto y `home.ts` lo renderiza, sin depender de las demas historias.

### Tests for User Story 1

- [X] T010 [P] [US1] Crear `PortfolioApiIntegrationTest` en `backend/src/test/java/com/fotos/portfolio/PortfolioApiIntegrationTest.java` (Testcontainers, patron de `FotosBackendApplicationTests`) con el caso `GET /api/sections` devuelve solo secciones `publicado=true` ordenadas por `orden`, y `[]` si no hay ninguna

### Implementation for User Story 1

- [X] T011 [US1] Implementar `SectionController#listar` (`GET /api/sections`) en `backend/src/main/java/com/fotos/sections/SectionController.java`: `slug`/`nombre`/`orden`/`cantidadWorks` por seccion, segun `contracts/portfolio-api.md`
- [X] T012 [US1] Completar `frontend/src/app/core/portfolio.service.ts` con `listarSecciones()` (`GET /api/sections`)
- [X] T013 [US1] Completar `frontend/src/app/public/home/home.ts` + `home.html`: listar secciones con link a `/secciones/:slug`, estado vacio prolijo si no hay ninguna publicada
- [X] T014 [P] [US1] `frontend/src/app/public/home/home.spec.ts`: test de listado con secciones y de estado vacio

**Checkpoint**: la home funciona de punta a punta de forma independiente.

---

## Phase 4: User Story 2 - Navegar las secciones del portfolio (Priority: P1)

**Goal**: un visitante abre una seccion y ve la galeria paginada de sus fotos.

**Independent Test**: `GET /api/sections/{slug}` y `GET /api/sections/{slug}/works` responden segun el contrato, y `sections.ts` pinta la galeria con scroll infinito — probable sin depender de la home.

### Tests for User Story 2

- [X] T015 [P] [US2] Extender `PortfolioApiIntegrationTest` con: `GET /api/sections/{slug}` (200 publicada / 404 inexistente o no publicada, mismo body) y `GET /api/sections/{slug}/works` (paginado con `size` default 24 y max 100, `[]` si la seccion no tiene works publicados)

### Implementation for User Story 2

- [X] T016 [US2] Implementar `SectionController#detalle` (`GET /api/sections/{slug}`) con 404 uniforme para inexistente/no-publicada (research.md #4)
- [X] T017 [US2] Implementar `WorkController` (`GET /api/sections/{slug}/works`) en `backend/src/main/java/com/fotos/works/WorkController.java`: `Pageable` (`size` default 24, max 100), usa `MinioPresignedUrlService` (T007) para `imageUrl`/`imageUrlExpiraEn`
- [X] T018 [P] [US2] Completar `frontend/src/app/core/portfolio.service.ts` con `obtenerSeccion(slug)` y `listarWorks(slug, page)`
- [X] T019 [US2] Completar `frontend/src/app/public/sections/sections.ts` + `sections.html`: galeria responsive con scroll infinito (carga la pagina siguiente al acercarse al final), estado vacio, manejo del 404 como "no encontrado"
- [X] T020 [P] [US2] `frontend/src/app/public/sections/sections.spec.ts`: tests de galeria paginada, estado vacio y 404

**Checkpoint**: US1 + US2 cubren el MVP completo de Epic 1 (ambas P1).

---

## Phase 5: User Story 3 - Ver una foto en detalle dentro de la galeria (Priority: P2)

**Goal**: un visitante amplia una foto sin perder el contexto de la seccion, con navegacion siguiente/anterior.

**Independent Test**: `GET /api/sections/{slug}/works/{workId}` responde `anteriorId`/`siguienteId` segun el contrato, y la ruta `/secciones/:slug/:workId` navega sin recargar toda la pagina.

### Tests for User Story 3

- [X] T021 [P] [US3] Extender `PortfolioApiIntegrationTest` con `GET /api/sections/{slug}/works/{workId}`: 200 con `anteriorId`/`siguienteId` (`null` en los extremos), 404 si el `workId` no pertenece a la seccion o no esta publicado

### Implementation for User Story 3

- [X] T022 [US3] Agregar `WorkController#detalle` (`GET /api/sections/{slug}/works/{workId}`) segun `contracts/portfolio-api.md`
- [X] T023 [P] [US3] Agregar ruta hija `/secciones/:slug/:workId` en `frontend/src/app/app.routes.ts`
- [X] T024 [US3] Crear `frontend/src/app/public/sections/section-detail/section-detail.ts` + `.html`: vista ampliada, botones siguiente/anterior sin recargar la pagina (usa `anteriorId`/`siguienteId`)
- [X] T025 [P] [US3] `frontend/src/app/public/sections/section-detail/section-detail.spec.ts`

**Checkpoint**: las 3 historias funcionan de forma independiente entre si.

---

## Phase 6: Polish & Cross-Cutting Concerns

- [X] T026 [P] Seed de secciones de demo (bodas, retratos, books, eventos) — documentar en `docs/` como subir objetos de prueba a MinIO para poblar `Work`, ya que la migracion (T002) no versiona binarios (research.md #5)
- [X] T027 Correr `quickstart.md` completo (backend, frontend, responsive 360-1920px, caso 404) y corregir lo que falle — encontro y arreglo 2 bugs reales (endpoint publico vs interno de MinIO + region para presigning, ver research.md #6/#7; prefijo `/api` duplicado entre proxy y controllers). Validado con Docker real: build, `docker compose up`, curl a los 4 endpoints, subida de un objeto de prueba a MinIO, y navegacion real en el browser (home -> seccion -> foto ampliada).
- [ ] T028 Code-review con `opencode` (modelo `big-pickle`) como reviewer independiente antes de abrir el PR, mismo patron que Epic 0

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: sin dependencias, arranca primero
- **Foundational (Phase 2)**: depende de Setup — bloquea las 3 historias
- **User Stories (Phase 3-5)**: dependen de Foundational; US1 y US2 son ambas P1 y no dependen entre si (pueden ir en paralelo o secuencial); US3 (P2) consume las mismas entidades pero es independiente de US1/US2 en su propio endpoint/ruta
- **Polish (Phase 6)**: depende de que las historias que se quieran entregar esten completas

### Parallel Opportunities

- T003-T006, T008-T009 (Foundational) en paralelo entre si — archivos distintos
- Dentro de cada historia, las tareas `[P]` (tests, service de frontend, specs) en paralelo con la implementacion de otra parte de la misma historia
- US1 y US2 pueden trabajarse en paralelo una vez cerrada la Fase 2 (comparten controller de `sections` en el mismo archivo `SectionController.java`, asi que T011 y T016 mejor secuenciales si es la misma persona tocando el archivo)

---

## Implementation Strategy

### MVP primero (User Story 1 + User Story 2, ambas P1)

1. Fase 1 (Setup) → Fase 2 (Foundational)
2. Fase 3 (US1, home) → validar independiente
3. Fase 4 (US2, galeria de seccion) → validar independiente
4. **Con esto ya hay un MVP demostrable end-to-end** (home + navegacion de secciones)
5. Fase 5 (US3, detalle de foto) es la mejora de UX que cierra el Epic 1 completo
6. Fase 6 (Polish) antes de abrir el PR
