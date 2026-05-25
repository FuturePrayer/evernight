package cn.suhoan.evernight.maven;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

@Service
public class MavenArtifactTools {

    private static final Logger log = LoggerFactory.getLogger(MavenArtifactTools.class);

    private final MavenArtifactClient mavenArtifactClient;

    public MavenArtifactTools(MavenArtifactClient mavenArtifactClient) {
        this.mavenArtifactClient = mavenArtifactClient;
    }

    @Tool(name = "maven_artifact_search", description = "搜索 Maven Artifact 坐标，支持关键词、groupId、artifactId 组合查询。")
    public MavenArtifactSearchResult search(
            @ToolParam(required = false, description = "搜索关键词，例如 spring boot") String keyword,
            @ToolParam(required = false, description = "可选 Maven groupId，例如 org.springframework.boot") String groupId,
            @ToolParam(required = false, description = "可选 Maven artifactId，例如 spring-boot-starter-web") String artifactId,
            @ToolParam(required = false, description = "返回条数，默认 10，最大 50") Integer rows,
            @ToolParam(required = false, description = "可选，本次查询使用的 Maven 仓库镜像地址，例如 https://repo1.maven.org/maven2 或 https://maven.aliyun.com/repository/public。非 Maven Central 镜像仅支持 groupId + artifactId 精确查询") String repositoryBaseUrl) {
        log.info("调用 MCP 工具 maven_artifact_search，keyword={}, groupId={}, artifactId={}, rows={}, repositoryBaseUrl={}",
                keyword, groupId, artifactId, rows, repositoryBaseUrl);
        return mavenArtifactClient.search(keyword, groupId, artifactId, rows, repositoryBaseUrl);
    }

    @Tool(name = "maven_artifact_detail", description = "查询 Maven Artifact 详情，包括最新版本、打包类型、POM 描述、许可证、SCM 和开发者信息。repositoryBaseUrl 可选，传入时必须是 Maven 仓库白名单中的镜像地址。")
    public MavenArtifactDetail detail(
            @ToolParam(description = "Maven groupId，例如 org.springframework.boot") String groupId,
            @ToolParam(description = "Maven artifactId，例如 spring-boot-starter-web") String artifactId,
            @ToolParam(required = false, description = "可选，本次查询使用的 Maven 仓库镜像地址，例如 https://repo1.maven.org/maven2 或 https://maven.aliyun.com/repository/public") String repositoryBaseUrl) {
        log.info("调用 MCP 工具 maven_artifact_detail，groupId={}, artifactId={}, repositoryBaseUrl={}",
                groupId, artifactId, repositoryBaseUrl);
        return mavenArtifactClient.detail(groupId, artifactId, repositoryBaseUrl);
    }

}
