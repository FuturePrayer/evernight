package cn.suhoan.evernight.whitelist;


import cn.suhoan.evernight.whitelist.RepositoryWhitelistResolver;
import java.util.List;

import cn.suhoan.evernight.config.MavenRepositoryProperties;
import cn.suhoan.evernight.whitelist.MavenRepositoryResolver;
import cn.suhoan.evernight.config.NpmRegistryProperties;
import cn.suhoan.evernight.whitelist.NpmRegistryResolver;
import cn.suhoan.evernight.config.PypiRepositoryProperties;
import cn.suhoan.evernight.whitelist.PypiRepositoryResolver;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@EnableConfigurationProperties({ MavenRepositoryProperties.class, NpmRegistryProperties.class, PypiRepositoryProperties.class })
public class RepositoryWhitelistController {

    private final MavenRepositoryProperties mavenRepositoryProperties;

    private final MavenRepositoryResolver mavenRepositoryResolver;

    private final NpmRegistryProperties npmRegistryProperties;

    private final NpmRegistryResolver npmRegistryResolver;

    private final PypiRepositoryProperties pypiRepositoryProperties;

    private final PypiRepositoryResolver pypiRepositoryResolver;

    public RepositoryWhitelistController(MavenRepositoryProperties mavenRepositoryProperties,
            MavenRepositoryResolver mavenRepositoryResolver, NpmRegistryProperties npmRegistryProperties,
            NpmRegistryResolver npmRegistryResolver, PypiRepositoryProperties pypiRepositoryProperties,
            PypiRepositoryResolver pypiRepositoryResolver) {
        this.mavenRepositoryProperties = mavenRepositoryProperties;
        this.mavenRepositoryResolver = mavenRepositoryResolver;
        this.npmRegistryProperties = npmRegistryProperties;
        this.npmRegistryResolver = npmRegistryResolver;
        this.pypiRepositoryProperties = pypiRepositoryProperties;
        this.pypiRepositoryResolver = pypiRepositoryResolver;
    }

    @GetMapping("/api/repositories")
    public RepositoryWhitelistResponse repositories() {
        return new RepositoryWhitelistResponse(
                new RepositoryGroup(mavenRepositoryResolver.resolve(null),
                        RepositoryWhitelistResolver.normalizeRepositories(mavenRepositoryProperties.getRepositories())),
                new RepositoryGroup(npmRegistryResolver.resolve(null),
                        RepositoryWhitelistResolver.normalizeRepositories(npmRegistryProperties.getRepositories())),
                new RepositoryGroup(pypiRepositoryResolver.resolve(null),
                        RepositoryWhitelistResolver.normalizeRepositories(pypiRepositoryProperties.getRepositories())));
    }

    public record RepositoryWhitelistResponse(
            RepositoryGroup maven,
            RepositoryGroup npm,
            RepositoryGroup pypi) {
    }

    public record RepositoryGroup(
            String defaultBaseUrl,
            List<String> repositories) {
    }

}
