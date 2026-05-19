package com.hwayoung.hwayoungserver.portfolio.domain.model;

import com.hwayoung.hwayoungserver.common.AuditableEntity;
import com.hwayoung.hwayoungserver.portfolio.domain.type.ExtractionStatus;
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

import java.util.UUID;

@Entity
@Table(name = "portfolio_pages")
public class PortfolioPage extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "portfolio_file_id", nullable = false)
    private PortfolioFile portfolioFile;

    @Column(name = "page_number", nullable = false)
    private int pageNumber;

    @Column(name = "page_image_url")
    private String pageImageUrl;

    @Lob
    @Column(name = "extracted_text", columnDefinition = "TEXT")
    private String extractedText;

    @Enumerated(EnumType.STRING)
    @Column(name = "extraction_status", nullable = false)
    private ExtractionStatus extractionStatus;

    protected PortfolioPage() {
    }

    public PortfolioPage(Portfolio portfolio, PortfolioFile portfolioFile, int pageNumber, String pageImageUrl, String extractedText) {
        this.portfolio = portfolio;
        this.portfolioFile = portfolioFile;
        this.pageNumber = pageNumber;
        this.pageImageUrl = pageImageUrl;
        this.extractedText = extractedText;
        this.extractionStatus = ExtractionStatus.DONE;
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

    public String getPageImageUrl() {
        return pageImageUrl;
    }

    public String getExtractedText() {
        return extractedText;
    }
}
