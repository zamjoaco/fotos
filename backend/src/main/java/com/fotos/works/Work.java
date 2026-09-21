package com.fotos.works;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import com.fotos.sections.Section;

/**
 * Una foto publicada dentro de una Section. Entidad rica: valida su propia
 * referencia al objeto de MinIO y controla sus transiciones de publicacion.
 */
@Entity
@Table(name = "work")
public class Work {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "section_id", nullable = false)
    private Section section;

    @Column(name = "minio_object_key", nullable = false)
    private String minioObjectKey;

    @Column(nullable = false)
    private int orden;

    @Column(nullable = false)
    private boolean publicado;

    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn;

    protected Work() {
        // JPA
    }

    public Work(UUID id, Section section, String minioObjectKey, int orden) {
        this.id = Objects.requireNonNull(id, "id no puede ser null");
        this.section = Objects.requireNonNull(section, "section no puede ser null");
        this.minioObjectKey = requireNonBlank(minioObjectKey);
        this.orden = orden;
        this.publicado = false;
        this.creadoEn = Instant.now();
    }

    private static String requireNonBlank(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("minioObjectKey no puede estar vacio");
        }
        return value;
    }

    public void publicar() {
        this.publicado = true;
    }

    public void despublicar() {
        this.publicado = false;
    }

    public UUID getId() {
        return id;
    }

    public Section getSection() {
        return section;
    }

    public String getMinioObjectKey() {
        return minioObjectKey;
    }

    public int getOrden() {
        return orden;
    }

    public boolean isPublicado() {
        return publicado;
    }

    public Instant getCreadoEn() {
        return creadoEn;
    }
}
