package com.hwayoung.hwayoungserver.common;

public record ErrorResponse(
        int status,
        String code,
        String message
) {
}
