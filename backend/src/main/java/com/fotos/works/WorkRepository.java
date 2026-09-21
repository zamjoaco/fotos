package com.fotos.works;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WorkRepository extends JpaRepository<Work, UUID> {

    long countBySectionSlugAndPublicadoTrueAndSectionPublicadoTrue(String slug);

    // Desempate por creadoEn (data-model.md): sin esto, dos Work con el mismo
    // orden hacen que el corte de pagina (offset) sea no determinista.
    Page<Work> findBySectionSlugAndPublicadoTrueAndSectionPublicadoTrueOrderByOrdenAscCreadoEnAsc(
            String slug, Pageable pageable);

    Optional<Work> findByIdAndSectionSlugAndPublicadoTrueAndSectionPublicadoTrue(
            UUID id, String slug);

    // Comparacion compuesta (orden, creadoEn) en vez de solo "orden < :orden":
    // con empates de orden, un LessThan/GreaterThan estricto salteaba al
    // hermano del mismo orden en la navegacion anterior/siguiente.
    @Query("""
            SELECT w FROM Work w
            WHERE w.section.slug = :slug AND w.publicado = true AND w.section.publicado = true
              AND (w.orden < :orden OR (w.orden = :orden AND w.creadoEn < :creadoEn))
            ORDER BY w.orden DESC, w.creadoEn DESC
            """)
    List<Work> buscarAnteriores(
            @Param("slug") String slug, @Param("orden") int orden, @Param("creadoEn") Instant creadoEn, Pageable pageable);

    @Query("""
            SELECT w FROM Work w
            WHERE w.section.slug = :slug AND w.publicado = true AND w.section.publicado = true
              AND (w.orden > :orden OR (w.orden = :orden AND w.creadoEn > :creadoEn))
            ORDER BY w.orden ASC, w.creadoEn ASC
            """)
    List<Work> buscarSiguientes(
            @Param("slug") String slug, @Param("orden") int orden, @Param("creadoEn") Instant creadoEn, Pageable pageable);
}
