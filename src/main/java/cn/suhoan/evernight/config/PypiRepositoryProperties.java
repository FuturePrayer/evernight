package cn.suhoan.evernight.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pypi.repository")
public class PypiRepositoryProperties {

    public static final String PYPI_BASE_URL = "https://pypi.org/pypi";

    private List<String> repositories = defaultRepositories();

    public List<String> getRepositories() {
        return repositories;
    }

    public void setRepositories(List<String> repositories) {
        this.repositories = repositories;
    }

    private static List<String> defaultRepositories() {
        return new ArrayList<>(List.of(PYPI_BASE_URL, "https://pypi.tuna.tsinghua.edu.cn/pypi"));
    }

}
