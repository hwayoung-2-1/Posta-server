package com.hwayoung.hwayoungserver.portfolio.presentation.dto.response;

import com.hwayoung.hwayoungserver.portfolio.domain.model.ChatMessage;
import com.hwayoung.hwayoungserver.portfolio.domain.model.ChatMessageSource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ChatMessageItemResponse(
        UUID messageId,
        String role,
        String content,
        boolean answerable,
        List<ChatSourceResponse> sources,
        LocalDateTime createdAt
) {
    public static ChatMessageItemResponse of(ChatMessage message, List<ChatMessageSource> sources) {
        return new ChatMessageItemResponse(
                message.getId(),
                message.getRole().name(),
                message.getContent(),
                message.isAnswerable(),
                sources.stream().map(ChatSourceResponse::from).toList(),
                message.getCreatedAt()
        );
    }
}
