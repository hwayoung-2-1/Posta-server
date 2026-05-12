package com.hwayoung.hwayoungserver.storage;

import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface FileStorageService {
    StoredFile storePortfolioFile(UUID portfolioId, MultipartFile file);
}
