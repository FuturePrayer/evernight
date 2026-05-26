package cn.suhoan.evernight.whitelist;

import cn.suhoan.evernight.config.CargoRegistryProperties;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;

class CargoRegistryResolverTests {

    @Test
    void resolveUsesFirstRegistryWhenRequestIsBlank() {
        CargoRegistryProperties properties = new CargoRegistryProperties();
        properties.setRepositories(List.of("https://mirror.example/api/v1", "https://crates.io/api/v1"));
        CargoRegistryResolver resolver = new CargoRegistryResolver(properties);

        assertThat(resolver.resolve(null)).isEqualTo("https://mirror.example/api/v1");
    }

    @Test
    void resolveAcceptsWhitelistedRegistryUrlAndNormalizesTrailingSlash() {
        CargoRegistryResolver resolver = new CargoRegistryResolver(new CargoRegistryProperties());

        assertThat(resolver.resolve("https://crates.io/api/v1/")).isEqualTo("https://crates.io/api/v1");
    }

    @Test
    void resolveRejectsUnknownRegistryUrl() {
        CargoRegistryResolver resolver = new CargoRegistryResolver(new CargoRegistryProperties());

        assertThatThrownBy(() -> resolver.resolve("https://evil.example/api/v1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("不允许访问");
    }

}
