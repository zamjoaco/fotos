# Seed de datos: Portfolio publico (Epic 1)

La migracion `V2__portfolio_publico.sql` carga 4 secciones de demo
publicadas (`bodas`, `retratos`, `books`, `eventos`) sin fotos, para poder
probar la home y la navegacion antes de que exista el ABM real (Epic 4).

Para ver la galeria de una seccion con fotos reales hay que subir objetos
al bucket de MinIO y despues insertar las filas de `work` a mano (no hay
ABM todavia):

## 1. Subir imagenes a MinIO

Con el stack levantado (`docker compose up`), entrar a la consola de MinIO
en `http://localhost:9001` (usuario/clave: `MINIO_ROOT_USER` /
`MINIO_ROOT_PASSWORD` del `.env`) y subir imagenes al bucket
`fotos-works` (o el que tengas configurado en `MINIO_BUCKET`), por ejemplo
bajo la key `bodas/foto-01.jpg`.

## 2. Insertar la fila de `work` correspondiente

```sql
INSERT INTO work (id, section_id, minio_object_key, orden, publicado)
SELECT gen_random_uuid(), s.id, 'bodas/foto-01.jpg', 0, TRUE
FROM section s WHERE s.slug = 'bodas';
```

Repetir por cada objeto subido, **incrementando `orden`** (no repetir el
mismo valor entre fotos de la misma seccion: el orden final se desempata
por `creadoEn`, asi que si insertas varias filas con el mismo `orden` en
la misma transaccion/segundo, el orden de aparicion puede no ser el que
esperas). La galeria de la seccion (`GET /sections/bodas/works`) los va a
listar apenas esten `publicado = true`.
