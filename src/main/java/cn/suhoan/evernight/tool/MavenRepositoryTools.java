package cn.suhoan.evernight.tool;


import cn.suhoan.evernight.client.MavenMetadataClient;
import cn.suhoan.evernight.model.MavenVersionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

@Service
public class MavenRepositoryTools {

    private static final Logger log = LoggerFactory.getLogger(MavenRepositoryTools.class);

    private final MavenMetadataClient mavenMetadataClient;

    public MavenRepositoryTools(MavenMetadataClient mavenMetadataClient) {
        this.mavenMetadataClient = mavenMetadataClient;
    }

    @Tool(name = "maven_latest_version", description = "根据 groupId 和 artifactId 查询 Maven 仓库中的最新版本。repositoryBaseUrl 可选，传入时必须是 Maven 仓库白名单中的镜像地址。")
    public MavenVersionInfo latestVersion(
            @ToolParam(description = "Maven groupId，例如 org.springframework.boot") String groupId,
            @ToolParam(description = "Maven artifactId，例如 spring-boot-starter") String artifactId,
            @ToolParam(required = false, description = "可选，本次查询使用的 Maven 仓库镜像地址，例如 https://repo1.maven.org/maven2 或 https://maven.aliyun.com/repository/public") String repositoryBaseUrl) {
        log.info("调用 MCP 工具 maven_latest_version，groupId={}, artifactId={}, repositoryBaseUrl={}",
                groupId, artifactId, repositoryBaseUrl);
        return mavenMetadataClient.getLatestVersion(groupId, artifactId, repositoryBaseUrl);
    }

    @Tool(name = "maven_version_list", description = "根据 groupId 和 artifactId 查询 Maven 仓库中的全部版本列表。repositoryBaseUrl 可选，传入时必须是 Maven 仓库白名单中的镜像地址。")
    public MavenVersionInfo versionList(
            @ToolParam(description = "Maven groupId，例如 org.springframework.boot") String groupId,
            @ToolParam(description = "Maven artifactId，例如 spring-boot-starter") String artifactId,
            @ToolParam(required = false, description = "可选，本次查询使用的 Maven 仓库镜像地址，例如 https://repo1.maven.org/maven2 或 https://maven.aliyun.com/repository/public") String repositoryBaseUrl) {
        log.info("调用 MCP 工具 maven_version_list，groupId={}, artifactId={}, repositoryBaseUrl={}",
                groupId, artifactId, repositoryBaseUrl);
        return mavenMetadataClient.getVersions(groupId, artifactId, repositoryBaseUrl);
    }

}
