package com.hwayoung.hwayoungserver.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.minio")
public class MinioProperties {
    private String endpoint;
    private String publicUrl;
    private String accessKey;
    private String secretKey;
    private String bucket;
    private String urlMode = "PRESIGNED";
    private boolean publicRead = false;
    private int presignedExpirySeconds = 3600;

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getPublicUrl() {
        return publicUrl == null || publicUrl.isBlank() ? endpoint : publicUrl;
    }

    public void setPublicUrl(String publicUrl) {
        this.publicUrl = publicUrl;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getUrlMode() {
        return urlMode;
    }

    public void setUrlMode(String urlMode) {
        this.urlMode = urlMode;
    }

    public boolean isPublicRead() {
        return publicRead;
    }

    public void setPublicRead(boolean publicRead) {
        this.publicRead = publicRead;
    }

    public int getPresignedExpirySeconds() {
        return presignedExpirySeconds;
    }

    public void setPresignedExpirySeconds(int presignedExpirySeconds) {
        this.presignedExpirySeconds = presignedExpirySeconds;
    }
}
