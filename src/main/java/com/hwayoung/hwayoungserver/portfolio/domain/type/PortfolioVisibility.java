package com.hwayoung.hwayoungserver.portfolio.domain.type;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Locale;

public enum PortfolioVisibility {
    PRIVATE,
    PUBLIC,
    LINK_ONLY;

    @JsonCreator
    public static PortfolioVisibility from(String value) {
        if (value == null || value.isBlank()) {
            return PRIVATE;
        }

        String normalized = value.trim().replace("-", "_").toUpperCase(Locale.ROOT);
        return PortfolioVisibility.valueOf(normalized);
    }

    public static PortfolioVisibility fromPdfUploadValue(String value) {
        PortfolioVisibility visibility = from(value);
        if (visibility == LINK_ONLY) {
            throw new IllegalArgumentException("PDF 포트폴리오 공개 범위는 public 또는 private만 사용할 수 있습니다.");
        }
        return visibility;
    }

    public boolean isPublic() {
        return this == PUBLIC;
    }

    @JsonValue
    public String toJson() {
        return name().toLowerCase(Locale.ROOT);
    }
}
