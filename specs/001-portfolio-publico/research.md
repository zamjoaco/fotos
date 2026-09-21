# Research: Portfolio publico

No quedaron `NEEDS CLARIFICATION` en el Technical Context del plan; este documento registra las decisiones tecnicas necesarias para bajar la spec a diseño (Phase 1), no resuelve ambiguedades de negocio (esas ya se resolvieron en `spec.md`).

## 1. Como servir las imagenes de MinIO al publico

**Decision**: El backend genera URLs presignadas (pre-signed, de corta duracion) de MinIO para cada `Work` y las incluye en la respuesta del endpoint, en vez de hacer streaming/proxy del binario a traves del propio backend.

**Rationale**: MinIO ya soporta URLs presignadas nativamente (cliente `io.minio:minio` ya en `pom.xml` desde Epic 0). Evita que el backend cargue el peso de servir binarios de imagenes (CPU/memoria/ancho de banda), lo cual es clave para SC-002 (primeras fotos visibles en <2s). El bucket de MinIO no necesita quedar publico, asi que sigue controlado por el backend cuando expira cada URL.

**Alternatives considered**:
- *Proxy/streaming por el backend*: mas simple de un lado, pero el backend se vuelve cuello de botella de ancho de banda para algo que MinIO ya resuelve mejor; descartado.
- *Bucket publico + URLs directas*: mas rapido de implementar, pero pierde control de acceso a futuro (por ejemplo si Epic 2 necesita ocultar imagenes de secciones exclusivas antes del gate); descartado para no tener que revertirlo despues.

## 2. Paginado/carga incremental de la galeria (FR-009)

**Decision**: El endpoint `GET /sections/{slug}/works` pagina por `page`/`size` (offset-based, default `size=24`), ordenado por el campo `orden` de `Work`. El frontend usa scroll infinito (carga la siguiente pagina al acercarse al final del listado).

**Rationale**: Offset-based pagination es standard de Spring Data JPA (`Pageable`) y no agrega dependencias nuevas. `size=24` balancea el objetivo de SC-002 (carga rapida inicial) con no generar demasiados round-trips en secciones chicas.

**Alternatives considered**:
- *Cursor-based pagination*: mas eficiente a gran escala, pero es complejidad innecesaria para el volumen esperado (Scale/Scope: decenas/cientos de fotos por seccion, no millones); descartado por YAGNI.
- *Cargar todo de una*: simple pero viola FR-009 directamente para secciones grandes; descartado.

## 3. Vista ampliada de una foto (User Story 3)

**Decision**: Ruta hija de Angular Router (`/secciones/:slug/:workId`) en vez de modal superpuesto sobre el mismo componente.

**Rationale**: Da URL propia a cada foto (compartible, back/forward del browser funciona naturalmente), mas alineado con "no debe recargar toda la pagina" (acceptance scenario de User Story 3) usando el router de Angular sin necesitar un modal-manager custom.

**Alternatives considered**:
- *Modal/overlay con estado local del componente*: evita definir una ruta nueva, pero no da URL compartible ni historial de navegador; descartado.

## 4. Secciones inexistentes/no publicadas (FR-004)

**Decision**: El backend responde 404 tanto para un slug que no existe como para uno que existe pero no esta publicado — mismo status, mismo body generico, sin distinguir el motivo.

**Rationale**: Cumple SC-004 (0% de exposicion de informacion sobre secciones ocultas) sin logica adicional: la consulta del repositorio ya filtra por `publicado = true`, asi que ambos casos caen en "no encontrado" por construccion.

**Alternatives considered**: Ninguna otra opcion evaluada — es la unica forma de no filtrar informacion por timing/contenido de la respuesta.

## 5. Seed de contenido de demo (FR-007)

**Decision**: La migracion Flyway (`V2__portfolio_publico.sql`) crea las tablas `section`/`work` y carga secciones de ejemplo (bodas, retratos, books, eventos) sin fotos reales asociadas (referencias a objetos de MinIO son responsabilidad de un script/fixture separado, ver `docs/seed-portfolio-publico.md`, fuera de esta migracion versionada).

**Rationale**: Mantiene la migracion de esquema (Flyway, versionada, corre en todo ambiente) separada del contenido de demo real (que depende de que existan objetos subidos a MinIO, algo que no tiene sentido versionar en SQL). Las secciones sin `Work` asociado ya estan cubiertas por el edge case "estado vacio prolijo" de la spec.

**Alternatives considered**:
- *Seed con URLs de imagenes externas (placeholder.com, etc.)*: violaria FR-008 (imagenes deben venir del almacenamiento de objetos propio) y añadiria una dependencia de red externa a los tests de integracion; descartado.

## 6. Prefijo `/api` de los controllers (encontrado al validar end-to-end)

**Decision**: `SectionController` y `WorkController` mapean sin prefijo (`/sections`, no `/api/sections`).

**Rationale**: `frontend/proxy.conf.json` (Epic 0) ya reenvia `/api/*` a `http://backend:8080` haciendo `pathRewrite` de `^/api` a vacio — asi es como `/api/actuator/health` llegaba a `/actuator/health` en la validacion de Epic 0. Los primeros controllers de esta feature se escribieron con `@RequestMapping("/api/sections")`, duplicando el prefijo: el proxy lo sacaba y el backend esperaba encontrarlo, asi que toda request via el proxy daba 404. Se detecto recien al validar `quickstart.md` con el stack real (los tests con `TestRestTemplate` pegandole directo al backend no lo hubieran detectado). Se sincroniza el backend con la convencion que ya establecio Epic 0.

**Alternatives considered**: sacar el `pathRewrite` del proxy y mantener `/api` en los controllers — descartado porque hubiera roto `/api/actuator/health`, que ya funcionaba.

## 7. Region explicita en el MinioClient (encontrado al validar end-to-end)

**Decision**: el bean `MinioClient` fija `.region("us-east-1")` explicitamente.

**Rationale**: la decision #1 asumia que generar una URL presignada era 100% local (sin red). En la practica, el SDK de MinIO (8.5.17) hace un round-trip real contra el `endpoint` configurado para resolver la region del bucket antes de firmar, salvo que la region se pase explicita. Como el `endpoint` ahora es el publico (`http://localhost:9000`, decision #1) y no es alcanzable *desde dentro* del contenedor del backend, esa llamada fallaba con `Connection refused`. Fijar la region evita el round-trip: el presigning queda local de verdad, como se habia asumido.

**Alternatives considered**: mantener dos `MinioClient` (uno interno para llamadas de red, otro publico solo para firmar) — mas correcto a largo plazo si el backend algun dia necesita hablarle de verdad a MinIO (uploads server-side), pero over-engineering para lo que esta feature necesita hoy (solo presigning). Revisar si Epic 4+ agrega uploads reales.
