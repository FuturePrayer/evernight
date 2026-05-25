package cn.suhoan.evernight.web;


import cn.suhoan.evernight.web.ClientIpResolver;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class ClientIpResolverTests {

    @Test
    void resolveUsesConfiguredHeaderFirst() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.1");
        request.addHeader("X-Evernight-Client-IP", "1.2.3.4");

        assertThat(ClientIpResolver.resolve(request, "X-Evernight-Client-IP")).isEqualTo("1.2.3.4");
    }

    @Test
    void resolveFallsBackToForwardedFor() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.1");
        request.addHeader("X-Forwarded-For", "2.2.2.2, 3.3.3.3");

        assertThat(ClientIpResolver.resolve(request, "X-Missing-IP")).isEqualTo("2.2.2.2");
    }

}