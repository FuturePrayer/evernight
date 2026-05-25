package cn.suhoan.evernight.client;


import cn.suhoan.evernight.client.NpmPackageClient;
import cn.suhoan.evernight.config.NpmRegistryProperties;
import cn.suhoan.evernight.http.ExternalHttpClient;
import cn.suhoan.evernight.http.ExternalHttpResponse;
import cn.suhoan.evernight.support.JsonSupport;
import cn.suhoan.evernight.support.TestSupport;
import cn.suhoan.evernight.whitelist.NpmRegistryResolver;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;

class NpmPackageClientTests {

    @Test
    void encodePackageNameEncodesScopedPackage() {
        assertThat(NpmPackageClient.encodePackageName("@types/node")).isEqualTo("%40types%2Fnode");
    }

    @Test
    void buildPackageUrlUsesRegistryBaseUrl() {
        assertThat(NpmPackageClient.buildPackageUrl("https://registry.npmmirror.com", "@types/node"))
                .isEqualTo("https://registry.npmmirror.com/%40types%2Fnode");
    }

    @Test
    void versionDetailUsesLatestWhenVersionIsBlank() {
        NpmRegistryProperties properties = new NpmRegistryProperties();
        NpmPackageClient client = new NpmPackageClient(new JsonHttpClient("""
                {
                  "dist-tags": { "latest": "1.0.0" },
                  "versions": {
                    "1.0.0": {
                      "name": "demo",
                      "version": "1.0.0",
                      "dependencies": { "left-pad": "^1.3.0" },
                      "bin": "cli.js",
                      "dist": { "tarball": "https://registry.example/demo.tgz", "shasum": "abc" }
                    }
                  }
                }
                """), new JsonSupport(), TestSupport.disabledCacheService(), new NpmRegistryResolver(properties));

        var detail = client.versionDetail("demo", null, null);

        assertThat(detail.version()).isEqualTo("1.0.0");
        assertThat(detail.dependencies()).containsEntry("left-pad", "^1.3.0");
        assertThat(detail.bin()).containsEntry("demo", "cli.js");
        assertThat(detail.tarball()).isEqualTo("https://registry.example/demo.tgz");
    }

    private record JsonHttpClient(String body) implements ExternalHttpClient {
        @Override
        public ExternalHttpResponse get(String url, Map<String, String> headers) {
            return new ExternalHttpResponse(200, body);
        }

        @Override
        public ExternalHttpResponse postJson(String url, String body, Map<String, String> headers) {
            return new ExternalHttpResponse(404, "");
        }
    }

}
