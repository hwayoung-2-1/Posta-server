package com.hwayoung.hwayoungserver.portfolio.presentation.dto.response;

import com.hwayoung.hwayoungserver.portfolio.domain.model.ChatMessageSource;
import org.springframework.ai.document.Document;

import java.util.Map;

public record ChatSourceResponse(
        String chunkId,
        Integer pageNumber,
        String sourceType,
        String snippet,
        Double score
) {
    public static ChatSourceResponse from(ChatMessageSource source) {
        return new ChatSourceResponse(
                source.getVectorDocumentId(),
                source.getPageNumber(),
                source.getSourceType(),
                source.getSnippet(),
                source.getScore()
        );
    }

    public static ChatSourceResponse from(Document document) {
        Map<String, Object> metadata = document.getMetadata();
        return new ChatSourceResponse(
                document.getId(),
                intValue(metadata.get("pageNumber")),
                stringValue(metadata.get("sourceType")),
                snippet(document.getText()),
                document.getScore()
        );
    }

    private static Integer intValue(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value == null) {
            return null;
        }
        return Integer.parseInt(value.toString());
    }

    private static String stringValue(Object value) {
        return value == null ? null : value.toString();
    }

    private static String snippet(String text) {
        if (text == null) {
            return "";
        }
        String compact = text.replaceAll("\\s+", " ").trim();
        return compact.length() <= 220 ? compact : compact.substring(0, 220) + "...";
    }
}
