package cn.suhoan.evernight.http;


import cn.suhoan.evernight.config.HttpClientProperties;
import cn.suhoan.evernight.http.HutoolExternalHttpClient;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class HutoolExternalHttpClientTests {

    @Test
    void createProxyReturnsNullWhenDisabled() {
        HttpClientProperties properties = new HttpClientProperties();
        HutoolExternalHttpClient client = new HutoolExternalHttpClient(properties);

        assertThat(client.createProxy()).isNull();
    }

    @Test
    void createProxyCreatesHttpProxy() {
        HttpClientProperties properties = new HttpClientProperties();
        properties.setProxyUrl("http://127.0.0.1:8080");
        HutoolExternalHttpClient client = new HutoolExternalHttpClient(properties);

        assertThat(client.createProxy()).isNotNull();
        assertThat(client.createProxy().address().toString()).contains("127.0.0.1");
    }

    @Test
    void createProxyCreatesSocks5Proxy() {
        HttpClientProperties properties = new HttpClientProperties();
        properties.setProxyUrl("socks5://127.0.0.1:1080");
        HutoolExternalHttpClient client = new HutoolExternalHttpClient(properties);

        assertThat(client.createProxy()).isNotNull();
        assertThat(client.createProxy().type()).isEqualTo(java.net.Proxy.Type.SOCKS);
    }

    @Test
    void createProxyRejectsMissingPort() {
        HttpClientProperties properties = new HttpClientProperties();
        properties.setProxyUrl("http://127.0.0.1");
        HutoolExternalHttpClient client = new HutoolExternalHttpClient(properties);

        assertThatThrownBy(client::createProxy)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("协议://host:端口");
    }

    @Test
    void createProxyRejectsAuthenticationInfo() {
        HttpClientProperties properties = new HttpClientProperties();
        properties.setProxyUrl("http://user:pass@127.0.0.1:8080");
        HutoolExternalHttpClient client = new HutoolExternalHttpClient(properties);

        assertThatThrownBy(client::createProxy)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("不支持认证信息");
    }

}
