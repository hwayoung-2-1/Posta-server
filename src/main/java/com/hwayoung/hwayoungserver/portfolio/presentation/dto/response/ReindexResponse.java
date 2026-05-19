package com.hwayoung.hwayoungserver.portfolio.presentation.dto.response;

import com.hwayoung.hwayoungserver.portfolio.domain.type.JobStatus;

import java.util.UUID;

public record ReindexResponse(
        UUID portfolioId,
        UUID jobId,
        JobStatus status
) {
}
