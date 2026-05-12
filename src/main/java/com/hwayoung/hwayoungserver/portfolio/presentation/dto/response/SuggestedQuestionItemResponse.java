package com.hwayoung.hwayoungserver.portfolio.presentation.dto.response;

import com.hwayoung.hwayoungserver.portfolio.domain.model.SuggestedQuestion;

import java.util.UUID;

public record SuggestedQuestionItemResponse(
        UUID questionId,
        Integer pageNumber,
        String question
) {
    public static SuggestedQuestionItemResponse from(SuggestedQuestion question) {
        Integer pageNumber = question.getPortfolioPage() == null ? null : question.getPortfolioPage().getPageNumber();
        return new SuggestedQuestionItemResponse(question.getId(), pageNumber, question.getQuestion());
    }
}
