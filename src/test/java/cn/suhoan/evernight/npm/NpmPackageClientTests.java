package cn.suhoan.evernight.npm;

import static org.assertj.core.api.Assertions.assertThat;

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

}
