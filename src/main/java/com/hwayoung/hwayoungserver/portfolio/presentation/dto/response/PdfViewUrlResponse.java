package com.hwayoung.hwayoungserver.portfolio.presentation.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record PdfViewUrlResponse(
        UUID portfolioId,
        String url,
        LocalDateTime expiresAt
) {
}
