package cn.suhoan.evernight.common;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "evernight.http")
public class HttpClientProperties {

    /**
     * 连接超时时间，单位毫秒。
     */
    private int connectTimeoutMillis = 5000;

    /**
     * 读取超时时间，单位毫秒。
     */
    private int readTimeoutMillis = 10000;

    /**
     * 代理类型：none、http、https、socks5。
     */
    private String proxyType = "none";

    /**
     * 代理主机地址，默认不启用代理。
     */
    private String proxyHost;

    /**
     * 代理端口。
     */
    private Integer proxyPort;

    public int getConnectTimeoutMillis() {
        return connectTimeoutMillis;
    }

    public void setConnectTimeoutMillis(int connectTimeoutMillis) {
        this.connectTimeoutMillis = connectTimeoutMillis;
    }

    public int getReadTimeoutMillis() {
        return readTimeoutMillis;
    }

    public void setReadTimeoutMillis(int readTimeoutMillis) {
        this.readTimeoutMillis = readTimeoutMillis;
    }

    public String getProxyType() {
        return proxyType;
    }

    public void setProxyType(String proxyType) {
        this.proxyType = proxyType;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    public Integer getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(Integer proxyPort) {
        this.proxyPort = proxyPort;
    }

}
