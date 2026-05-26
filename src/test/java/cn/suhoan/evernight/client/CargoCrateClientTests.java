package cn.suhoan.evernight.client;

import cn.suhoan.evernight.config.CargoRegistryProperties;
import cn.suhoan.evernight.http.ExternalHttpClient;
import cn.suhoan.evernight.http.ExternalHttpResponse;
import cn.suhoan.evernight.support.JsonSupport;
import cn.suhoan.evernight.support.TestSupport;
import cn.suhoan.evernight.whitelist.CargoRegistryResolver;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class CargoCrateClientTests {

    @Test
    void buildUrlsUseRegistryBaseUrlAndEncodeArguments() {
        assertThat(CargoCrateClient.buildSearchUrl("https://crates.io/api/v1", "serde json", 10))
                .isEqualTo("https://crates.io/api/v1/crates?q=serde%20json&per_page=10");
        assertThat(CargoCrateClient.buildCrateUrl("https://crates.io/api/v1", "serde"))
                .isEqualTo("https://crates.io/api/v1/crates/serde");
        assertThat(CargoCrateClient.buildVersionUrl("https://crates.io/api/v1", "serde", "1.0.0"))
                .isEqualTo("https://crates.io/api/v1/crates/serde/1.0.0");
        assertThat(CargoCrateClient.buildDependenciesUrl("https://crates.io/api/v1", "serde", "1.0.0"))
                .isEqualTo("https://crates.io/api/v1/crates/serde/1.0.0/dependencies");
    }

    @Test
    void searchClampsPerPageToMaximum() {
        RecordingHttpClient httpClient = new RecordingHttpClient(Map.of(
                "https://crates.io/api/v1/crates?q=serde&per_page=50", """
                        {
                          "meta": { "total": 1 },
                          "crates": [
                            {
                              "name": "serde",
                              "max_version": "1.0.0",
                              "description": "Serialization",
                              "downloads": 100,
                              "recent_downloads": 10,
                              "num_versions": 1
                            }
                          ]
                        }
                        """));
        CargoCrateClient client = newClient(httpClient);

        var result = client.search("serde", 500, null);

        assertThat(result.total()).isEqualTo(1);
        assertThat(result.crates()).hasSize(1);
        assertThat(httpClient.urls()).containsExactly("https://crates.io/api/v1/crates?q=serde&per_page=50");
    }

    @Test
    void crateInfoClampsVersionLimit() {
        RecordingHttpClient httpClient = new RecordingHttpClient(Map.of(
                "https://crates.io/api/v1/crates/serde", """
                        {
                          "crate": {
                            "name": "serde",
                            "max_version": "1.0.0",
                            "max_stable_version": "1.0.0",
                            "description": "Serialization",
                            "downloads": 100,
                            "recent_downloads": 10
                          },
                          "versions": [
                            { "num": "1.0.0", "yanked": false },
                            { "num": "0.9.0", "yanked": false }
                          ],
                          "keywords": [{ "keyword": "serde" }],
                          "categories": [{ "category": "encoding" }]
                        }
                        """));
        CargoCrateClient client = newClient(httpClient);

        var info = client.crateInfo("serde", 1, null);

        assertThat(info.versions()).extracting("version").containsExactly("1.0.0");
        assertThat(info.keywords()).containsExactly("serde");
        assertThat(info.categories()).containsExactly("encoding");
    }

    @Test
    void versionDetailUsesLatestStableVersionWhenVersionIsBlankAndFetchesDependencies() {
        RecordingHttpClient httpClient = new RecordingHttpClient(Map.of(
                "https://crates.io/api/v1/crates/serde", """
                        {
                          "crate": { "name": "serde", "max_version": "1.0.0", "max_stable_version": "1.0.0" },
                          "versions": []
                        }
                        """,
                "https://crates.io/api/v1/crates/serde/1.0.0", """
                        {
                          "version": {
                            "crate": "serde",
                            "num": "1.0.0",
                            "rust_version": "1.56",
                            "edition": "2021",
                            "license": "MIT OR Apache-2.0",
                            "features": { "default": ["std"] },
                            "yanked": false
                          },
                          "dependencies": null
                        }
                        """,
                "https://crates.io/api/v1/crates/serde/1.0.0/dependencies", """
                        {
                          "dependencies": [
                            {
                              "crate_id": "serde_derive",
                              "req": "^1",
                              "kind": "normal",
                              "optional": true,
                              "default_features": true,
                              "features": []
                            }
                          ]
                        }
                        """));
        CargoCrateClient client = newClient(httpClient);

        var detail = client.versionDetail("serde", null, null);

        assertThat(detail.version()).isEqualTo("1.0.0");
        assertThat(detail.rustVersion()).isEqualTo("1.56");
        assertThat(detail.dependencies()).hasSize(1);
        assertThat(detail.dependencies().getFirst().crateName()).isEqualTo("serde_derive");
        assertThat(detail.note()).contains("不代表实际编译时使用的 rustc 版本");
        assertThat(httpClient.urls()).containsExactly(
                "https://crates.io/api/v1/crates/serde",
                "https://crates.io/api/v1/crates/serde/1.0.0",
                "https://crates.io/api/v1/crates/serde/1.0.0/dependencies");
    }

    private static CargoCrateClient newClient(RecordingHttpClient httpClient) {
        return new CargoCrateClient(httpClient, new JsonSupport(), TestSupport.disabledCacheService(),
                new CargoRegistryResolver(new CargoRegistryProperties()));
    }

    private static final class RecordingHttpClient implements ExternalHttpClient {

        private final Map<String, String> responses;

        private final List<String> urls = new ArrayList<>();

        private RecordingHttpClient(Map<String, String> responses) {
            this.responses = responses;
        }

        @Override
        public ExternalHttpResponse get(String url, Map<String, String> headers) {
            urls.add(url);
            String body = responses.get(url);
            return body == null ? new ExternalHttpResponse(404, "") : new ExternalHttpResponse(200, body);
        }

        @Override
        public ExternalHttpResponse postJson(String url, String body, Map<String, String> headers) {
            return new ExternalHttpResponse(404, "");
        }

        private List<String> urls() {
            return urls;
        }

    }

}
