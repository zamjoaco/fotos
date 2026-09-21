# Quickstart: validar Portfolio publico end-to-end

## Prerrequisitos

- Stack de Epic 0 levantado: `docker compose up --build` desde la raiz del repo (ver README).
- Migracion `V2__portfolio_publico.sql` aplicada (Flyway corre sola al levantar el backend).
- Al menos una `Section` publicada con `Work` publicados y sus objetos subidos a MinIO (via el seed de esta feature, o subida manual al bucket `fotos-works` por la consola de MinIO en `http://localhost:9001`).

## Validar el backend (API)

1. Listar secciones publicadas:
   ```
   curl http://localhost:8080/api/sections
   ```
   Esperado: 200, array con al menos una seccion (ej. `bodas`).

2. Traer la galeria paginada de una seccion:
   ```
   curl "http://localhost:8080/api/sections/bodas/works?page=0&size=24"
   ```
   Esperado: 200, `content` con `imageUrl` presignadas de MinIO que abren directo en el browser.

3. Confirmar que una seccion no publicada/inexistente da 404 sin distinguir el motivo:
   ```
   curl -i http://localhost:8080/api/sections/no-existe
   ```
   Esperado: `HTTP/1.1 404`.

4. Swagger UI (`http://localhost:8080/swagger-ui.html`) debe listar los 4 endpoints de `contracts/portfolio-api.md`.

## Validar el frontend

1. Abrir `http://localhost:4200/` (via el proxy de Epic 0, `/api/*` llega al backend).
2. **Home**: debe listar las secciones publicadas con link a cada una. Si no hay ninguna seccion publicada, debe verse un estado vacio prolijo, no una pantalla en blanco ni un error de consola.
3. **Seccion**: entrar a `/secciones/bodas` (o el slug que tengas publicado) y verificar que la galeria carga las fotos, con scroll infinito si hay mas de 24.
4. **Detalle de foto**: click en una foto -> navega a `/secciones/bodas/<workId>` sin recargar toda la pagina, y los botones siguiente/anterior cambian de foto sin volver a la galeria.
5. **Responsive**: con las devtools del browser, probar el layout en 360px y en 1920px de ancho — sin scroll horizontal, sin contenido cortado (SC-003).
6. **404**: entrar a `/secciones/no-existe` y verificar que el frontend muestra un "no encontrado" (no una pantalla rota ni un error sin manejar).

## Validar tests automatizados

```
cd backend
./mvnw verify   # requiere Docker (Testcontainers), igual que el smoke test de Epic 0
```

Esperado: unitarios de `Section`/`Work` en verde, integracion `PortfolioApiIntegrationTest` en verde (levanta un Postgres real via Testcontainers y ejercita los 4 endpoints).
