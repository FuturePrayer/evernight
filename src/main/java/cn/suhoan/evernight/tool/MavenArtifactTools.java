package cn.suhoan.evernight.tool;


import cn.suhoan.evernight.client.MavenArtifactClient;
import cn.suhoan.evernight.model.MavenArtifactDetail;
import cn.suhoan.evernight.model.MavenArtifactJavaVersion;
import cn.suhoan.evernight.model.MavenArtifactSearchResult;
import cn.suhoan.evernight.model.MavenDependencyDetail;
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

    @Tool(name = "maven_artifact_search", description = "用于 Java/JVM 项目的 Maven Artifact 坐标搜索，支持关键词、groupId、artifactId 组合查询。")
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

    @Tool(name = "maven_artifact_detail", description = "用于 Java/JVM 项目，查询 Maven Artifact 详情，包括最新版本、打包类型、POM 描述、许可证、SCM 和开发者信息。repositoryBaseUrl 可选，传入时必须是 Maven 仓库白名单中的镜像地址。")
    public MavenArtifactDetail detail(
            @ToolParam(description = "Maven groupId，例如 org.springframework.boot") String groupId,
            @ToolParam(description = "Maven artifactId，例如 spring-boot-starter-web") String artifactId,
            @ToolParam(required = false, description = "可选，本次查询使用的 Maven 仓库镜像地址，例如 https://repo1.maven.org/maven2 或 https://maven.aliyun.com/repository/public") String repositoryBaseUrl) {
        log.info("调用 MCP 工具 maven_artifact_detail，groupId={}, artifactId={}, repositoryBaseUrl={}",
                groupId, artifactId, repositoryBaseUrl);
        return mavenArtifactClient.detail(groupId, artifactId, repositoryBaseUrl);
    }

    @Tool(name = "maven_dependency_detail", description = "用于 Java/JVM 项目，查询指定 Maven 坐标和版本的 POM 依赖详情，包括 parent、dependencies 和 dependencyManagement。repositoryBaseUrl 可选，传入时必须是 Maven 仓库白名单中的镜像地址。")
    public MavenDependencyDetail dependencyDetail(
            @ToolParam(description = "Maven groupId，例如 org.springframework.boot") String groupId,
            @ToolParam(description = "Maven artifactId，例如 spring-boot-starter-web") String artifactId,
            @ToolParam(description = "Maven 版本号，例如 3.4.5") String version,
            @ToolParam(required = false, description = "可选，本次查询使用的 Maven 仓库镜像地址，例如 https://repo1.maven.org/maven2 或 https://maven.aliyun.com/repository/public") String repositoryBaseUrl) {
        log.info("调用 MCP 工具 maven_dependency_detail，groupId={}, artifactId={}, version={}, repositoryBaseUrl={}",
                groupId, artifactId, version, repositoryBaseUrl);
        return mavenArtifactClient.dependencyDetail(groupId, artifactId, version, repositoryBaseUrl);
    }

    @Tool(name = "maven_artifact_java_version", description = "用于 Java/JVM 项目，只读取指定 Maven Artifact POM 中声明的 Java 版本字段，包括 maven.compiler.release/source/target、maven-compiler-plugin 和 maven-enforcer-plugin。注意：这些字段表示“目标兼容版本”或“要求的 Java 版本”，不一定是实际执行编译的 JDK。version 可选，留空时查询最新版本。repositoryBaseUrl 可选，传入时必须是 Maven 仓库白名单中的镜像地址。")
    public MavenArtifactJavaVersion javaVersion(
            @ToolParam(description = "Maven groupId，例如 org.springframework.boot") String groupId,
            @ToolParam(description = "Maven artifactId，例如 spring-boot-starter-web") String artifactId,
            @ToolParam(required = false, description = "Maven 版本号；留空使用最新版本") String version,
            @ToolParam(required = false, description = "可选，本次查询使用的 Maven 仓库镜像地址，例如 https://repo1.maven.org/maven2 或 https://maven.aliyun.com/repository/public") String repositoryBaseUrl) {
        log.info("调用 MCP 工具 maven_artifact_java_version，groupId={}, artifactId={}, version={}, repositoryBaseUrl={}",
                groupId, artifactId, version, repositoryBaseUrl);
        return mavenArtifactClient.javaVersion(groupId, artifactId, version, repositoryBaseUrl);
    }

}
