package com.hwayoung.hwayoungserver.portfolio.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class SavedPortfolioId implements Serializable {
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "portfolio_id")
    private UUID portfolioId;

    protected SavedPortfolioId() {
    }

    public SavedPortfolioId(UUID userId, UUID portfolioId) {
        this.userId = userId;
        this.portfolioId = portfolioId;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof SavedPortfolioId that)) {
            return false;
        }
        return Objects.equals(userId, that.userId) && Objects.equals(portfolioId, that.portfolioId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, portfolioId);
    }
}
