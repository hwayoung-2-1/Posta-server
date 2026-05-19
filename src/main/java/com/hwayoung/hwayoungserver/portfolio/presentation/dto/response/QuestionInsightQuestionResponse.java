package com.hwayoung.hwayoungserver.portfolio.presentation.dto.response;

import com.hwayoung.hwayoungserver.portfolio.domain.model.ChatMessage;

import java.time.LocalDateTime;
import java.util.UUID;

public record QuestionInsightQuestionResponse(
        UUID messageId,
        String content,
        LocalDateTime askedAt
) {
    public static QuestionInsightQuestionResponse from(ChatMessage message) {
        return new QuestionInsightQuestionResponse(message.getId(), message.getContent(), message.getCreatedAt());
    }
}
