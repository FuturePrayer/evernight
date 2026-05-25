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
     * 代理地址，格式为 协议://host:端口；为空时不启用代理。
     */
    private String proxyUrl;

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

    public String getProxyUrl() {
        return proxyUrl;
    }

    public void setProxyUrl(String proxyUrl) {
        this.proxyUrl = proxyUrl;
    }

}
