package cn.suhoan.evernight.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class HutoolExternalHttpClientTests {

    @Test
    void createProxyReturnsNullWhenDisabled() {
        HttpClientProperties properties = new HttpClientProperties();
        properties.setProxyType("none");
        HutoolExternalHttpClient client = new HutoolExternalHttpClient(properties);

        assertThat(client.createProxy()).isNull();
    }

    @Test
    void createProxyCreatesHttpProxy() {
        HttpClientProperties properties = new HttpClientProperties();
        properties.setProxyType("http");
        properties.setProxyHost("127.0.0.1");
        properties.setProxyPort(8080);
        HutoolExternalHttpClient client = new HutoolExternalHttpClient(properties);

        assertThat(client.createProxy()).isNotNull();
        assertThat(client.createProxy().address().toString()).contains("127.0.0.1");
    }

    @Test
    void createProxyRejectsMissingHostOrPort() {
        HttpClientProperties properties = new HttpClientProperties();
        properties.setProxyType("socks5");
        HutoolExternalHttpClient client = new HutoolExternalHttpClient(properties);

        assertThatThrownBy(client::createProxy)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("proxy-host");
    }

}
