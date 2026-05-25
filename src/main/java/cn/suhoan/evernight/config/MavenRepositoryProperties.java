package cn.suhoan.evernight.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "maven.repository")
public class MavenRepositoryProperties {

    public static final String MAVEN_CENTRAL_BASE_URL = "https://repo1.maven.org/maven2";

    /**
     * 允许访问的 Maven 仓库白名单，避免公共服务被用作 SSRF 跳板。
     * 第一个地址作为默认仓库。
     */
    private List<String> repositories = defaultRepositories();

    public List<String> getRepositories() {
        return repositories;
    }

    public void setRepositories(List<String> repositories) {
        this.repositories = repositories;
    }

    private static List<String> defaultRepositories() {
        return new ArrayList<>(List.of(MAVEN_CENTRAL_BASE_URL, "https://maven.aliyun.com/repository/public"));
    }

}
