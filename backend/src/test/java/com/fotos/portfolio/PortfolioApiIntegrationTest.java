package com.fotos.portfolio;

import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.fotos.sections.Section;
import com.fotos.sections.SectionRepository;
import com.fotos.works.MinioPresignedUrlService;
import com.fotos.works.Work;
import com.fotos.works.WorkRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Integracion end-to-end de los endpoints de portfolio publico (feature
 * 001-portfolio-publico) contra un Postgres real via Testcontainers, mismo
 * patron que FotosBackendApplicationTests (Epic 0). Requiere Docker
 * disponible (por eso el CI de Epic 0 sigue en -DskipTests).
 */
@Testcontainers
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class PortfolioApiIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private TestRestTemplate rest;

    @Autowired
    private SectionRepository sectionRepository;

    @Autowired
    private WorkRepository workRepository;

    @MockitoBean
    private MinioPresignedUrlService presignedUrlService;

    @BeforeEach
    void mockearUrlsPresignadas() {
        when(presignedUrlService.generar(anyString())).thenAnswer(invocation -> {
            String objectKey = invocation.getArgument(0, String.class);
            return new MinioPresignedUrlService.PresignedUrl(
                    "https://minio.test/" + objectKey, Instant.now().plusSeconds(900));
        });
    }

    @AfterEach
    void limpiar() {
        workRepository.deleteAll();
        sectionRepository.deleteAll();
    }

    @Test
    void listaSoloSeccionesPublicadasOrdenadas() {
        crearSeccionPublicada("Bodas", "bodas", 1);
        crearSeccionPublicada("Retratos", "retratos", 0);
        crearSeccion("Eventos (borrador)", "eventos", 2); // no publicada

        ResponseEntity<SectionSummaryResponse[]> response =
                rest.getForEntity("/sections", SectionSummaryResponse[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).extracting(SectionSummaryResponse::slug)
                .containsExactly("retratos", "bodas");
    }

    @Test
    void listaVaciaCuandoNoHaySeccionesPublicadas() {
        ResponseEntity<SectionSummaryResponse[]> response =
                rest.getForEntity("/sections", SectionSummaryResponse[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void detalleDeSeccionPublicadaDevuelve200() {
        crearSeccionPublicada("Bodas", "bodas", 0);

        ResponseEntity<String> response = rest.getForEntity("/sections/bodas", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void detalleDeSeccionInexistenteDevuelve404() {
        ResponseEntity<String> response = rest.getForEntity("/sections/no-existe", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void detalleDeSeccionNoPublicadaDevuelve404IgualQueInexistente() {
        crearSeccion("Eventos (borrador)", "eventos", 0);

        ResponseEntity<String> response = rest.getForEntity("/sections/eventos", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void worksDeSeccionSinFotosPublicadasDevuelveListaVacia() {
        crearSeccionPublicada("Bodas", "bodas", 0);

        ResponseEntity<String> response = rest.getForEntity("/sections/bodas/works", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"content\":[]");
    }

    @Test
    void worksDeSeccionInexistenteDevuelve404() {
        ResponseEntity<String> response = rest.getForEntity("/sections/no-existe/works", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void worksDeSeccionPublicadaDevuelveSoloLosPublicadosPaginadosYOrdenados() {
        Section bodas = crearSeccionPublicada("Bodas", "bodas", 0);
        crearWorkPublicado(bodas, "bodas/1.jpg", 0);
        crearWorkPublicado(bodas, "bodas/2.jpg", 1);
        crearWorkPublicado(bodas, "bodas/3.jpg", 2);
        crearWork(bodas, "bodas/borrador.jpg", 3); // no publicado, no debe aparecer

        ResponseEntity<WorkPageResponse> response =
                rest.getForEntity("/sections/bodas/works?size=2", WorkPageResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().content()).hasSize(2);
        assertThat(response.getBody().content().get(0).imageUrl()).contains("bodas/1.jpg");
        assertThat(response.getBody().totalElements()).isEqualTo(3);
        assertThat(response.getBody().totalPages()).isEqualTo(2);
    }

    private Section crearSeccionPublicada(String nombre, String slug, int orden) {
        Section section = crearSeccion(nombre, slug, orden);
        section.publicar();
        return sectionRepository.save(section);
    }

    private Section crearSeccion(String nombre, String slug, int orden) {
        return sectionRepository.save(new Section(UUID.randomUUID(), nombre, slug, orden));
    }

    @Test
    void detalleDeWorkPublicadoDevuelveAnteriorYSiguiente() {
        Section bodas = crearSeccionPublicada("Bodas", "bodas", 0);
        Work primero = crearWorkPublicado(bodas, "bodas/1.jpg", 0);
        Work segundo = crearWorkPublicado(bodas, "bodas/2.jpg", 1);
        Work tercero = crearWorkPublicado(bodas, "bodas/3.jpg", 2);

        ResponseEntity<WorkDetailResponse> response = rest.getForEntity(
                "/sections/bodas/works/" + segundo.getId(), WorkDetailResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().anteriorId()).isEqualTo(primero.getId().toString());
        assertThat(response.getBody().siguienteId()).isEqualTo(tercero.getId().toString());
    }

    @Test
    void detalleDePrimerYUltimoWorkTieneAnteriorOSiguienteNulo() {
        Section bodas = crearSeccionPublicada("Bodas", "bodas", 0);
        Work primero = crearWorkPublicado(bodas, "bodas/1.jpg", 0);
        Work ultimo = crearWorkPublicado(bodas, "bodas/2.jpg", 1);

        ResponseEntity<WorkDetailResponse> primerResponse = rest.getForEntity(
                "/sections/bodas/works/" + primero.getId(), WorkDetailResponse.class);
        ResponseEntity<WorkDetailResponse> ultimoResponse = rest.getForEntity(
                "/sections/bodas/works/" + ultimo.getId(), WorkDetailResponse.class);

        assertThat(primerResponse.getBody().anteriorId()).isNull();
        assertThat(ultimoResponse.getBody().siguienteId()).isNull();
    }

    @Test
    void detalleDeWorkInexistenteODeOtraSeccionDevuelve404() {
        Section bodas = crearSeccionPublicada("Bodas", "bodas", 0);
        Section retratos = crearSeccionPublicada("Retratos", "retratos", 1);
        Work workDeRetratos = crearWorkPublicado(retratos, "retratos/1.jpg", 0);

        ResponseEntity<String> inexistente =
                rest.getForEntity("/sections/bodas/works/" + UUID.randomUUID(), String.class);
        ResponseEntity<String> otraSeccion =
                rest.getForEntity("/sections/bodas/works/" + workDeRetratos.getId(), String.class);

        assertThat(inexistente.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(otraSeccion.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    private Work crearWorkPublicado(Section section, String objectKey, int orden) {
        Work work = crearWork(section, objectKey, orden);
        work.publicar();
        return workRepository.save(work);
    }

    private Work crearWork(Section section, String objectKey, int orden) {
        return workRepository.save(new Work(UUID.randomUUID(), section, objectKey, orden));
    }

    private record SectionSummaryResponse(String slug, String nombre, int orden, long cantidadWorks) {
    }

    private record WorkGalleryItemResponse(String id, String imageUrl, Instant imageUrlExpiraEn, int orden) {
    }

    private record WorkPageResponse(
            List<WorkGalleryItemResponse> content, int page, int size, long totalElements, int totalPages) {
    }

    private record WorkDetailResponse(
            String id, String imageUrl, Instant imageUrlExpiraEn, int orden, String anteriorId, String siguienteId) {
    }
}
