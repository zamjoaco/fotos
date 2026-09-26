package com.fotos.works;

import java.time.Instant;

public record WorkDetailResponse(
        String id,
        String imageUrl,
        Instant imageUrlExpiraEn,
        int orden,
        String anteriorId,
        String siguienteId) {
}
