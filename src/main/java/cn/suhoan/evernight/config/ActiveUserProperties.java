package cn.suhoan.evernight.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "evernight.online-users")
public class ActiveUserProperties {

    /**
     * 是否启用在线用户统计。
     */
    private boolean enabled = true;

    /**
     * 活跃窗口长度，单位秒；窗口内有请求的客户端会被计为在线。
     */
    private long windowSeconds = 300;

    /**
     * 最多保留的客户端标识数量。
     */
    private long maximumSize = 100000;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public long getWindowSeconds() {
        return windowSeconds;
    }

    public void setWindowSeconds(long windowSeconds) {
        this.windowSeconds = windowSeconds;
    }

    public long getMaximumSize() {
        return maximumSize;
    }

    public void setMaximumSize(long maximumSize) {
        this.maximumSize = maximumSize;
    }

}
