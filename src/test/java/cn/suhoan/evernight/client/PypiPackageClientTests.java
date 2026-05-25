package cn.suhoan.evernight.client;


import cn.suhoan.evernight.client.PypiPackageClient;
import cn.suhoan.evernight.config.PypiRepositoryProperties;
import cn.suhoan.evernight.http.ExternalHttpClient;
import cn.suhoan.evernight.http.ExternalHttpResponse;
import cn.suhoan.evernight.support.JsonSupport;
import cn.suhoan.evernight.support.TestSupport;
import cn.suhoan.evernight.whitelist.PypiRepositoryResolver;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;

class PypiPackageClientTests {

    @Test
    void encodePackageNameEncodesSpaces() {
        assertThat(PypiPackageClient.encode("some package")).isEqualTo("some%20package");
    }

    @Test
    void buildPackageUrlUsesRepositoryBaseUrl() {
        assertThat(PypiPackageClient.buildPackageUrl("https://pypi.tuna.tsinghua.edu.cn/pypi", "some package"))
                .isEqualTo("https://pypi.tuna.tsinghua.edu.cn/pypi/some%20package/json");
    }

    @Test
    void releaseFilesUsesLatestVersionWhenVersionIsBlank() {
        PypiRepositoryProperties properties = new PypiRepositoryProperties();
        PypiPackageClient client = new PypiPackageClient(new JsonHttpClient("""
                {
                  "info": { "name": "demo", "version": "1.0.0" },
                  "releases": {
                    "1.0.0": [
                      {
                        "filename": "demo-1.0.0-py3-none-any.whl",
                        "packagetype": "bdist_wheel",
                        "python_version": "py3",
                        "url": "https://files.example/demo.whl",
                        "size": 1234,
                        "upload_time_iso_8601": "2026-01-01T00:00:00Z",
                        "digests": { "sha256": "sha", "md5": "md5", "blake2b_256": "b2" },
                        "requires_python": ">=3.10",
                        "yanked": true,
                        "yanked_reason": "bad wheel"
                      }
                    ]
                  }
                }
                """), new JsonSupport(), TestSupport.disabledCacheService(), new PypiRepositoryResolver(properties));

        var files = client.releaseFiles("demo", null, null);

        assertThat(files.version()).isEqualTo("1.0.0");
        assertThat(files.files()).hasSize(1);
        assertThat(files.files().getFirst().sha256Digest()).isEqualTo("sha");
        assertThat(files.files().getFirst().yanked()).isTrue();
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
