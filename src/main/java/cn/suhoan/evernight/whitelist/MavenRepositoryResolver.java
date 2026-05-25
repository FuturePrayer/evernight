package cn.suhoan.evernight.whitelist;


import cn.suhoan.evernight.config.MavenRepositoryProperties;
import cn.suhoan.evernight.whitelist.RepositoryWhitelistResolver;
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
