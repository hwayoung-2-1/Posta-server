package com.hwayoung.hwayoungserver.portfolio.exception;

import com.hwayoung.hwayoungserver.common.ApiException;

public class FileSizeExceededException extends ApiException {
    public FileSizeExceededException(long maxSizeBytes) {
        super(
                org.springframework.http.HttpStatus.PAYLOAD_TOO_LARGE,
                "FILE_SIZE_EXCEEDED",
                "PDF 파일 크기는 " + maxSizeBytes + " bytes를 초과할 수 없습니다."
        );
    }
}
