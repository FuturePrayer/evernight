package cn.suhoan.evernight.pypi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class PypiRepositoryResolverTests {

    @Test
    void resolveAcceptsWhitelistedRepositoryUrl() {
        PypiRepositoryResolver resolver = new PypiRepositoryResolver(new PypiRepositoryProperties());

        assertThat(resolver.resolve("https://pypi.tuna.tsinghua.edu.cn/pypi/"))
                .isEqualTo("https://pypi.tuna.tsinghua.edu.cn/pypi");
    }

    @Test
    void resolveRejectsUnknownRepositoryUrl() {
        PypiRepositoryResolver resolver = new PypiRepositoryResolver(new PypiRepositoryProperties());

        assertThatThrownBy(() -> resolver.resolve("https://evil.example/pypi"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("不允许访问");
    }

}
