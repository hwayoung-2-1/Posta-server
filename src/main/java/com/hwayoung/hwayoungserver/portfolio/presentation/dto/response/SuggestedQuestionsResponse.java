package com.hwayoung.hwayoungserver.portfolio.presentation.dto.response;

import java.util.List;

public record SuggestedQuestionsResponse(
        List<SuggestedQuestionItemResponse> questions
) {
}
