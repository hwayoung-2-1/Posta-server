package com.hwayoung.hwayoungserver.portfolio.application;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.portfolio.pdf")
public class PortfolioPdfProperties {
    private long maxSizeBytes = 50L * 1024L * 1024L;
    private int viewUrlExpirySeconds = 600;

    public long getMaxSizeBytes() {
        return maxSizeBytes;
    }

    public void setMaxSizeBytes(long maxSizeBytes) {
        this.maxSizeBytes = maxSizeBytes;
    }

    public int getViewUrlExpirySeconds() {
        return viewUrlExpirySeconds;
    }

    public void setViewUrlExpirySeconds(int viewUrlExpirySeconds) {
        this.viewUrlExpirySeconds = viewUrlExpirySeconds;
    }
}
