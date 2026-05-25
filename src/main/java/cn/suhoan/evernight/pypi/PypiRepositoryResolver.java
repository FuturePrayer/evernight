package cn.suhoan.evernight.pypi;

import cn.suhoan.evernight.common.RepositoryWhitelistResolver;
import org.springframework.stereotype.Component;

@Component
public class PypiRepositoryResolver {

    private final PypiRepositoryProperties properties;

    public PypiRepositoryResolver(PypiRepositoryProperties properties) {
        this.properties = properties;
    }

    public String resolve(String repositoryBaseUrl) {
        return RepositoryWhitelistResolver.resolve(repositoryBaseUrl, properties.getRepositories(), "PyPI 仓库");
    }

}
