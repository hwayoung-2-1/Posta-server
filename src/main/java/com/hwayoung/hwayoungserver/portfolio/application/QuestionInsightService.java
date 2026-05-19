package com.hwayoung.hwayoungserver.portfolio.application;

import com.hwayoung.hwayoungserver.common.ApiException;
import com.hwayoung.hwayoungserver.portfolio.domain.model.ChatMessage;
import com.hwayoung.hwayoungserver.portfolio.domain.model.Portfolio;
import com.hwayoung.hwayoungserver.portfolio.domain.model.QuestionCluster;
import com.hwayoung.hwayoungserver.portfolio.domain.model.QuestionClusterItem;
import com.hwayoung.hwayoungserver.portfolio.domain.type.ChatMessageRole;
import com.hwayoung.hwayoungserver.portfolio.domain.type.JobStatus;
import com.hwayoung.hwayoungserver.portfolio.domain.type.JobType;
import com.hwayoung.hwayoungserver.portfolio.domain.type.QuestionCategory;
import com.hwayoung.hwayoungserver.portfolio.domain.type.QuestionClusterStatus;
import com.hwayoung.hwayoungserver.portfolio.domain.model.PortfolioIndexJob;
import com.hwayoung.hwayoungserver.portfolio.persistence.ChatMessageRepository;
import com.hwayoung.hwayoungserver.portfolio.persistence.PortfolioIndexJobRepository;
import com.hwayoung.hwayoungserver.portfolio.persistence.QuestionClusterItemRepository;
import com.hwayoung.hwayoungserver.portfolio.persistence.QuestionClusterRepository;
import com.hwayoung.hwayoungserver.portfolio.presentation.dto.response.QuestionInsightDetailResponse;
import com.hwayoung.hwayoungserver.portfolio.presentation.dto.response.QuestionInsightItemResponse;
import com.hwayoung.hwayoungserver.portfolio.presentation.dto.response.QuestionInsightQuestionResponse;
import com.hwayoung.hwayoungserver.portfolio.presentation.dto.response.QuestionInsightsResponse;
import com.hwayoung.hwayoungserver.portfolio.presentation.dto.response.ReindexResponse;
import com.hwayoung.hwayoungserver.portfolio.presentation.dto.response.UpdateQuestionInsightStatusResponse;
import com.hwayoung.hwayoungserver.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QuestionInsightService {
    private final PortfolioPdfService portfolioPdfService;
    private final QuestionClusterRepository questionClusterRepository;
    private final QuestionClusterItemRepository questionClusterItemRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final PortfolioIndexJobRepository portfolioIndexJobRepository;

    @Transactional
    public void recordUnansweredQuestion(Portfolio portfolio, ChatMessage questionMessage, Double similarityScore) {
        QuestionCategory category = categorize(questionMessage.getContent());
        QuestionCluster cluster = questionClusterRepository
                .findFirstByPortfolioAndCategoryAndStatusOrderByLastAskedAtDesc(
                        portfolio,
                        category,
                        QuestionClusterStatus.OPEN
                )
                .orElseGet(() -> questionClusterRepository.save(new QuestionCluster(
                        portfolio,
                        category,
                        title(category),
                        summary(category),
                        askedAt(questionMessage)
                )));

        cluster.recordQuestion(askedAt(questionMessage));
        questionClusterItemRepository.save(new QuestionClusterItem(cluster, questionMessage, similarityScore));
    }

    @Transactional(readOnly = true)
    public QuestionInsightsResponse list(User owner, UUID portfolioId, QuestionClusterStatus status, int page, int size) {
        Portfolio portfolio = portfolioPdfService.getOwnedPortfolio(owner, portfolioId);
        Pageable pageable = PageRequest.of(Math.max(page, 0), size <= 0 ? 20 : Math.min(size, 100));
        Page<QuestionCluster> clusters = status == null
                ? questionClusterRepository.findByPortfolioOrderByLastAskedAtDesc(portfolio, pageable)
                : questionClusterRepository.findByPortfolioAndStatusOrderByLastAskedAtDesc(portfolio, status, pageable);

        List<QuestionInsightItemResponse> content = clusters.getContent().stream()
                .map(cluster -> QuestionInsightItemResponse.of(cluster, sampleQuestions(cluster)))
                .toList();

        return new QuestionInsightsResponse(
                portfolio.getId(),
                chatMessageRepository.countByChatSessionPortfolioAndRoleAndAnalyzableTrue(portfolio, ChatMessageRole.USER),
                content,
                clusters.getNumber(),
                clusters.getSize(),
                clusters.getTotalElements()
        );
    }

    @Transactional(readOnly = true)
    public QuestionInsightDetailResponse detail(User owner, UUID portfolioId, UUID clusterId) {
        Portfolio portfolio = portfolioPdfService.getOwnedPortfolio(owner, portfolioId);
        QuestionCluster cluster = questionClusterRepository.findByPortfolioAndId(portfolio, clusterId)
                .orElseThrow(() -> ApiException.notFound("QUESTION_INSIGHT_NOT_FOUND", "질문 인사이트를 찾을 수 없습니다."));
        List<QuestionInsightQuestionResponse> questions = questionClusterItemRepository.findByQuestionClusterOrderByCreatedAtDesc(cluster)
                .stream()
                .map(QuestionClusterItem::getChatMessage)
                .map(QuestionInsightQuestionResponse::from)
                .toList();
        return QuestionInsightDetailResponse.of(cluster, questions, recommendedAction(cluster.getCategory()));
    }

    @Transactional
    public UpdateQuestionInsightStatusResponse updateStatus(
            User owner,
            UUID portfolioId,
            UUID clusterId,
            QuestionClusterStatus status
    ) {
        if (status == null) {
            throw ApiException.badRequest("status는 필수입니다.");
        }
        Portfolio portfolio = portfolioPdfService.getOwnedPortfolio(owner, portfolioId);
        QuestionCluster cluster = questionClusterRepository.findByPortfolioAndId(portfolio, clusterId)
                .orElseThrow(() -> ApiException.notFound("QUESTION_INSIGHT_NOT_FOUND", "질문 인사이트를 찾을 수 없습니다."));
        cluster.updateStatus(status);
        return UpdateQuestionInsightStatusResponse.from(cluster);
    }

    @Transactional
    public ReindexResponse rebuild(User owner, UUID portfolioId) {
        Portfolio portfolio = portfolioPdfService.getOwnedPortfolio(owner, portfolioId);
        PortfolioIndexJob job = portfolioIndexJobRepository.save(new PortfolioIndexJob(
                portfolio,
                JobType.QUESTION_INSIGHT_REBUILD,
                JobStatus.RUNNING
        ));
        questionClusterItemRepository.deleteByQuestionClusterPortfolio(portfolio);
        questionClusterRepository.deleteByPortfolio(portfolio);
        chatMessageRepository.findByChatSessionPortfolioAndRoleAndAnalyzableTrueOrderByCreatedAtAsc(
                        portfolio,
                        ChatMessageRole.USER
                )
                .forEach(message -> recordUnansweredQuestion(portfolio, message, null));
        job.markDone();
        return new ReindexResponse(portfolio.getId(), job.getId(), job.getStatus());
    }

    private List<String> sampleQuestions(QuestionCluster cluster) {
        return questionClusterItemRepository.findByQuestionClusterOrderByCreatedAtDesc(cluster).stream()
                .limit(3)
                .map(QuestionClusterItem::getChatMessage)
                .map(ChatMessage::getContent)
                .toList();
    }

    private LocalDateTime askedAt(ChatMessage questionMessage) {
        return questionMessage.getCreatedAt() == null ? LocalDateTime.now() : questionMessage.getCreatedAt();
    }

    private QuestionCategory categorize(String question) {
        String value = question == null ? "" : question.toLowerCase();
        if (containsAny(value, "욕", "바보", "멍청", "hate", "stupid")) {
            return QuestionCategory.ABUSIVE;
        }
        if (containsAny(value, "역할", "기여", "담당", "맡", "role", "contribution")) {
            return QuestionCategory.PROJECT_ROLE;
        }
        if (containsAny(value, "기술", "스택", "라이브러리", "프레임워크", "tool", "stack")) {
            return QuestionCategory.TECH_STACK;
        }
        if (containsAny(value, "구현", "아키텍처", "api", "db", "database", "implementation")) {
            return QuestionCategory.IMPLEMENTATION_DETAIL;
        }
        if (containsAny(value, "디자인", "ux", "ui", "figma", "decision")) {
            return QuestionCategory.DESIGN_DECISION;
        }
        if (containsAny(value, "협업", "팀", "소통", "collaboration")) {
            return QuestionCategory.COLLABORATION;
        }
        if (containsAny(value, "성과", "결과", "지표", "impact", "result")) {
            return QuestionCategory.ACHIEVEMENT;
        }
        if (containsAny(value, "경력", "이력", "학력", "career")) {
            return QuestionCategory.CAREER;
        }
        return QuestionCategory.UNCLEAR_PORTFOLIO_CONTENT;
    }

    private boolean containsAny(String value, String... keywords) {
        for (String keyword : keywords) {
            if (value.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private String title(QuestionCategory category) {
        return switch (category) {
            case PROJECT_ROLE -> "프로젝트 역할과 기여도";
            case TECH_STACK -> "기술 스택과 도구";
            case CAREER -> "경력과 배경";
            case DESIGN_DECISION -> "디자인 의사결정";
            case IMPLEMENTATION_DETAIL -> "구현 세부사항";
            case COLLABORATION -> "협업 방식";
            case ACHIEVEMENT -> "성과와 결과";
            case IRRELEVANT -> "포트폴리오와 무관한 질문";
            case ABUSIVE -> "부적절한 질문";
            case UNCLEAR_PORTFOLIO_CONTENT -> "자료가 부족한 질문";
        };
    }

    private String summary(QuestionCategory category) {
        return switch (category) {
            case PROJECT_ROLE -> "조회자가 작성자의 실제 역할과 기여도를 확인하지 못했습니다.";
            case TECH_STACK -> "조회자가 사용 기술, 도구, 선택 이유를 확인하지 못했습니다.";
            case CAREER -> "조회자가 작성자의 경력과 배경 정보를 확인하지 못했습니다.";
            case DESIGN_DECISION -> "조회자가 디자인 판단 근거를 확인하지 못했습니다.";
            case IMPLEMENTATION_DETAIL -> "조회자가 구현 방식과 기술적 의사결정을 확인하지 못했습니다.";
            case COLLABORATION -> "조회자가 팀 협업 방식과 커뮤니케이션 정보를 확인하지 못했습니다.";
            case ACHIEVEMENT -> "조회자가 결과 지표와 성과를 확인하지 못했습니다.";
            case IRRELEVANT -> "포트폴리오 범위를 벗어난 질문이 반복되었습니다.";
            case ABUSIVE -> "부적절한 질문이 감지되었습니다.";
            case UNCLEAR_PORTFOLIO_CONTENT -> "현재 포트폴리오 설명만으로 답변하기 어려운 질문이 발생했습니다.";
        };
    }

    private String recommendedAction(QuestionCategory category) {
        return switch (category) {
            case PROJECT_ROLE -> "관련 페이지의 작성자 설명에 본인 역할, 담당 범위, 기여 결과를 구체적으로 추가하세요.";
            case TECH_STACK -> "사용 기술과 도구를 나열하는 데서 끝내지 말고 선택 이유와 실제 사용 범위를 추가하세요.";
            case CAREER -> "소개나 요약 영역에 경력, 경험 기간, 지원 직무와 연결되는 배경을 보강하세요.";
            case DESIGN_DECISION -> "문제 정의, 대안 비교, 최종 디자인 선택 이유를 페이지 설명에 추가하세요.";
            case IMPLEMENTATION_DETAIL -> "아키텍처, API, DB, 성능 개선 등 구현 근거를 더 명확히 작성하세요.";
            case COLLABORATION -> "팀 규모, 협업 방식, 본인이 조율한 내용과 산출물을 추가하세요.";
            case ACHIEVEMENT -> "정량 지표, 사용자 반응, 개선 전후 비교 등 결과 중심 설명을 보강하세요.";
            case IRRELEVANT -> "반복되는 무관 질문은 무시 처리하거나 공개 범위와 안내 문구를 점검하세요.";
            case ABUSIVE -> "부적절한 질문은 무시 처리하고 필요하면 신고/차단 정책을 적용하세요.";
            case UNCLEAR_PORTFOLIO_CONTENT -> "반복 질문이 발생한 페이지에 조회자가 궁금해한 맥락을 직접 설명으로 추가하세요.";
        };
    }
}
