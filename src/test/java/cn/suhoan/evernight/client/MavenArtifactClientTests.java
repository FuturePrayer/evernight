package cn.suhoan.evernight.client;


import cn.suhoan.evernight.client.MavenArtifactClient;
import cn.suhoan.evernight.config.MavenRepositoryProperties;
import cn.suhoan.evernight.http.ExternalHttpClient;
import cn.suhoan.evernight.http.ExternalHttpResponse;
import cn.suhoan.evernight.model.MavenVersionInfo;
import cn.suhoan.evernight.support.JsonSupport;
import cn.suhoan.evernight.support.TestSupport;
import cn.suhoan.evernight.whitelist.MavenRepositoryResolver;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;

import org.junit.jupiter.api.Test;

class MavenArtifactClientTests {

    private final MavenArtifactClient client = new MavenArtifactClient(null, null, cn.suhoan.evernight.support.TestSupport.disabledCacheService());

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
    void parseDependencyPomReadsParentDependenciesAndManagedDependencies() {
        String pom = """
                <project>
                  <parent>
                    <groupId>org.example</groupId>
                    <artifactId>parent</artifactId>
                    <version>1.0.0</version>
                  </parent>
                  <packaging>jar</packaging>
                  <dependencyManagement>
                    <dependencies>
                      <dependency>
                        <groupId>org.managed</groupId>
                        <artifactId>bom</artifactId>
                        <version>2.0.0</version>
                        <type>pom</type>
                        <scope>import</scope>
                      </dependency>
                    </dependencies>
                  </dependencyManagement>
                  <dependencies>
                    <dependency>
                      <groupId>org.example</groupId>
                      <artifactId>api</artifactId>
                      <version>1.2.3</version>
                      <scope>compile</scope>
                      <optional>true</optional>
                    </dependency>
                  </dependencies>
                </project>
                """;

        var detail = client.parseDependencyPom(pom, "org.example", "demo", "1.0.0", "https://repo.example.com");

        assertThat(detail.parent().artifactId()).isEqualTo("parent");
        assertThat(detail.dependencies()).hasSize(1);
        assertThat(detail.dependencies().getFirst().artifactId()).isEqualTo("api");
        assertThat(detail.dependencies().getFirst().optional()).isTrue();
        assertThat(detail.managedDependencies()).hasSize(1);
        assertThat(detail.managedDependencies().getFirst().scope()).isEqualTo("import");
    }

    @Test
    void parseJavaVersionPomReadsPomDeclarationsAndCarriesWarningNote() {
        String pom = """
                <project>
                  <properties>
                    <java.version>21</java.version>
                    <maven.compiler.release>17</maven.compiler.release>
                    <maven.compiler.source>17</maven.compiler.source>
                    <maven.compiler.target>17</maven.compiler.target>
                  </properties>
                  <build>
                    <plugins>
                      <plugin>
                        <artifactId>maven-compiler-plugin</artifactId>
                        <configuration>
                          <release>17</release>
                          <source>17</source>
                          <target>17</target>
                        </configuration>
                      </plugin>
                      <plugin>
                        <artifactId>maven-enforcer-plugin</artifactId>
                        <configuration>
                          <rules>
                            <requireJavaVersion>
                              <version>[17,)</version>
                            </requireJavaVersion>
                          </rules>
                        </configuration>
                      </plugin>
                    </plugins>
                  </build>
                </project>
                """;

        var javaVersion = client.parseJavaVersionPom(pom, "org.example", "demo", "1.0.0", "https://repo.example.com");

        assertThat(javaVersion.mavenCompilerRelease()).isEqualTo("17");
        assertThat(javaVersion.compilerPluginRelease()).isEqualTo("17");
        assertThat(javaVersion.enforcerJavaVersion()).isEqualTo("[17,)");
        assertThat(javaVersion.relatedProperties()).containsEntry("java.version", "21");
        assertThat(javaVersion.note())
                .contains("目标兼容版本")
                .contains("不一定是实际执行编译的 JDK");
    }

    @Test
    void detailFallsBackToParentPomWithoutOverridingCurrentUrl() {
        MavenArtifactClient client = new MavenArtifactClient(new PomHttpClient(Map.of(
                "https://repo1.maven.org/maven2/org/example/demo/1.0.0/demo-1.0.0.pom", """
                        <project>
                          <parent>
                            <groupId>org.example</groupId>
                            <artifactId>parent</artifactId>
                            <version>1.0.0</version>
                          </parent>
                          <url>https://child.example.com</url>
                        </project>
                        """,
                "https://repo1.maven.org/maven2/org/example/parent/1.0.0/parent-1.0.0.pom", """
                        <project>
                          <url>https://parent.example.com</url>
                          <licenses>
                            <license>
                              <name>Apache-2.0</name>
                              <url>https://www.apache.org/licenses/LICENSE-2.0</url>
                            </license>
                          </licenses>
                        </project>
                        """
        )), new JsonSupport(), TestSupport.disabledCacheService(), new MavenRepositoryResolver(new MavenRepositoryProperties()),
                fixedMetadataClient());

        var detail = client.doDetail("org.example", "demo", "https://repo1.maven.org/maven2");

        assertThat(detail.url()).isEqualTo("https://child.example.com");
        assertThat(detail.licenses()).hasSize(1);
        assertThat(detail.inheritedFromParent()).doesNotContainKey("url");
        assertThat(detail.inheritedFromParent().get("licenses")).contains("继承自父 POM");
    }

    @Test
    void javaVersionFallsBackToParentPomForMissingFields() {
        MavenArtifactClient client = new MavenArtifactClient(new PomHttpClient(Map.of(
                "https://repo1.maven.org/maven2/org/example/demo/1.0.0/demo-1.0.0.pom", """
                        <project>
                          <parent>
                            <groupId>org.example</groupId>
                            <artifactId>parent</artifactId>
                            <version>1.0.0</version>
                          </parent>
                          <properties>
                            <maven.compiler.source>11</maven.compiler.source>
                          </properties>
                        </project>
                        """,
                "https://repo1.maven.org/maven2/org/example/parent/1.0.0/parent-1.0.0.pom", """
                        <project>
                          <properties>
                            <java.version>17</java.version>
                            <maven.compiler.release>17</maven.compiler.release>
                            <maven.compiler.target>17</maven.compiler.target>
                          </properties>
                        </project>
                        """
        )), new JsonSupport(), TestSupport.disabledCacheService(), new MavenRepositoryResolver(new MavenRepositoryProperties()), null);

        var javaVersion = client.doJavaVersion("org.example", "demo", "1.0.0", "https://repo1.maven.org/maven2");

        assertThat(javaVersion.mavenCompilerSource()).isEqualTo("11");
        assertThat(javaVersion.mavenCompilerRelease()).isEqualTo("17");
        assertThat(javaVersion.mavenCompilerTarget()).isEqualTo("17");
        assertThat(javaVersion.inheritedFromParent()).doesNotContainKey("mavenCompilerSource");
        assertThat(javaVersion.inheritedFromParent().get("mavenCompilerRelease")).contains("继承自父 POM");
        assertThat(javaVersion.inheritedFromParent().get("relatedProperties.java.version")).contains("继承自父 POM");
    }

    @Test
    void buildPomUrlUsesRepositoryPath() {
        assertThat(MavenArtifactClient.buildPomUrl("https://repo.example.com", "org.example", "demo", "1.0.0"))
                .isEqualTo("https://repo.example.com/org/example/demo/1.0.0/demo-1.0.0.pom");
    }

    @Test
    void repositorySearchRejectsKeywordSearch() {
        assertThatThrownBy(() -> client.doSearch("spring", null, null, 10, "https://maven.aliyun.com/repository/public"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("关键词搜索");
    }

    private record PomHttpClient(Map<String, String> responses) implements ExternalHttpClient {
        @Override
        public ExternalHttpResponse get(String url, Map<String, String> headers) {
            return new ExternalHttpResponse(responses.containsKey(url) ? 200 : 404, responses.getOrDefault(url, ""));
        }

        @Override
        public ExternalHttpResponse postJson(String url, String body, Map<String, String> headers) {
            return new ExternalHttpResponse(404, "");
        }
    }

    private static MavenMetadataClient fixedMetadataClient() {
        return new MavenMetadataClient(new MavenRepositoryResolver(new MavenRepositoryProperties()), new PomHttpClient(Map.of())) {
            @Override
            public MavenVersionInfo getVersions(String groupId, String artifactId, String repositoryBaseUrl) {
                return new MavenVersionInfo(groupId, artifactId, repositoryBaseUrl, "1.0.0", java.util.List.of("1.0.0"));
            }
        };
    }

}
