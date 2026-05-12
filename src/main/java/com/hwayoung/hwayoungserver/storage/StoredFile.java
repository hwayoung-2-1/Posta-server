package com.hwayoung.hwayoungserver.storage;

public record StoredFile(
        String objectName,
        String storageUrl,
        String accessUrl
) {
}
