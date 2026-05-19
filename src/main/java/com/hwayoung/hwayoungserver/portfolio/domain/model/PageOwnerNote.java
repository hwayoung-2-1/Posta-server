package com.hwayoung.hwayoungserver.portfolio.domain.model;

import com.hwayoung.hwayoungserver.common.AuditableEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.util.UUID;

@Entity
@Table(
        name = "page_owner_notes",
        uniqueConstraints = @UniqueConstraint(name = "uk_page_owner_notes_page", columnNames = "portfolio_page_id")
)
public class PageOwnerNote extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "portfolio_page_id", nullable = false)
    private PortfolioPage portfolioPage;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String content;

    protected PageOwnerNote() {
    }

    public PageOwnerNote(PortfolioPage portfolioPage, String content) {
        this.portfolioPage = portfolioPage;
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void update(String content) {
        this.content = content;
    }
}
