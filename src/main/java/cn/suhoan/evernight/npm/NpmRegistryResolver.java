package cn.suhoan.evernight.npm;

import cn.suhoan.evernight.common.RepositoryWhitelistResolver;
import org.springframework.stereotype.Component;

@Component
public class NpmRegistryResolver {

    private final NpmRegistryProperties properties;

    public NpmRegistryResolver(NpmRegistryProperties properties) {
        this.properties = properties;
    }

    public String resolve(String registryBaseUrl) {
        return RepositoryWhitelistResolver.resolve(registryBaseUrl, properties.getRepositories(), "npm registry");
    }

}
