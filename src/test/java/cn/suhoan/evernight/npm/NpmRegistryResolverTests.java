package cn.suhoan.evernight.npm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class NpmRegistryResolverTests {

    @Test
    void resolveAcceptsWhitelistedRegistryUrl() {
        NpmRegistryResolver resolver = new NpmRegistryResolver(new NpmRegistryProperties());

        assertThat(resolver.resolve("https://registry.npmmirror.com/"))
                .isEqualTo("https://registry.npmmirror.com");
    }

    @Test
    void resolveRejectsUnknownRegistryUrl() {
        NpmRegistryResolver resolver = new NpmRegistryResolver(new NpmRegistryProperties());

        assertThatThrownBy(() -> resolver.resolve("https://evil.example/npm"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("不允许访问");
    }

}
