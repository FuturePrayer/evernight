package cn.suhoan.evernight.common;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Map;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@EnableConfigurationProperties(HttpClientProperties.class)
public class HutoolExternalHttpClient implements ExternalHttpClient {

    private static final Logger log = LoggerFactory.getLogger(HutoolExternalHttpClient.class);

    private static final String USER_AGENT = "Evernight-MCP/0.0.1";

    private final HttpClientProperties properties;

    public HutoolExternalHttpClient(HttpClientProperties properties) {
        this.properties = properties;
    }

    @Override
    public ExternalHttpResponse get(String url, Map<String, String> headers) {
        log.debug("开始发送 GET 请求，url={}", url);
        return execute(HttpUtil.createGet(url), headers);
    }

    @Override
    public ExternalHttpResponse postJson(String url, String body, Map<String, String> headers) {
        log.debug("开始发送 POST JSON 请求，url={}", url);
        return execute(HttpUtil.createPost(url).body(body), headers);
    }

    private ExternalHttpResponse execute(HttpRequest request, Map<String, String> headers) {
        try {
            request.timeout(properties.getReadTimeoutMillis())
                    .setConnectionTimeout(properties.getConnectTimeoutMillis())
                    .header("User-Agent", USER_AGENT);
            headers.forEach(request::header);
            Proxy proxy = createProxy();
            if (proxy != null) {
                request.setProxy(proxy);
            }
            try (HttpResponse response = request.execute()) {
                log.debug("外部 HTTP 请求完成，status={}", response.getStatus());
                return new ExternalHttpResponse(response.getStatus(), response.body());
            }
        }
        catch (Exception ex) {
            log.warn("外部 HTTP 请求失败", ex);
            throw new ExternalServiceException("外部 HTTP 请求失败", ex);
        }
    }

    Proxy createProxy() {
        String proxyType = properties.getProxyType();
        if (!StringUtils.hasText(proxyType) || "none".equalsIgnoreCase(proxyType)) {
            return null;
        }
        if (!StringUtils.hasText(properties.getProxyHost()) || properties.getProxyPort() == null) {
            throw new IllegalArgumentException("启用代理时必须配置 evernight.http.proxy-host 和 evernight.http.proxy-port");
        }
        Proxy.Type type = switch (proxyType.trim().toLowerCase()) {
            case "http", "https" -> Proxy.Type.HTTP;
            case "socks5" -> Proxy.Type.SOCKS;
            default -> throw new IllegalArgumentException("不支持的代理类型: " + proxyType);
        };
        return new Proxy(type, new InetSocketAddress(properties.getProxyHost(), properties.getProxyPort()));
    }

}
