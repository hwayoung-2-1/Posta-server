package com.hwayoung.hwayoungserver.portfolio.domain.model;

import com.hwayoung.hwayoungserver.common.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "chat_message_sources")
public class ChatMessageSource extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chat_message_id", nullable = false)
    private ChatMessage chatMessage;

    @Column(name = "vector_document_id", nullable = false, length = 255)
    private String vectorDocumentId;

    @Column(name = "source_type", nullable = false, length = 40)
    private String sourceType;

    @Column(name = "source_id", length = 80)
    private String sourceId;

    @Column(name = "page_number")
    private Integer pageNumber;

    @Column
    private Double score;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String snippet;

    protected ChatMessageSource() {
    }

    public ChatMessageSource(
            ChatMessage chatMessage,
            String vectorDocumentId,
            String sourceType,
            String sourceId,
            Integer pageNumber,
            Double score,
            String snippet
    ) {
        this.chatMessage = chatMessage;
        this.vectorDocumentId = vectorDocumentId;
        this.sourceType = sourceType;
        this.sourceId = sourceId;
        this.pageNumber = pageNumber;
        this.score = score;
        this.snippet = snippet;
    }

    public UUID getId() {
        return id;
    }

    public ChatMessage getChatMessage() {
        return chatMessage;
    }

    public String getVectorDocumentId() {
        return vectorDocumentId;
    }

    public String getSourceType() {
        return sourceType;
    }

    public String getSourceId() {
        return sourceId;
    }

    public Integer getPageNumber() {
        return pageNumber;
    }

    public Double getScore() {
        return score;
    }

    public String getSnippet() {
        return snippet;
    }
}
