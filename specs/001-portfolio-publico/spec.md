# Feature Specification: Portfolio publico

**Feature Branch**: `001-portfolio-publico`

**Created**: 2026-09-21

**Status**: Draft

**Input**: User description: "Epic 1 - Portfolio publico: sitio de marketing de un estudio de fotografia, portfolio interactivo dividido en secciones (bodas, retratos, books, eventos, etc.), con galeria responsive y una home. Es contenido publico, sin gate de email (eso es Epic 2). El admin gestiona que secciones/fotos se publican (el ABM real del admin llega en Epic 4; por ahora las secciones/fotos pueden poblarse via Flyway seed o fixtures para poder mostrar el portfolio). Referencia: Issues #5 y #6 del repo (epic:1), y el stack ya definido (Spring Boot backend por feature, Angular + Tailwind frontend, Postgres, MinIO para las imagenes)."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Ver la home del estudio (Priority: P1)

Un visitante entra al sitio y ve una home que presenta al estudio de fotografia: una seleccion destacada de trabajos y un punto de entrada claro a cada seccion del portfolio.

**Why this priority**: Es la puerta de entrada del sitio; sin una home que comunique de que se trata el estudio, ninguna otra pantalla tiene contexto. Es el MVP minimo demostrable.

**Independent Test**: Se puede probar completamente cargando la home sin autenticacion y verificando que se ve contenido destacado y links a las secciones, sin depender de ninguna otra historia.

**Acceptance Scenarios**:

1. **Given** un visitante sin cuenta ni email registrado, **When** entra a la URL raiz del sitio, **Then** ve la home con una presentacion del estudio y accesos a las secciones del portfolio publicadas.
2. **Given** que no hay ninguna seccion publicada todavia, **When** un visitante entra a la home, **Then** ve un estado vacio prolijo (sin errores) en vez de una pantalla rota.

---

### User Story 2 - Navegar las secciones del portfolio (Priority: P1)

Un visitante navega a una seccion especifica (bodas, retratos, books, eventos, etc.) y ve una galeria de las fotos publicadas de esa categoria.

**Why this priority**: Es el contenido central del Epic ("portfolio interactivo dividido en secciones") y la razon de negocio del sitio: mostrar trabajo real para convertir visitas en contactos por WhatsApp (Epic 5).

**Independent Test**: Se puede probar entrando directo a la URL de una seccion publicada y verificando que se listan sus fotos, independientemente de si la home ya esta terminada.

**Acceptance Scenarios**:

1. **Given** una seccion publicada con fotos, **When** un visitante la abre, **Then** ve todas las fotos publicadas de esa seccion en una galeria.
2. **Given** una seccion que existe pero no tiene fotos publicadas, **When** un visitante la abre, **Then** ve un estado vacio prolijo en vez de una galeria rota.
3. **Given** una seccion que no existe o no esta publicada, **When** un visitante intenta acceder a su URL, **Then** el sitio responde con un "no encontrado" claro (no un error generico).

---

### User Story 3 - Ver una foto en detalle dentro de la galeria (Priority: P2)

Dentro de una seccion, un visitante elige una foto de la galeria para verla en tamano mas grande.

**Why this priority**: Mejora la experiencia de "portfolio interactivo" pero el valor central (mostrar el trabajo) ya esta cubierto por la User Story 2 con miniaturas; esto es una mejora de UX, no un bloqueante para el MVP.

**Independent Test**: Se puede probar sobre una seccion ya poblada, haciendo click/tap en una foto y verificando que se amplia sin salir de la seccion.

**Acceptance Scenarios**:

1. **Given** una galeria de seccion con fotos, **When** el visitante selecciona una foto, **Then** la ve ampliada manteniendo el contexto de la seccion (puede volver a la galeria facilmente).
2. **Given** que el visitante esta viendo una foto ampliada, **When** navega a la foto siguiente o anterior, **Then** el sitio la muestra sin recargar toda la pagina.

---

### Edge Cases

- Que pasa si una seccion tiene un volumen muy grande de fotos (cientos): la galeria debe seguir siendo usable (paginado o carga incremental), no cargar todo de una.
- Como se comporta la galeria en un dispositivo movil angosto: debe reflowear a un layout de una sola columna sin recortar contenido.
- Que pasa si una imagen fuente falla al cargar (broken image): el layout de la galeria no debe romperse, debe degradar prolijamente.
- Que pasa si se accede a una seccion valida pero despublicada (dada de baja por el admin en Epic 4 a futuro): debe comportarse igual que "no encontrado", no debe filtrar el nombre de la seccion.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: El sistema MUST mostrar una home publica, accesible sin autenticacion, que presente al estudio y liste las secciones de portfolio publicadas.
- **FR-002**: El sistema MUST permitir navegar a cada seccion publicada del portfolio (bodas, retratos, books, eventos, y cualquier otra que se cargue) desde la home.
- **FR-003**: El sistema MUST mostrar, dentro de cada seccion, una galeria responsive con todas las fotos publicadas de esa seccion.
- **FR-004**: El sistema MUST devolver un estado "no encontrado" para secciones inexistentes o no publicadas, sin revelar si la seccion existe pero esta oculta.
- **FR-005**: El sistema MUST mostrar un estado vacio (sin fotos) de forma prolija cuando una seccion publicada todavia no tiene fotos cargadas.
- **FR-006**: El sistema MUST permitir ampliar una foto individual dentro de la galeria de su seccion (User Story 3).
- **FR-007**: El contenido de secciones y fotos MUST poder cargarse en el entorno de desarrollo/demo sin depender del panel de administracion (que llega en Epic 4), via datos de siembra (seed).
- **FR-008**: El sistema MUST servir las imagenes del portfolio desde el almacenamiento de objetos ya provisionado por la infraestructura del proyecto, en vez de servirlas embebidas en el backend.
- **FR-009**: La galeria de una seccion MUST seguir siendo usable (sin degradar la performance percibida) cuando la seccion tiene un volumen grande de fotos, mediante carga incremental o paginado.

### Key Entities *(include if feature involves data)*

- **Section (Seccion)**: Categoria del portfolio (bodas, retratos, books, eventos, etc.). Tiene un nombre visible, un identificador de URL (slug), un estado de publicacion, y un orden de aparicion en la home. Es la entidad que Epic 2 (secciones exclusivas) va a extender con el gate de email.
- **Work (Foto/Trabajo)**: Una foto publicada dentro de una Section. Tiene una referencia a la imagen almacenada en MinIO, un estado de publicacion propio, y un orden dentro de su seccion.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Un visitante puede llegar desde la home hasta ver las fotos de cualquier seccion publicada en 2 clicks o menos.
- **SC-002**: La galeria de una seccion muestra sus primeras fotos visibles en pantalla en menos de 2 segundos en una conexion de banda ancha estandar.
- **SC-003**: El layout de home y de galeria se adapta correctamente (sin scroll horizontal ni contenido cortado) en anchos de pantalla desde 360px (mobile) hasta 1920px (desktop).
- **SC-004**: 0% de las URLs de secciones no publicadas o inexistentes exponen contenido o distinguen su motivo de "no encontrado" frente a un visitante no autenticado.

## Assumptions

- El ABM real de secciones/fotos desde un panel de administracion es explicitamente de Epic 4; esta feature asume que el contenido se carga por datos de siembra (Flyway seed / fixtures) para poder demostrarse end-to-end antes de que exista ese panel.
- Las secciones del portfolio en este Epic son todas de acceso publico; el gate por email de "secciones exclusivas" es Epic 2 y no se implementa aca (esta feature sienta las bases de datos que Epic 2 va a extender, no el gate en si).
- El almacenamiento de imagenes (MinIO) ya esta provisionado por Epic 0; esta feature es la primera en consumirlo activamente.
- No hay carrito, pago ni checkout involucrado (fuera de alcance de todo el proyecto, ver constitution).
- El listado de secciones a cargar por seed usa como referencia los ejemplos ya mencionados en el README/backlog: bodas, retratos, books, eventos.
