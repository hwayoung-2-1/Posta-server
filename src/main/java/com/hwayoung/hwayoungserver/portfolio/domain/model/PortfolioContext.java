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
import jakarta.persistence.UniqueConstraint;

import java.util.UUID;

@Entity
@Table(
        name = "portfolio_context",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_portfolio_context_portfolio_page",
                columnNames = {"portfolio_id", "page_number"}
        )
)
public class PortfolioContext extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    @Column(name = "page_number", nullable = false)
    private int pageNumber;

    @Lob
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(columnDefinition = "TEXT")
    private String embedding;

    protected PortfolioContext() {
    }

    public PortfolioContext(Portfolio portfolio, int pageNumber, String content) {
        this.portfolio = portfolio;
        this.pageNumber = pageNumber;
        this.content = content;
    }

    public UUID getId() {
        return id;
    }

    public Portfolio getPortfolio() {
        return portfolio;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public String getContent() {
        return content;
    }

    public String getEmbedding() {
        return embedding;
    }

    public void updateContent(String content, String embedding) {
        this.content = content;
        this.embedding = embedding;
    }
}
