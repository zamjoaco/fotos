package com.fotos.sections;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SectionRepository extends JpaRepository<Section, UUID> {

    // Desempate por creadoEn (data-model.md): dos Section con el mismo orden
    // no deben quedar en orden no determinista entre requests.
    List<Section> findByPublicadoTrueOrderByOrdenAscCreadoEnAsc();

    Optional<Section> findBySlugAndPublicadoTrue(String slug);
}
