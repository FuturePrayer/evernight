package cn.suhoan.evernight.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "npm.registry")
public class NpmRegistryProperties {

    public static final String NPMJS_BASE_URL = "https://registry.npmjs.org";

    private List<String> repositories = defaultRepositories();

    public List<String> getRepositories() {
        return repositories;
    }

    public void setRepositories(List<String> repositories) {
        this.repositories = repositories;
    }

    private static List<String> defaultRepositories() {
        return new ArrayList<>(List.of(NPMJS_BASE_URL, "https://registry.npmmirror.com"));
    }

}
