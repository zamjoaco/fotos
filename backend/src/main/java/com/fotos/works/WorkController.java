package com.fotos.works;

import com.fotos.common.NotFoundException;
import com.fotos.sections.SectionRepository;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sections/{slug}/works")
public class WorkController {

    private static final int DEFAULT_SIZE = 24;
    private static final int MAX_SIZE = 100;

    private final SectionRepository sectionRepository;
    private final WorkRepository workRepository;
    private final MinioPresignedUrlService presignedUrlService;

    public WorkController(
            SectionRepository sectionRepository,
            WorkRepository workRepository,
            MinioPresignedUrlService presignedUrlService) {
        this.sectionRepository = sectionRepository;
        this.workRepository = workRepository;
        this.presignedUrlService = presignedUrlService;
    }

    @GetMapping
    public WorkPageResponse galeria(
            @PathVariable String slug,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + DEFAULT_SIZE) int size) {
        sectionRepository.findBySlugAndPublicadoTrue(slug).orElseThrow(NotFoundException::new);

        int clampedSize = Math.min(Math.max(size, 1), MAX_SIZE);
        int clampedPage = Math.max(page, 0);
        Page<Work> result = workRepository.findBySectionSlugAndPublicadoTrueAndSectionPublicadoTrueOrderByOrdenAsc(
                slug, PageRequest.of(clampedPage, clampedSize));

        return new WorkPageResponse(
                result.getContent().stream().map(this::aRespuestaDeGaleria).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages());
    }

    private WorkGalleryItemResponse aRespuestaDeGaleria(Work work) {
        MinioPresignedUrlService.PresignedUrl presignedUrl = presignedUrlService.generar(work.getMinioObjectKey());
        return new WorkGalleryItemResponse(
                work.getId().toString(), presignedUrl.url(), presignedUrl.expiraEn(), work.getOrden());
    }

    @GetMapping("/{workId}")
    public WorkDetailResponse detalle(@PathVariable String slug, @PathVariable UUID workId) {
        Work work = workRepository.findByIdAndSectionSlugAndPublicadoTrueAndSectionPublicadoTrue(workId, slug)
                .orElseThrow(NotFoundException::new);

        String anteriorId = workRepository
                .findFirstBySectionSlugAndPublicadoTrueAndSectionPublicadoTrueAndOrdenLessThanOrderByOrdenDesc(
                        slug, work.getOrden())
                .map(w -> w.getId().toString())
                .orElse(null);
        String siguienteId = workRepository
                .findFirstBySectionSlugAndPublicadoTrueAndSectionPublicadoTrueAndOrdenGreaterThanOrderByOrdenAsc(
                        slug, work.getOrden())
                .map(w -> w.getId().toString())
                .orElse(null);

        MinioPresignedUrlService.PresignedUrl presignedUrl = presignedUrlService.generar(work.getMinioObjectKey());
        return new WorkDetailResponse(
                work.getId().toString(),
                presignedUrl.url(),
                presignedUrl.expiraEn(),
                work.getOrden(),
                anteriorId,
                siguienteId);
    }
}
