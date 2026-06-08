package com.hwayoung.hwayoungserver.portfolio.domain.model;

import com.hwayoung.hwayoungserver.common.AuditableEntity;
import com.hwayoung.hwayoungserver.portfolio.domain.type.ChatMessageRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "chat_messages")
public class ChatMessage extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chat_session_id", nullable = false)
    private ChatSession chatSession;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ChatMessageRole role;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(nullable = false)
    private boolean answerable;

    @Column(nullable = false)
    private boolean analyzable;

    protected ChatMessage() {
    }

    public ChatMessage(ChatSession chatSession, ChatMessageRole role, String content, boolean answerable, boolean analyzable) {
        this.chatSession = chatSession;
        this.role = role;
        this.content = content;
        this.answerable = answerable;
        this.analyzable = analyzable;
    }

    public UUID getId() {
        return id;
    }

    public ChatSession getChatSession() {
        return chatSession;
    }

    public ChatMessageRole getRole() {
        return role;
    }

    public String getContent() {
        return content;
    }

    public boolean isAnswerable() {
        return answerable;
    }

    public boolean isAnalyzable() {
        return analyzable;
    }
}
