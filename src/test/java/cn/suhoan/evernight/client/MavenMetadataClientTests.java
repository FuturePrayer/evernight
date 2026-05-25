package cn.suhoan.evernight.client;


import cn.suhoan.evernight.client.MavenMetadataClient;
import cn.suhoan.evernight.config.MavenRepositoryProperties;
import cn.suhoan.evernight.exception.MavenArtifactNotFoundException;
import cn.suhoan.evernight.model.MavenVersionInfo;
import cn.suhoan.evernight.whitelist.MavenRepositoryResolver;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Map;

import cn.suhoan.evernight.http.ExternalHttpClient;
import cn.suhoan.evernight.http.ExternalHttpResponse;
import org.junit.jupiter.api.Test;

class MavenMetadataClientTests {

    private final MavenRepositoryProperties properties = new MavenRepositoryProperties();

    private final MavenMetadataClient client = new MavenMetadataClient(new MavenRepositoryResolver(properties), new StubHttpClient());

    @Test
    void resolveRepositoryBaseUrlUsesMavenCentralByDefault() {
        assertThat(client.resolveRepositoryBaseUrl(null))
                .isEqualTo(MavenRepositoryProperties.MAVEN_CENTRAL_BASE_URL);
    }

    @Test
    void resolveRepositoryBaseUrlUsesConfiguredDefaultRepository() {
        properties.setRepositories(List.of("https://maven.aliyun.com/repository/public/", MavenRepositoryProperties.MAVEN_CENTRAL_BASE_URL));

        assertThat(client.resolveRepositoryBaseUrl(null))
                .isEqualTo("https://maven.aliyun.com/repository/public");
    }

    @Test
    void resolveRepositoryBaseUrlUsesWhitelistedRepositoryUrl() {
        properties.setRepositories(List.of("https://repo1.maven.org/maven2", "https://maven.aliyun.com/repository/public"));

        assertThat(client.resolveRepositoryBaseUrl("https://maven.aliyun.com/repository/public"))
                .isEqualTo("https://maven.aliyun.com/repository/public");
    }

    @Test
    void buildMetadataUriUsesGroupPath() {
        assertThat(client.buildMetadataUri("https://repo1.maven.org/maven2", "org.springframework.boot", "spring-boot"))
                .hasToString("https://repo1.maven.org/maven2/org/springframework/boot/spring-boot/maven-metadata.xml");
    }

    @Test
    void parseMetadataReturnsLatestAndVersionList() {
        String metadata = """
                <metadata>
                  <groupId>org.example</groupId>
                  <artifactId>demo</artifactId>
                  <versioning>
                    <latest>1.2.0</latest>
                    <release>1.1.0</release>
                    <versions>
                      <version>1.0.0</version>
                      <version>1.1.0</version>
                      <version>1.2.0</version>
                    </versions>
                  </versioning>
                </metadata>
                """;

        MavenVersionInfo versionInfo = client.parseMetadata(metadata, "org.example", "demo", "https://repo.example.com");

        assertThat(versionInfo.latestVersion()).isEqualTo("1.2.0");
        assertThat(versionInfo.versions()).containsExactly("1.0.0", "1.1.0", "1.2.0");
        assertThat(versionInfo.repositoryBaseUrl()).isEqualTo("https://repo.example.com");
    }

    @Test
    void parseMetadataFallsBackToLastVersion() {
        String metadata = """
                <metadata>
                  <versioning>
                    <versions>
                      <version>1.0.0</version>
                      <version>1.1.0</version>
                    </versions>
                  </versioning>
                </metadata>
                """;

        MavenVersionInfo versionInfo = client.parseMetadata(metadata, "org.example", "demo", "https://repo.example.com");

        assertThat(versionInfo.latestVersion()).isEqualTo("1.1.0");
    }

    @Test
    void parseMetadataThrowsWhenNoVersionsExist() {
        String metadata = """
                <metadata>
                  <versioning>
                    <versions />
                  </versioning>
                </metadata>
                """;

        assertThatThrownBy(() -> client.parseMetadata(metadata, "org.example", "demo", "https://repo.example.com"))
                .isInstanceOf(MavenArtifactNotFoundException.class);
    }

    private static final class StubHttpClient implements ExternalHttpClient {
        @Override
        public ExternalHttpResponse get(String url, Map<String, String> headers) {
            return new ExternalHttpResponse(404, "");
        }

        @Override
        public ExternalHttpResponse postJson(String url, String body, Map<String, String> headers) {
            return new ExternalHttpResponse(404, "");
        }
    }

}
