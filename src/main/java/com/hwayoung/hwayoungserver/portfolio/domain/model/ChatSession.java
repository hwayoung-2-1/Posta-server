package com.hwayoung.hwayoungserver.portfolio.domain.model;

import com.hwayoung.hwayoungserver.common.AuditableEntity;
import com.hwayoung.hwayoungserver.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "chat_sessions")
public class ChatSession extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "viewer_user_id", nullable = false)
    private User viewer;

    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    protected ChatSession() {
    }

    public ChatSession(Portfolio portfolio, User viewer) {
        this.portfolio = portfolio;
        this.viewer = viewer;
        this.lastMessageAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public Portfolio getPortfolio() {
        return portfolio;
    }

    public User getViewer() {
        return viewer;
    }

    public LocalDateTime getLastMessageAt() {
        return lastMessageAt;
    }

    public void touch() {
        this.lastMessageAt = LocalDateTime.now();
    }
}
