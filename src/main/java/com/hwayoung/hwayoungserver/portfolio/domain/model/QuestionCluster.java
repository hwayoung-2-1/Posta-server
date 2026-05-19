package com.hwayoung.hwayoungserver.portfolio.domain.model;

import com.hwayoung.hwayoungserver.common.AuditableEntity;
import com.hwayoung.hwayoungserver.portfolio.domain.type.QuestionCategory;
import com.hwayoung.hwayoungserver.portfolio.domain.type.QuestionClusterStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "question_clusters")
public class QuestionCluster extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    @Column(nullable = false)
    private String title;

    @Lob
    @Column(columnDefinition = "TEXT", nullable = false)
    private String summary;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 60)
    private QuestionCategory category;

    @Column(name = "question_count", nullable = false)
    private int questionCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private QuestionClusterStatus status;

    @Column(name = "first_asked_at", nullable = false)
    private LocalDateTime firstAskedAt;

    @Column(name = "last_asked_at", nullable = false)
    private LocalDateTime lastAskedAt;

    protected QuestionCluster() {
    }

    public QuestionCluster(Portfolio portfolio, QuestionCategory category, String title, String summary, LocalDateTime askedAt) {
        this.portfolio = portfolio;
        this.category = category;
        this.title = title;
        this.summary = summary;
        this.questionCount = 0;
        this.status = QuestionClusterStatus.OPEN;
        this.firstAskedAt = askedAt;
        this.lastAskedAt = askedAt;
    }

    public UUID getId() {
        return id;
    }

    public Portfolio getPortfolio() {
        return portfolio;
    }

    public String getTitle() {
        return title;
    }

    public String getSummary() {
        return summary;
    }

    public QuestionCategory getCategory() {
        return category;
    }

    public int getQuestionCount() {
        return questionCount;
    }

    public QuestionClusterStatus getStatus() {
        return status;
    }

    public LocalDateTime getFirstAskedAt() {
        return firstAskedAt;
    }

    public LocalDateTime getLastAskedAt() {
        return lastAskedAt;
    }

    public void recordQuestion(LocalDateTime askedAt) {
        this.questionCount++;
        this.lastAskedAt = askedAt;
    }

    public void updateStatus(QuestionClusterStatus status) {
        this.status = status;
    }
}
