package com.hwayoung.hwayoungserver.portfolio.presentation.dto.response;

import com.hwayoung.hwayoungserver.portfolio.domain.model.QuestionCluster;

import java.util.UUID;

public record UpdateQuestionInsightStatusResponse(
        UUID clusterId,
        String status
) {
    public static UpdateQuestionInsightStatusResponse from(QuestionCluster cluster) {
        return new UpdateQuestionInsightStatusResponse(cluster.getId(), cluster.getStatus().name());
    }
}
