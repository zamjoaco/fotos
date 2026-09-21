package com.fotos.sections;

import com.fotos.common.NotFoundException;
import com.fotos.works.WorkRepository;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sections")
public class SectionController {

    private final SectionRepository sectionRepository;
    private final WorkRepository workRepository;

    public SectionController(SectionRepository sectionRepository, WorkRepository workRepository) {
        this.sectionRepository = sectionRepository;
        this.workRepository = workRepository;
    }

    @GetMapping
    public List<SectionSummaryResponse> listar() {
        return sectionRepository.findByPublicadoTrueOrderByOrdenAsc().stream()
                .map(section -> new SectionSummaryResponse(
                        section.getSlug(),
                        section.getNombre(),
                        section.getOrden(),
                        workRepository.countBySectionSlugAndPublicadoTrueAndSectionPublicadoTrue(section.getSlug())))
                .toList();
    }

    @GetMapping("/{slug}")
    public SectionDetailResponse detalle(@PathVariable String slug) {
        Section section = sectionRepository.findBySlugAndPublicadoTrue(slug)
                .orElseThrow(NotFoundException::new);
        return new SectionDetailResponse(section.getSlug(), section.getNombre(), section.getOrden());
    }
}
