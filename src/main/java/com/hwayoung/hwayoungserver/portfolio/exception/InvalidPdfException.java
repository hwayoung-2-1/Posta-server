package com.hwayoung.hwayoungserver.portfolio.exception;

import com.hwayoung.hwayoungserver.common.ApiException;

public class InvalidPdfException extends ApiException {
    public InvalidPdfException(String message) {
        super(org.springframework.http.HttpStatus.BAD_REQUEST, "INVALID_PDF", message);
    }
}
