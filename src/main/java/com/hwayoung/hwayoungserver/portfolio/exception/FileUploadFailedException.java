package com.hwayoung.hwayoungserver.portfolio.exception;

import com.hwayoung.hwayoungserver.common.ApiException;

public class FileUploadFailedException extends ApiException {
    public FileUploadFailedException() {
        super(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "FILE_UPLOAD_FAILED", "MinIO 업로드에 실패했습니다.");
    }
}
