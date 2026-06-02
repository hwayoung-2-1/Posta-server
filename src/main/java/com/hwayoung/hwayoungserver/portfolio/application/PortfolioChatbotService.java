package com.hwayoung.hwayoungserver.portfolio.application;

import com.hwayoung.hwayoungserver.common.ApiException;
import com.hwayoung.hwayoungserver.portfolio.domain.model.ChatMessage;
import com.hwayoung.hwayoungserver.portfolio.domain.model.ChatMessageSource;
import com.hwayoung.hwayoungserver.portfolio.domain.model.ChatSession;
import com.hwayoung.hwayoungserver.portfolio.domain.model.PageOwnerNote;
import com.hwayoung.hwayoungserver.portfolio.domain.model.Portfolio;
import com.hwayoung.hwayoungserver.portfolio.domain.model.PortfolioContext;
import com.hwayoung.hwayoungserver.portfolio.domain.model.PortfolioPage;
import com.hwayoung.hwayoungserver.portfolio.domain.type.ChatMessageRole;
import com.hwayoung.hwayoungserver.portfolio.persistence.ChatMessageRepository;
import com.hwayoung.hwayoungserver.portfolio.persistence.ChatMessageSourceRepository;
import com.hwayoung.hwayoungserver.portfolio.persistence.ChatSessionRepository;
import com.hwayoung.hwayoungserver.portfolio.persistence.PageOwnerNoteRepository;
import com.hwayoung.hwayoungserver.portfolio.persistence.PortfolioContextRepository;
import com.hwayoung.hwayoungserver.portfolio.persistence.PortfolioPageRepository;
import com.hwayoung.hwayoungserver.portfolio.presentation.dto.response.ChatMessageItemResponse;
import com.hwayoung.hwayoungserver.portfolio.presentation.dto.response.ChatMessagesResponse;
import com.hwayoung.hwayoungserver.portfolio.presentation.dto.response.ChatSourceResponse;
import com.hwayoung.hwayoungserver.portfolio.presentation.dto.response.CreateChatSessionResponse;
import com.hwayoung.hwayoungserver.portfolio.presentation.dto.response.SendChatMessageResponse;
import com.hwayoung.hwayoungserver.taxonomy.domain.model.RoleEntity;
import com.hwayoung.hwayoungserver.taxonomy.domain.model.SkillEntity;
import com.hwayoung.hwayoungserver.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PortfolioChatbotService {
    private static final String UNANSWERABLE_MESSAGE = "제공된 포트폴리오 자료만으로는 답변하기 어렵습니다.";
    private static final String SOURCE_PDF_TEXT = "PDF_TEXT";
    private static final String SOURCE_OWNER_NOTE = "OWNER_NOTE";
    private static final String SOURCE_PAGE_CONTEXT = "PAGE_CONTEXT";

    private final PortfolioPdfService portfolioPdfService;
    private final ObjectProvider<ChatModel> chatModelProvider;
    private final ObjectProvider<VectorStore> vectorStoreProvider;
    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatMessageSourceRepository chatMessageSourceRepository;
    private final PortfolioPageRepository portfolioPageRepository;
    private final PageOwnerNoteRepository pageOwnerNoteRepository;
    private final PortfolioContextRepository portfolioContextRepository;
    private final QuestionInsightService questionInsightService;
    private final PortfolioAiProperties properties;

    @Transactional
    public CreateChatSessionResponse createSession(User viewer, UUID portfolioId) {
        Portfolio portfolio = portfolioPdfService.getAccessiblePortfolio(viewer, portfolioId);
        ChatSession chatSession = chatSessionRepository.save(new ChatSession(portfolio, viewer));
        return CreateChatSessionResponse.from(chatSession);
    }

    @Transactional(readOnly = true)
    public ChatMessagesResponse messages(User viewer, UUID chatSessionId) {
        ChatSession chatSession = getSessionForViewer(viewer, chatSessionId);
        List<ChatMessage> messages = chatMessageRepository.findByChatSessionOrderByCreatedAtAsc(chatSession);
        if (messages.isEmpty()) {
            return new ChatMessagesResponse(chatSession.getId(), List.of());
        }
        Map<UUID, List<ChatMessageSource>> sourcesByMessageId = chatMessageSourceRepository.findByChatMessageIn(messages).stream()
                .collect(Collectors.groupingBy(source -> source.getChatMessage().getId()));
        List<ChatMessageItemResponse> response = messages.stream()
                .map(message -> ChatMessageItemResponse.of(
                        message,
                        sourcesByMessageId.getOrDefault(message.getId(), List.of())
                ))
                .toList();
        return new ChatMessagesResponse(chatSession.getId(), response);
    }

    @Transactional
    public SendChatMessageResponse sendMessage(User viewer, UUID chatSessionId, String message, Integer currentPage) {
        validateMessage(message);
        ChatSession chatSession = getSessionForViewer(viewer, chatSessionId);
        validateCurrentPage(chatSession.getPortfolio(), currentPage);

        List<ChatMessage> previousMessages = recentMessages(chatMessageRepository.findByChatSessionOrderByCreatedAtAsc(chatSession));
        List<Document> documents = retrieveDocuments(chatSession.getPortfolio(), message, currentPage);

        boolean answerable = !documents.isEmpty();
        String answer = answerable
                ? askModel(chatSession, previousMessages, documents, message, currentPage)
                : UNANSWERABLE_MESSAGE;
        if (answer == null || answer.isBlank()) {
            answer = UNANSWERABLE_MESSAGE;
        }
        answerable = answerable && !isUnanswerable(answer);

        ChatMessage questionMessage = chatMessageRepository.save(new ChatMessage(
                chatSession,
                ChatMessageRole.USER,
                message.trim(),
                true,
                !answerable
        ));
        ChatMessage answerMessage = chatMessageRepository.save(new ChatMessage(
                chatSession,
                ChatMessageRole.ASSISTANT,
                answer,
                answerable,
                false
        ));
        chatSession.touch();

        List<ChatMessageSource> sources = saveSources(answerMessage, documents);
        if (!answerable) {
            Double bestScore = documents.stream()
                    .map(Document::getScore)
                    .filter(score -> score != null)
                    .max(Comparator.naturalOrder())
                    .orElse(null);
            questionInsightService.recordUnansweredQuestion(chatSession.getPortfolio(), questionMessage, bestScore);
        }

        return new SendChatMessageResponse(
                questionMessage.getId(),
                answerMessage.getId(),
                answer,
                answerable,
                sources.stream().map(ChatSourceResponse::from).toList(),
                answerable ? "SKIPPED" : "DONE"
        );
    }

    private ChatSession getSessionForViewer(User viewer, UUID chatSessionId) {
        ChatSession chatSession = chatSessionRepository.findById(chatSessionId)
                .orElseThrow(() -> ApiException.notFound("CHAT_SESSION_NOT_FOUND", "챗봇 세션을 찾을 수 없습니다."));
        if (!chatSession.getViewer().getId().equals(viewer.getId())) {
            throw ApiException.forbidden("챗봇 세션 접근 권한이 없습니다.");
        }
        portfolioPdfService.getAccessiblePortfolio(viewer, chatSession.getPortfolio().getId());
        return chatSession;
    }

    private List<Document> retrieveDocuments(Portfolio portfolio, String message, Integer currentPage) {
        VectorStore vectorStore = vectorStoreProvider.getIfAvailable();
        if (vectorStore == null) {
            throw ApiException.conflict(
                    "AI_VECTOR_STORE_NOT_CONFIGURED",
                    "VectorRAG를 사용하려면 EmbeddingModel과 PgVector VectorStore 설정이 필요합니다."
            );
        }
        SearchRequest request = SearchRequest.builder()
                .query(message)
                .topK(properties.getTopK())
                .similarityThreshold(properties.getSimilarityThreshold())
                .filterExpression(PortfolioVectorIndexService.portfolioFilter(portfolio.getId()))
                .build();
        List<Document> documents = vectorStore.similaritySearch(request);
        List<Document> currentPageDocuments = currentPageInputDocuments(portfolio, currentPage);
        return mergeDocuments(currentPageDocuments, documents == null ? List.of() : documents);
    }

    private String askModel(
            ChatSession chatSession,
            List<ChatMessage> previousMessages,
            List<Document> documents,
            String message,
            Integer currentPage
    ) {
        ChatModel chatModel = chatModelProvider.getIfAvailable();
        if (chatModel == null) {
            throw ApiException.conflict(
                    "AI_CHAT_MODEL_NOT_CONFIGURED",
                    "ChatGPT, Gemini, Ollama 중 사용할 ChatModel 설정이 필요합니다."
            );
        }
        ChatClient chatClient = ChatClient.builder(chatModel)
                .defaultSystem(systemPrompt())
                .build();
        return chatClient.prompt()
                .user(buildUserPrompt(chatSession, previousMessages, documents, message, currentPage))
                .call()
                .content();
    }

    private String systemPrompt() {
        return """
                당신은 포트폴리오 조회자를 돕는 챗봇입니다.
                반드시 제공된 사용자 입력 데이터와 포트폴리오 문맥만 근거로 답변하세요.
                사용자 입력 데이터는 작성자가 업로드한 PDF 텍스트, 페이지별 참고 텍스트, 페이지별 작성자 메모, 포트폴리오 제목/설명/직군/기술입니다.
                문맥에 없는 사실, 경력, 수치, 기술 사용 범위는 추측하지 마세요.
                답변할 근거가 부족하면 정확히 "%s" 문장으로 시작하세요.
                사용자가 다른 언어를 요청하지 않으면 한국어로 간결하게 답변하세요.
                """.formatted(UNANSWERABLE_MESSAGE);
    }

    private String buildUserPrompt(
            ChatSession chatSession,
            List<ChatMessage> previousMessages,
            List<Document> documents,
            String message,
            Integer currentPage
    ) {
        String context = documents.stream()
                .map(this::formatDocument)
                .collect(Collectors.joining("\n\n"));
        if (context.length() > properties.getMaxContextCharacters()) {
            context = context.substring(0, properties.getMaxContextCharacters());
        }
        String history = previousMessages.stream()
                .map(previous -> previous.getRole().name() + ": " + previous.getContent())
                .collect(Collectors.joining("\n"));

        return """
                [포트폴리오]
                제목: %s
                설명: %s
                현재 사용자가 보고 있는 페이지: %s

                [작성자 정보]
                %s

                [최근 대화]
                %s

                [검색된 포트폴리오 문맥]
                %s

                [사용자 질문]
                %s
                """.formatted(
                chatSession.getPortfolio().getTitle(),
                nullToEmpty(chatSession.getPortfolio().getDescription()),
                currentPage == null ? "알 수 없음" : currentPage,
                ownerInfo(chatSession.getPortfolio()),
                history.isBlank() ? "없음" : history,
                context,
                message
        );
    }

    private String ownerInfo(Portfolio portfolio) {
        return """
                이름: %s
                직군: %s
                기술: %s
                """.formatted(
                portfolio.getOwner().getName(),
                names(portfolio.getRoles().stream().map(RoleEntity::getName).toList()),
                names(portfolio.getSkills().stream().map(SkillEntity::getName).toList())
        );
    }

    private String formatDocument(Document document) {
        Map<String, Object> metadata = document.getMetadata();
        return """
                출처=%s, 페이지=%s, 점수=%s
                %s
                """.formatted(
                metadata.getOrDefault("sourceType", "UNKNOWN"),
                metadata.getOrDefault("pageNumber", "전체"),
                document.getScore() == null ? "알 수 없음" : document.getScore(),
                document.getText()
        );
    }

    private List<Document> currentPageInputDocuments(Portfolio portfolio, Integer currentPage) {
        if (currentPage == null) {
            return List.of();
        }

        List<Document> documents = new ArrayList<>();
        portfolioContextRepository.findByPortfolioIdAndPageNumber(portfolio.getId(), currentPage)
                .ifPresent(context -> documents.add(pageContextDocument(portfolio, context)));

        portfolioPageRepository.findByPortfolioAndPageNumber(portfolio, currentPage)
                .ifPresent(page -> {
                    addDocument(documents, portfolio, page.getPageNumber(), SOURCE_PDF_TEXT, page.getId().toString(), page.getExtractedText());
                    pageOwnerNoteRepository.findByPortfolioPage(page)
                            .ifPresent(note -> documents.add(ownerNoteDocument(portfolio, page, note)));
                });
        return documents;
    }

    private Document pageContextDocument(Portfolio portfolio, PortfolioContext context) {
        return inputDocument(
                portfolio,
                context.getPageNumber(),
                SOURCE_PAGE_CONTEXT,
                context.getId().toString(),
                context.getContent()
        );
    }

    private Document ownerNoteDocument(Portfolio portfolio, PortfolioPage page, PageOwnerNote note) {
        return inputDocument(
                portfolio,
                page.getPageNumber(),
                SOURCE_OWNER_NOTE,
                note.getId().toString(),
                note.getContent()
        );
    }

    private void addDocument(
            List<Document> documents,
            Portfolio portfolio,
            Integer pageNumber,
            String sourceType,
            String sourceId,
            String content
    ) {
        if (content == null || content.isBlank()) {
            return;
        }
        documents.add(inputDocument(portfolio, pageNumber, sourceType, sourceId, content));
    }

    private Document inputDocument(
            Portfolio portfolio,
            Integer pageNumber,
            String sourceType,
            String sourceId,
            String content
    ) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("portfolioId", portfolio.getId().toString());
        metadata.put("portfolioTitle", portfolio.getTitle());
        metadata.put("sourceType", sourceType);
        metadata.put("sourceId", sourceId);
        if (pageNumber != null) {
            metadata.put("pageNumber", pageNumber);
        }
        String id = "direct-%s-%s".formatted(sourceType, sourceId);
        String pageText = pageNumber == null ? "전체" : pageNumber.toString();
        String text = """
                포트폴리오 제목: %s
                페이지: %s
                출처: %s
                내용:
                %s
                """.formatted(portfolio.getTitle(), pageText, sourceType, content);
        return new Document(id, text, metadata);
    }

    private List<Document> mergeDocuments(List<Document> prioritizedDocuments, List<Document> vectorDocuments) {
        Map<String, Document> documentsBySource = new LinkedHashMap<>();
        prioritizedDocuments.forEach(document -> documentsBySource.putIfAbsent(documentKey(document), document));
        vectorDocuments.forEach(document -> documentsBySource.putIfAbsent(documentKey(document), document));
        return new ArrayList<>(documentsBySource.values());
    }

    private String documentKey(Document document) {
        Map<String, Object> metadata = document.getMetadata();
        Object sourceType = metadata.get("sourceType");
        Object sourceId = metadata.get("sourceId");
        if (sourceType == null || sourceId == null) {
            return document.getId();
        }
        return sourceType + ":" + sourceId;
    }

    private List<ChatMessage> recentMessages(List<ChatMessage> messages) {
        int from = Math.max(0, messages.size() - 8);
        return messages.subList(from, messages.size());
    }

    private List<ChatMessageSource> saveSources(ChatMessage answerMessage, List<Document> documents) {
        List<ChatMessageSource> sources = documents.stream()
                .map(document -> {
                    Map<String, Object> metadata = document.getMetadata();
                    return new ChatMessageSource(
                            answerMessage,
                            document.getId(),
                            value(metadata.get("sourceType")),
                            value(metadata.get("sourceId")),
                            intValue(metadata.get("pageNumber")),
                            document.getScore(),
                            snippet(document.getText())
                    );
                })
                .toList();
        return chatMessageSourceRepository.saveAll(sources);
    }

    private void validateMessage(String message) {
        if (message == null || message.isBlank()) {
            throw ApiException.badRequest("message는 필수입니다.");
        }
        if (message.length() > 2000) {
            throw ApiException.badRequest("message는 2000자를 초과할 수 없습니다.");
        }
    }

    private void validateCurrentPage(Portfolio portfolio, Integer currentPage) {
        if (currentPage == null) {
            return;
        }
        if (currentPage < 1 || currentPage > portfolio.getPageCount()) {
            throw ApiException.badRequest("currentPage가 포트폴리오 페이지 범위를 벗어났습니다.");
        }
    }

    private boolean isUnanswerable(String answer) {
        return answer != null && answer.trim().startsWith(UNANSWERABLE_MESSAGE);
    }

    private String value(Object value) {
        return value == null ? null : value.toString();
    }

    private Integer intValue(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value == null) {
            return null;
        }
        return Integer.parseInt(value.toString());
    }

    private String snippet(String text) {
        if (text == null) {
            return "";
        }
        String compact = text.replaceAll("\\s+", " ").trim();
        return compact.length() <= 220 ? compact : compact.substring(0, 220) + "...";
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private String names(List<String> values) {
        String result = values.stream()
                .filter(value -> value != null && !value.isBlank())
                .collect(Collectors.joining(", "));
        return result.isBlank() ? "없음" : result;
    }
}
