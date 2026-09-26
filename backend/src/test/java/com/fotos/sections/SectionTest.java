package com.fotos.sections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class SectionTest {

    @Test
    void seCreaDespublicadaPorDefecto() {
        Section section = new Section(UUID.randomUUID(), "Bodas", "bodas", 0);

        assertThat(section.isPublicado()).isFalse();
    }

    @Test
    void publicarYDespublicarCambianElEstado() {
        Section section = new Section(UUID.randomUUID(), "Bodas", "bodas", 0);

        section.publicar();
        assertThat(section.isPublicado()).isTrue();

        section.despublicar();
        assertThat(section.isPublicado()).isFalse();
    }

    @Test
    void rechazaSlugConMayusculas() {
        assertThatThrownBy(() -> new Section(UUID.randomUUID(), "Bodas", "Bodas", 0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rechazaSlugConEspacios() {
        assertThatThrownBy(() -> new Section(UUID.randomUUID(), "Book de 15", "book de 15", 0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rechazaSlugVacio() {
        assertThatThrownBy(() -> new Section(UUID.randomUUID(), "Bodas", "", 0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rechazaNombreVacio() {
        assertThatThrownBy(() -> new Section(UUID.randomUUID(), "  ", "bodas", 0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aceptaSlugConGuionesYNumeros() {
        Section section = new Section(UUID.randomUUID(), "Books 2026", "books-2026", 0);

        assertThat(section.getSlug()).isEqualTo("books-2026");
    }
}
