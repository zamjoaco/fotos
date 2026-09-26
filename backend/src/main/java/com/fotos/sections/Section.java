package com.fotos.sections;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Categoria del portfolio (bodas, retratos, books, eventos, etc). Entidad
 * rica: valida su propio slug y controla sus transiciones de publicacion.
 */
@Entity
@Table(name = "section")
public class Section {

    private static final Pattern SLUG_FORMAT = Pattern.compile("^[a-z0-9-]+$");

    @Id
    private UUID id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(nullable = false)
    private int orden;

    @Column(nullable = false)
    private boolean publicado;

    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn;

    protected Section() {
        // JPA
    }

    public Section(UUID id, String nombre, String slug, int orden) {
        this.id = Objects.requireNonNull(id, "id no puede ser null");
        this.nombre = requireNonBlank(nombre, "nombre no puede estar vacio");
        this.slug = requireValidSlug(slug);
        this.orden = orden;
        this.publicado = false;
        this.creadoEn = Instant.now();
    }

    private static String requireNonBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    private static String requireValidSlug(String slug) {
        String value = requireNonBlank(slug, "slug no puede estar vacio");
        if (!SLUG_FORMAT.matcher(value).matches()) {
            throw new IllegalArgumentException(
                    "slug invalido: '" + value + "' (debe cumplir [a-z0-9-]+)");
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

    public String getNombre() {
        return nombre;
    }

    public String getSlug() {
        return slug;
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
