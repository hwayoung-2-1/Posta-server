package com.hwayoung.hwayoungserver.portfolio.exception;

import com.hwayoung.hwayoungserver.common.ApiException;

public class PortfolioAccessDeniedException extends ApiException {
    public PortfolioAccessDeniedException(String message) {
        super(org.springframework.http.HttpStatus.FORBIDDEN, "FORBIDDEN", message);
    }
}
