package cn.suhoan.evernight.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;

class RepositoryWhitelistResolverTests {

    @Test
    void resolveUsesFirstRepositoryWhenRequestIsBlank() {
        assertThat(RepositoryWhitelistResolver.resolve(null,
                List.of("https://repo1.maven.org/maven2", "https://maven.aliyun.com/repository/public"), "Maven 仓库"))
                .isEqualTo("https://repo1.maven.org/maven2");
    }

    @Test
    void resolveAcceptsWhitelistedUrlAndNormalizesTrailingSlash() {
        assertThat(RepositoryWhitelistResolver.resolve("https://repo1.maven.org/maven2/",
                List.of("https://repo1.maven.org/maven2"), "Maven 仓库"))
                .isEqualTo("https://repo1.maven.org/maven2");
    }

    @Test
    void normalizeRepositoriesSplitsCommaSeparatedEnvironmentValue() {
        assertThat(RepositoryWhitelistResolver.normalizeRepositories(
                List.of("https://repo1.maven.org/maven2, https://maven.aliyun.com/repository/public/")))
                .containsExactly("https://repo1.maven.org/maven2", "https://maven.aliyun.com/repository/public");
    }

    @Test
    void resolveRejectsUnknownUrl() {
        assertThatThrownBy(() -> RepositoryWhitelistResolver.resolve("https://evil.example/internal",
                List.of("https://repo1.maven.org/maven2"), "Maven 仓库"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("不允许访问");
    }

}
