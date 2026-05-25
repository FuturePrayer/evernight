package cn.suhoan.evernight.whitelist;


import cn.suhoan.evernight.config.MavenRepositoryProperties;
import cn.suhoan.evernight.whitelist.MavenRepositoryResolver;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;

class MavenRepositoryResolverTests {

    @Test
    void resolveUsesWhitelistedRepositoryUrl() {
        MavenRepositoryProperties properties = new MavenRepositoryProperties();
        properties.setRepositories(List.of("https://repo1.maven.org/maven2/", "https://maven.aliyun.com/repository/public"));
        MavenRepositoryResolver resolver = new MavenRepositoryResolver(properties);

        assertThat(resolver.resolve("https://maven.aliyun.com/repository/public/")).isEqualTo("https://maven.aliyun.com/repository/public");
    }

    @Test
    void resolveRejectsUnknownRepositoryId() {
        MavenRepositoryResolver resolver = new MavenRepositoryResolver(new MavenRepositoryProperties());

        assertThatThrownBy(() -> resolver.resolve("https://evil.example/internal"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("不允许访问");
    }

}
