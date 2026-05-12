package com.hwayoung.hwayoungserver.portfolio.domain.model;

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
@Table(name = "suggested_questions")
public class SuggestedQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_page_id")
    private PortfolioPage portfolioPage;

    @Column(nullable = false)
    private String question;

    @Column(nullable = false)
    private String source;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    protected SuggestedQuestion() {
    }

    public SuggestedQuestion(Portfolio portfolio, PortfolioPage portfolioPage, String question, String source, int displayOrder) {
        this.portfolio = portfolio;
        this.portfolioPage = portfolioPage;
        this.question = question;
        this.source = source;
        this.displayOrder = displayOrder;
    }

    public UUID getId() {
        return id;
    }

    public PortfolioPage getPortfolioPage() {
        return portfolioPage;
    }

    public String getQuestion() {
        return question;
    }
}
