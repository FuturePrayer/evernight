package cn.suhoan.evernight.maven;

import cn.suhoan.evernight.npm.NpmPackageTools;
import cn.suhoan.evernight.osv.OsvVulnerabilityTools;
import cn.suhoan.evernight.pypi.PypiPackageTools;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpToolConfiguration {

    @Bean
    ToolCallbackProvider toolCallbackProvider(MavenRepositoryTools mavenRepositoryTools,
            MavenArtifactTools mavenArtifactTools,
            NpmPackageTools npmPackageTools,
            PypiPackageTools pypiPackageTools,
            OsvVulnerabilityTools osvVulnerabilityTools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(mavenRepositoryTools, mavenArtifactTools, npmPackageTools, pypiPackageTools, osvVulnerabilityTools)
                .build();
    }

}
