package com.hwayoung.hwayoungserver.portfolio.domain.model;

import com.hwayoung.hwayoungserver.common.AuditableEntity;
import com.hwayoung.hwayoungserver.portfolio.domain.type.SummaryType;
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
@Table(name = "portfolio_summaries")
public class PortfolioSummary extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    @Enumerated(EnumType.STRING)
    @Column(name = "summary_type", nullable = false)
    private SummaryType summaryType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    protected PortfolioSummary() {
    }

    public PortfolioSummary(Portfolio portfolio, SummaryType summaryType, String content) {
        this.portfolio = portfolio;
        this.summaryType = summaryType;
        this.content = content;
    }

    public SummaryType getSummaryType() {
        return summaryType;
    }

    public Portfolio getPortfolio() {
        return portfolio;
    }

    public String getContent() {
        return content;
    }
}
