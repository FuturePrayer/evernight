package cn.suhoan.evernight.maven;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class MavenArtifactClientTests {

    private final MavenArtifactClient client = new MavenArtifactClient(null, null, cn.suhoan.evernight.common.TestSupport.disabledCacheService());

    @Test
    void buildSearchQueryCombinesProvidedFields() {
        assertThat(MavenArtifactClient.buildSearchQuery("spring boot", "org.springframework.boot", "spring-boot"))
                .contains("spring boot")
                .contains("g:\"")
                .contains("a:\"");
    }

    @Test
    void parsePomReadsBasicMetadata() {
        String pom = """
                <project>
                  <packaging>pom</packaging>
                  <name>Demo</name>
                  <description>Hello</description>
                  <url>https://example.com</url>
                </project>
                """;

        MavenArtifactClient.PomDetail detail = client.parsePom(pom);

        assertThat(detail.packaging()).isEqualTo("pom");
        assertThat(detail.name()).isEqualTo("Demo");
        assertThat(detail.description()).isEqualTo("Hello");
        assertThat(detail.url()).isEqualTo("https://example.com");
    }

    @Test
    void repositorySearchRejectsKeywordSearch() {
        assertThatThrownBy(() -> client.doSearch("spring", null, null, 10, "https://maven.aliyun.com/repository/public"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("关键词搜索");
    }

}
