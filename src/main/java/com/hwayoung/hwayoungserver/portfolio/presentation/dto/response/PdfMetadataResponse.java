package com.hwayoung.hwayoungserver.portfolio.presentation.dto.response;

import com.hwayoung.hwayoungserver.portfolio.domain.model.Portfolio;

public record PdfMetadataResponse(
        String originalFilename,
        String contentType,
        long size
) {
    public static PdfMetadataResponse from(Portfolio portfolio) {
        return new PdfMetadataResponse(
                portfolio.getPdfOriginalFilename(),
                portfolio.getPdfContentType(),
                portfolio.getPdfSize()
        );
    }
}
