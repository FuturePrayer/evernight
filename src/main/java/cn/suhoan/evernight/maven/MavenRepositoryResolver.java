package cn.suhoan.evernight.maven;

import cn.suhoan.evernight.common.RepositoryWhitelistResolver;
import org.springframework.stereotype.Component;

@Component
public class MavenRepositoryResolver {

    private final MavenRepositoryProperties properties;

    public MavenRepositoryResolver(MavenRepositoryProperties properties) {
        this.properties = properties;
    }

    public String resolve(String repositoryBaseUrl) {
        return RepositoryWhitelistResolver.resolve(repositoryBaseUrl, properties.getRepositories(), "Maven 仓库");
    }

}
