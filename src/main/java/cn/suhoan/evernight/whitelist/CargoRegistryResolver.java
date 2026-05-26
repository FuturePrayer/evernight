package cn.suhoan.evernight.whitelist;

import cn.suhoan.evernight.config.CargoRegistryProperties;
import org.springframework.stereotype.Component;

@Component
public class CargoRegistryResolver {

    private final CargoRegistryProperties properties;

    public CargoRegistryResolver(CargoRegistryProperties properties) {
        this.properties = properties;
    }

    public String resolve(String registryBaseUrl) {
        return RepositoryWhitelistResolver.resolve(registryBaseUrl, properties.getRepositories(), "Cargo registry API");
    }

}
