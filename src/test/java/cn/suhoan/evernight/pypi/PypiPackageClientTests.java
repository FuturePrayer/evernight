package cn.suhoan.evernight.pypi;

import static org.assertj.core.api.Assertions.assertThat;

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

}
