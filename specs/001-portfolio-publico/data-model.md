# Data Model: Portfolio publico

## Section

Representa una categoria del portfolio (bodas, retratos, books, eventos, etc.). Entidad rica: expone `publicar()`/`despublicar()` y valida su propio slug, no un service anemico manipulando campos directamente.

| Campo | Tipo | Notas |
|---|---|---|
| `id` | UUID | PK |
| `nombre` | String, requerido | Nombre visible (ej. "Bodas") |
| `slug` | String, requerido, unico | Identificador de URL (ej. `bodas`), inmutable una vez creado |
| `orden` | int | Orden de aparicion en la home |
| `publicado` | boolean, default `false` | Controla si es visible al publico (FR-004) |
| `creadoEn` | timestamp | Auditoria |

**Relaciones**: una `Section` tiene muchos `Work` (1:N).

**Validaciones**:
- `slug` unico, formato `[a-z0-9-]+` (sin espacios, sin mayusculas).
- No puede haber dos `Section` con el mismo `orden` visible simultaneamente publicadas (se resuelve por orden ascendente + desempate por `creadoEn`, no es una constraint de unicidad dura).

**Transiciones de estado**: `publicado: false -> true` (accion "publicar", futura, vive en Epic 4) y `true -> false` (accion "despublicar"). En esta feature el campo se puebla solo via seed; el ABM real que dispara estas transiciones es Epic 4.

## Work

Representa una foto publicada dentro de una `Section`.

| Campo | Tipo | Notas |
|---|---|---|
| `id` | UUID | PK |
| `section_id` | UUID, FK -> Section | Requerido |
| `minioObjectKey` | String, requerido | Key del objeto en el bucket de MinIO (no la URL; la URL presignada se genera en tiempo de request, ver research.md #1) |
| `orden` | int | Orden dentro de la seccion (para el paginado de FR-009) |
| `publicado` | boolean, default `false` | Un `Work` puede existir pero no estar publicado independientemente de su `Section` |
| `creadoEn` | timestamp | Auditoria |

**Relaciones**: pertenece a una `Section` (N:1). Si se borra una `Section`, sus `Work` se borran en cascada (no tiene sentido un `Work` huerfano).

**Validaciones**:
- `minioObjectKey` no vacio.
- Un `Work` solo es visible al publico si tanto el como su `Section` padre tienen `publicado = true` (regla de negocio que vive en el repositorio/query, no en el controller).

**Transiciones de estado**: igual que `Section.publicado`, disparadas por el futuro ABM de Epic 4; en esta feature el campo se puebla solo via seed.

## Consultas derivadas (no son entidades, documentan el contrato de lectura)

- **Secciones visibles en home**: `Section` con `publicado = true`, ordenadas por `orden`.
- **Works visibles de una seccion**: `Work` con `publicado = true` cuya `Section` tiene `publicado = true` y `slug` coincide, ordenados por `orden`, paginados (ver research.md #2).
- **Seccion por slug (no encontrada)**: si no existe ninguna `Section` con ese `slug` y `publicado = true`, la consulta no distingue "no existe" de "existe pero no publicada" (FR-004 / research.md #4).
