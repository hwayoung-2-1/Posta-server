package com.hwayoung.hwayoungserver.portfolio.exception;

import com.hwayoung.hwayoungserver.common.ApiException;

public class PortfolioNotFoundException extends ApiException {
    public PortfolioNotFoundException() {
        super(org.springframework.http.HttpStatus.NOT_FOUND, "PORTFOLIO_NOT_FOUND", "포트폴리오를 찾을 수 없습니다.");
    }
}
