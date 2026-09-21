package com.fotos.works;

import java.util.List;

public record WorkPageResponse(
        List<WorkGalleryItemResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages) {
}
