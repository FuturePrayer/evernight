package cn.suhoan.evernight.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "evernight.rate-limit")
public class RateLimitProperties {

    /**
     * 是否启用 IP 限流。
     */
    private boolean enabled = true;

    /**
     * CDN 或网关注入的真实客户端 IP 请求头名称。
     */
    private String clientIpHeader = "X-Evernight-Client-IP";

    /**
     * 每个 IP 每个窗口允许的最大请求数。
     */
    private int maxRequests = 120;

    /**
     * 限流窗口长度，单位秒。
     */
    private long windowSeconds = 60;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getClientIpHeader() {
        return clientIpHeader;
    }

    public void setClientIpHeader(String clientIpHeader) {
        this.clientIpHeader = clientIpHeader;
    }

    public int getMaxRequests() {
        return maxRequests;
    }

    public void setMaxRequests(int maxRequests) {
        this.maxRequests = maxRequests;
    }

    public long getWindowSeconds() {
        return windowSeconds;
    }

    public void setWindowSeconds(long windowSeconds) {
        this.windowSeconds = windowSeconds;
    }

}