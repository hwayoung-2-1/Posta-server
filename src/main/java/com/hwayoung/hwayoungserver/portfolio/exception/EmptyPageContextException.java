package com.hwayoung.hwayoungserver.portfolio.exception;

import com.hwayoung.hwayoungserver.common.ApiException;

public class EmptyPageContextException extends ApiException {
    public EmptyPageContextException() {
        super(org.springframework.http.HttpStatus.BAD_REQUEST, "EMPTY_PAGE_CONTEXT", "페이지 설명 content는 필수입니다.");
    }
}
