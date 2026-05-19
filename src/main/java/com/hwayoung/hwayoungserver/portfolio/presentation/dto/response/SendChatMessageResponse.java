package com.hwayoung.hwayoungserver.portfolio.presentation.dto.response;

import java.util.List;
import java.util.UUID;

public record SendChatMessageResponse(
        UUID questionMessageId,
        UUID answerMessageId,
        String answer,
        boolean answerable,
        List<ChatSourceResponse> sources,
        String questionAnalysisStatus
) {
}
