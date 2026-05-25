package cn.suhoan.evernight.web;


import cn.suhoan.evernight.web.PublicRequestMatcher;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class PublicRequestMatcherTests {

    @Test
    void matchesHomePageAndStaticAssets() {
        assertThat(PublicRequestMatcher.isHomePageRequest(request("/"))).isTrue();
        assertThat(PublicRequestMatcher.isHomePageRequest(request("/index.html"))).isTrue();
        assertThat(PublicRequestMatcher.isHomePageRequest(request("/tools.html"))).isTrue();
        assertThat(PublicRequestMatcher.isHomePageRequest(request("/app.js"))).isTrue();
        assertThat(PublicRequestMatcher.isHomePageRequest(request("/tools.js"))).isTrue();
        assertThat(PublicRequestMatcher.isHomePageRequest(request("/styles.css"))).isTrue();
        assertThat(PublicRequestMatcher.isHomePageRequest(request("/icon.svg"))).isTrue();
    }

    @Test
    void matchesHomePageSupportApisAndActuator() {
        assertThat(PublicRequestMatcher.isHomePageRequest(request("/api/repositories"))).isTrue();
        assertThat(PublicRequestMatcher.isHomePageRequest(request("/api/online-users"))).isTrue();
        assertThat(PublicRequestMatcher.isHomePageRequest(request("/api/human-challenge"))).isTrue();
        assertThat(PublicRequestMatcher.isHomePageRequest(request("/actuator/health"))).isTrue();
    }

    @Test
    void doesNotMatchBusinessEndpoints() {
        assertThat(PublicRequestMatcher.isHomePageRequest(request("/mcp"))).isFalse();
        assertThat(PublicRequestMatcher.isHomePageRequest(request("/api/tool-invocations"))).isFalse();
        assertThat(PublicRequestMatcher.isHomePageRequest(request("/api/unknown"))).isFalse();
    }

    private static MockHttpServletRequest request(String path) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI(path);
        return request;
    }

}
