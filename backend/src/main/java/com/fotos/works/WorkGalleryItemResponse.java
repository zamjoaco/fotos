package com.fotos.works;

import java.time.Instant;

public record WorkGalleryItemResponse(String id, String imageUrl, Instant imageUrlExpiraEn, int orden) {
}
