# Contrato: API de portfolio publico

Endpoints REST de solo lectura, sin autenticacion (contenido publico). Documentados tambien en Swagger UI via springdoc-openapi (ya en `pom.xml`).

Rutas tal como las expone el backend (`http://localhost:8080/...`). El frontend las consume via `/api/...` (`environment.apiUrl`), y el proxy/reverse-proxy le saca el prefijo `/api` antes de reenviar (ver `frontend/proxy.conf.json`) — el backend en si NO tiene el prefijo `/api`.

## `GET /sections`

Lista las secciones publicadas, para la home.

**Respuesta 200**:

```json
[
  {
    "slug": "bodas",
    "nombre": "Bodas",
    "orden": 0,
    "cantidadWorks": 12
  }
]
```

- Orden: ascendente por `orden`.
- `cantidadWorks`: cuenta solo `Work` publicados de esa seccion (para que la home pueda mostrar "sin fotos todavia" sin pegarle a `/works`).
- Lista vacia (`[]`) si no hay ninguna seccion publicada — no es un error (edge case de la spec).

## `GET /sections/{slug}`

Detalle de una seccion (para el header de la vista de galeria).

**Respuesta 200**:

```json
{
  "slug": "bodas",
  "nombre": "Bodas",
  "orden": 0
}
```

**Respuesta 404**: `slug` no existe o su `Section` no esta publicada. Mismo body generico en ambos casos (FR-004 / research.md #4):

```json
{
  "error": "not_found"
}
```

## `GET /sections/{slug}/works?page={n}&size={n}`

Galeria paginada de una seccion.

**Query params**:
- `page` (default `0`)
- `size` (default `24`, max `100`)

**Respuesta 200**:

```json
{
  "content": [
    {
      "id": "b3f1...",
      "imageUrl": "https://minio.../bucket/obj?X-Amz-...",
      "imageUrlExpiraEn": "2026-09-21T21:00:00Z",
      "orden": 0
    }
  ],
  "page": 0,
  "size": 24,
  "totalElements": 57,
  "totalPages": 3
}
```

- `imageUrl`: URL presignada de MinIO (research.md #1), de corta duracion — el frontend no debe cachearla mas alla de `imageUrlExpiraEn`.
- Si la `Section` no existe o no esta publicada: 404, mismo contrato que `GET /sections/{slug}`.
- Si la `Section` existe, esta publicada, pero no tiene `Work` publicados: 200 con `"content": []` (estado vacio, no error).
- **Respuesta 503**: MinIO no esta habilitado (`storage.minio.enabled=false`, el default) y no se puede generar `imageUrl` para ningun `Work` de la pagina:

```json
{
  "error": "storage_not_configured"
}
```

## `GET /sections/{slug}/works/{workId}`

Detalle de una foto individual, para la vista ampliada (User Story 3).

**Respuesta 200**:

```json
{
  "id": "b3f1...",
  "imageUrl": "https://minio.../bucket/obj?X-Amz-...",
  "imageUrlExpiraEn": "2026-09-21T21:00:00Z",
  "orden": 0,
  "anteriorId": "a2e0...",
  "siguienteId": "c4g2..."
}
```

- `anteriorId`/`siguienteId`: `null` si es la primera/ultima foto publicada de la seccion (para la navegacion "siguiente/anterior" sin recargar, acceptance scenario 2 de User Story 3).
- **Respuesta 404**: seccion no encontrada/no publicada, o `workId` no pertenece a esa seccion / no publicado — mismo contrato generico.
