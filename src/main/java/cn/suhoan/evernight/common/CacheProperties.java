package cn.suhoan.evernight.common;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "evernight.cache")
public class CacheProperties {

    /**
     * 是否启用外部查询缓存。
     */
    private boolean enabled = true;

    /**
     * 缓存最大条目数，避免公共服务内存无限增长。
     */
    private long maximumSize = 10000;

    /**
     * Maven Artifact 查询缓存时间，单位秒。
     */
    private long mavenTtlSeconds = 21600;

    /**
     * npm 包信息缓存时间，单位秒。
     */
    private long npmTtlSeconds = 21600;

    /**
     * PyPI 包信息缓存时间，单位秒。
     */
    private long pypiTtlSeconds = 21600;

    /**
     * OSV 漏洞查询缓存时间，单位秒。
     */
    private long osvTtlSeconds = 3600;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public long getMaximumSize() {
        return maximumSize;
    }

    public void setMaximumSize(long maximumSize) {
        this.maximumSize = maximumSize;
    }

    public long getMavenTtlSeconds() {
        return mavenTtlSeconds;
    }

    public void setMavenTtlSeconds(long mavenTtlSeconds) {
        this.mavenTtlSeconds = mavenTtlSeconds;
    }

    public long getNpmTtlSeconds() {
        return npmTtlSeconds;
    }

    public void setNpmTtlSeconds(long npmTtlSeconds) {
        this.npmTtlSeconds = npmTtlSeconds;
    }

    public long getPypiTtlSeconds() {
        return pypiTtlSeconds;
    }

    public void setPypiTtlSeconds(long pypiTtlSeconds) {
        this.pypiTtlSeconds = pypiTtlSeconds;
    }

    public long getOsvTtlSeconds() {
        return osvTtlSeconds;
    }

    public void setOsvTtlSeconds(long osvTtlSeconds) {
        this.osvTtlSeconds = osvTtlSeconds;
    }

}