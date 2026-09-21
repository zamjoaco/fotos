-- V2__portfolio_publico.sql
-- Epic 1 (Portfolio publico): tablas base de Section/Work.
-- El ABM real (Epic 4) todavia no existe; el contenido se carga por seed
-- separado (ver docs/, tarea T026) para no versionar datos de demo aca.

CREATE TABLE section (
    id          UUID PRIMARY KEY,
    nombre      TEXT NOT NULL,
    slug        TEXT NOT NULL UNIQUE,
    orden       INTEGER NOT NULL,
    publicado   BOOLEAN NOT NULL DEFAULT FALSE,
    creado_en   TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT section_slug_format CHECK (slug ~ '^[a-z0-9-]+$')
);

CREATE TABLE work (
    id                UUID PRIMARY KEY,
    section_id        UUID NOT NULL REFERENCES section (id) ON DELETE CASCADE,
    minio_object_key  TEXT NOT NULL,
    orden             INTEGER NOT NULL,
    publicado         BOOLEAN NOT NULL DEFAULT FALSE,
    creado_en         TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_work_section_orden ON work (section_id, orden);

-- Seed de demo: secciones publicadas sin fotos (los objetos de MinIO se
-- suben aparte, ver docs/seed-portfolio-publico.md). Permite demostrar
-- home + navegacion de secciones antes de que exista el ABM (Epic 4).
INSERT INTO section (id, nombre, slug, orden, publicado) VALUES
    (gen_random_uuid(), 'Bodas', 'bodas', 0, TRUE),
    (gen_random_uuid(), 'Retratos', 'retratos', 1, TRUE),
    (gen_random_uuid(), 'Books', 'books', 2, TRUE),
    (gen_random_uuid(), 'Eventos', 'eventos', 3, TRUE);
