package com.hwayoung.hwayoungserver.portfolio.presentation.dto.response;

import com.hwayoung.hwayoungserver.portfolio.domain.model.QuestionCluster;

import java.util.List;
import java.util.UUID;

public record QuestionInsightDetailResponse(
        UUID clusterId,
        String title,
        String summary,
        String category,
        int questionCount,
        String status,
        List<QuestionInsightQuestionResponse> questions,
        String recommendedAction
) {
    public static QuestionInsightDetailResponse of(
            QuestionCluster cluster,
            List<QuestionInsightQuestionResponse> questions,
            String recommendedAction
    ) {
        return new QuestionInsightDetailResponse(
                cluster.getId(),
                cluster.getTitle(),
                cluster.getSummary(),
                cluster.getCategory().name(),
                cluster.getQuestionCount(),
                cluster.getStatus().name(),
                questions,
                recommendedAction
        );
    }
}
