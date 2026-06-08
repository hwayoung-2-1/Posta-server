package com.hwayoung.hwayoungserver.storage;

import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface FileStorageService {
    StoredFile storePortfolioFile(UUID portfolioId, MultipartFile file);

    void upload(String objectKey, byte[] bytes, String contentType);

    void delete(String objectKey);

    String presignedGetUrl(String objectKey, int expirySeconds);

    String objectUrl(String objectKey);

    String viewUrl(String objectKey, int expirySeconds);
}
