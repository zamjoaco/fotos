package com.fotos.sections;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SectionRepository extends JpaRepository<Section, UUID> {

    List<Section> findByPublicadoTrueOrderByOrdenAsc();

    Optional<Section> findBySlugAndPublicadoTrue(String slug);
}
