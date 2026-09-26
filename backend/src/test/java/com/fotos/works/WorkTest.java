package com.fotos.works;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fotos.sections.Section;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class WorkTest {

    private final Section section = new Section(UUID.randomUUID(), "Bodas", "bodas", 0);

    @Test
    void seCreaDespublicadaPorDefecto() {
        Work work = new Work(UUID.randomUUID(), section, "bodas/foto-01.jpg", 0);

        assertThat(work.isPublicado()).isFalse();
    }

    @Test
    void publicarYDespublicarCambianElEstado() {
        Work work = new Work(UUID.randomUUID(), section, "bodas/foto-01.jpg", 0);

        work.publicar();
        assertThat(work.isPublicado()).isTrue();

        work.despublicar();
        assertThat(work.isPublicado()).isFalse();
    }

    @Test
    void rechazaMinioObjectKeyVacio() {
        assertThatThrownBy(() -> new Work(UUID.randomUUID(), section, "", 0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rechazaMinioObjectKeyNulo() {
        assertThatThrownBy(() -> new Work(UUID.randomUUID(), section, null, 0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rechazaSectionNula() {
        assertThatThrownBy(() -> new Work(UUID.randomUUID(), null, "bodas/foto-01.jpg", 0))
                .isInstanceOf(NullPointerException.class);
    }
}
