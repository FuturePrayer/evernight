package cn.suhoan.evernight.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cargo.registry")
public class CargoRegistryProperties {

    public static final String CRATES_IO_API_BASE_URL = "https://crates.io/api/v1";

    private List<String> repositories = defaultRepositories();

    public List<String> getRepositories() {
        return repositories;
    }

    public void setRepositories(List<String> repositories) {
        this.repositories = repositories;
    }

    private static List<String> defaultRepositories() {
        return new ArrayList<>(List.of(CRATES_IO_API_BASE_URL));
    }

}
