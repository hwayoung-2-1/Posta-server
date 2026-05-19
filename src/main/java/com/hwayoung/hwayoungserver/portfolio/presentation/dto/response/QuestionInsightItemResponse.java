package com.hwayoung.hwayoungserver.portfolio.presentation.dto.response;

import com.hwayoung.hwayoungserver.portfolio.domain.model.QuestionCluster;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record QuestionInsightItemResponse(
        UUID clusterId,
        String title,
        String summary,
        String category,
        int questionCount,
        String status,
        List<String> sampleQuestions,
        LocalDateTime firstAskedAt,
        LocalDateTime lastAskedAt
) {
    public static QuestionInsightItemResponse of(QuestionCluster cluster, List<String> sampleQuestions) {
        return new QuestionInsightItemResponse(
                cluster.getId(),
                cluster.getTitle(),
                cluster.getSummary(),
                cluster.getCategory().name(),
                cluster.getQuestionCount(),
                cluster.getStatus().name(),
                sampleQuestions,
                cluster.getFirstAskedAt(),
                cluster.getLastAskedAt()
        );
    }
}
