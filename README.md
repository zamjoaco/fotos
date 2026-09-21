# Estudio de Fotografía — Sitio publicitario interactivo

Proyecto personal (aprendizaje de flujo de trabajo *spec-driven* con GitHub
**spec-kit**, organizado como un Scrum de una sola persona) que implementa el
sitio de marketing de un estudio de fotografía.

## Qué es esto

Un sitio de **portfolio + captura de leads + newsletter/bot de mailing +
panel de administración**, no un e-commerce clásico:

- Portfolio interactivo dividido en secciones (bodas, retratos, books,
  eventos, etc.).
- Secciones exclusivas: dejar el email desbloquea el contenido (un email da
  acceso a todas las secciones exclusivas).
- Los emails capturados alimentan una lista para un bot que envía
  publicidad/ofertas por mail (con link de baja/unsubscribe).
- Panel de administración: qué se publica, qué ofertas manda el bot, ABM de
  secciones/fotos, listado de leads.
- Contacto real de venta = botón a WhatsApp (`wa.me`). **No hay checkout ni
  pago en el sitio.**

## Stack tecnológico

| Capa | Tecnología |
|---|---|
| Backend | Spring Boot 3.5.x, Java 21, Maven, Spring Security 6, Spring Data JPA, Flyway |
| Frontend | Angular + Tailwind CSS |
| Base de datos | PostgreSQL 16 |
| Cache / rate-limiting | Redis |
| Almacenamiento de imágenes | MinIO (S3-compatible) |
| Envío de mail (dev) | Mailhog |
| Docs de API | springdoc-openapi (Swagger UI) |
| Testing backend | JUnit 5, Mockito, Testcontainers |

Arquitectura del backend: paquetes **por feature** (`sections`, `works`,
`leads`, `campaigns`, `auth`), con entidades de dominio ricas (POO real, no
anémico/transaction-script).

## Estructura del repo

```
/fotos
  /backend        Spring Boot (Java 21, Maven, PostgreSQL, Flyway)
  /frontend        Angular + Tailwind (sitio público + /admin protegido)
  /specs           carpeta de spec-kit (spec.md/plan.md/tasks.md por feature)
  /docs            documentación complementaria (no code)
  docker-compose.yml   backend + frontend + postgres + redis + minio + mailhog (dev)
  .specify/        generado por `specify init` (templates, slash commands)
```

## Cómo levantar el entorno local

Requisitos: Docker.

```bash
cp .env.example .env    # ajustar valores si hace falta (nunca commitear .env)
docker compose up --build
```

Servicios expuestos (puertos por defecto, configurables en `.env`):

- Frontend: http://localhost:4200
- Backend (API + Swagger UI): http://localhost:8080
- Backend healthcheck: http://localhost:8080/actuator/health
- MinIO console: http://localhost:9001
- Mailhog UI: http://localhost:8025
- PostgreSQL: localhost:5432
- Redis: localhost:6379

El admin inicial se crea la primera vez que arranca el backend, leyendo
`ADMIN_INITIAL_EMAIL` / `ADMIN_INITIAL_PASSWORD` del `.env` (hasheado con
BCrypt antes de persistir).

## Flujo de trabajo

### Spec-kit (por feature)

Cada feature/Epic se desarrolla con los slash commands de spec-kit, en este
orden, y cada paso queda persistido en `/specs/<feature>/`:

1. `/specify` — qué y por qué (historias de usuario, criterios de
   aceptación).
2. `/plan` — cómo, stack, contratos, modelo de datos.
3. `/tasks` — lista de tareas chicas y ordenadas.
4. `/implement` — ejecuta las tareas.

### Scrum de una persona

En vez de un tablero físico se usa **GitHub Projects (Kanban)** + **Issues**
+ **Milestones**:

- Un **Milestone** por Epic (`Epic N - Nombre`).
- Issues con título en formato historia de usuario ("Como \<rol\> quiero
  \<acción\> para \<beneficio\>") y label `epic:N`.
- Las historias de usuario finas de cada Epic se redactan al hacer
  `/specify` de esa feature (en Epic 0 solo se cargan 1-2 Issues
  placeholder por Epic).

### Backlog (Epics)

0. **Fundación** — scaffold del repo, Docker, spec-kit, CI de solo build,
   Project board.
1. **Portfolio público** — secciones, galería responsive, home.
2. **Secciones exclusivas + captura de leads** — gate por email, alta de
   `Lead`, acceso a la sección.
3. **Bot de mailing** — ABM de campañas/ofertas, envío a leads suscriptos,
   plantillas, unsubscribe.
4. **Panel de administración** — login admin, CRUD de contenido, listado de
   leads, disparo de campañas.
5. **Contacto WhatsApp** — botón/CTA configurable desde el admin.
6. **Pulido UI** — pasada de diseño (skill Impeccable).
7. **Hardening & Deploy** — seguridad, backups, CI completa, despliegue.

Detalle completo de cada Epic: ver Issues/Milestones del repo y las specs en
`/specs/<feature>/` una vez desarrolladas.

## Testing

- Backend: JUnit 5 + Mockito (unitarios) y Testcontainers (integración
  contra PostgreSQL real). A partir del Epic 1, la CI corre `mvn verify`
  completo en cada PR.
- Frontend: se define su estrategia de testing al llegar a los Epics que
  agregan lógica de UI relevante.
