package com.fotos.works;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkRepository extends JpaRepository<Work, UUID> {

    long countBySectionSlugAndPublicadoTrueAndSectionPublicadoTrue(String slug);

    Page<Work> findBySectionSlugAndPublicadoTrueAndSectionPublicadoTrueOrderByOrdenAsc(
            String slug, Pageable pageable);

    Optional<Work> findByIdAndSectionSlugAndPublicadoTrueAndSectionPublicadoTrue(
            UUID id, String slug);

    Optional<Work> findFirstBySectionSlugAndPublicadoTrueAndSectionPublicadoTrueAndOrdenLessThanOrderByOrdenDesc(
            String slug, int orden);

    Optional<Work> findFirstBySectionSlugAndPublicadoTrueAndSectionPublicadoTrueAndOrdenGreaterThanOrderByOrdenAsc(
            String slug, int orden);
}
