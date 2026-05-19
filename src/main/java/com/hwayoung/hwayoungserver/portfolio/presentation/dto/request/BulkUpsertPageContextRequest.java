package com.hwayoung.hwayoungserver.portfolio.presentation.dto.request;

import java.util.List;

public record BulkUpsertPageContextRequest(
        List<Item> contexts
) {
    public record Item(
            int pageNumber,
            String content
    ) {
    }
}
