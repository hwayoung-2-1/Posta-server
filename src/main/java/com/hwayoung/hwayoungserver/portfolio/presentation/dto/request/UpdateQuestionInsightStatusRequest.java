package com.hwayoung.hwayoungserver.portfolio.presentation.dto.request;

import com.hwayoung.hwayoungserver.portfolio.domain.type.QuestionClusterStatus;

public record UpdateQuestionInsightStatusRequest(
        QuestionClusterStatus status
) {
}
