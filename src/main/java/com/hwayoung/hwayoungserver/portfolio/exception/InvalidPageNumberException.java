package com.hwayoung.hwayoungserver.portfolio.exception;

import com.hwayoung.hwayoungserver.common.ApiException;

public class InvalidPageNumberException extends ApiException {
    public InvalidPageNumberException(int pageNumber, int pageCount) {
        super(
                org.springframework.http.HttpStatus.BAD_REQUEST,
                "INVALID_PAGE_NUMBER",
                "pageNumber는 1 이상 " + pageCount + " 이하이어야 합니다. 요청값: " + pageNumber
        );
    }
}
