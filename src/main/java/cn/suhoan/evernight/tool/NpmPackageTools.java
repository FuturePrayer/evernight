package cn.suhoan.evernight.tool;


import cn.suhoan.evernight.client.NpmPackageClient;
import cn.suhoan.evernight.model.NpmPackageInfo;
import cn.suhoan.evernight.model.NpmPackageVersionDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

@Service
public class NpmPackageTools {

    private static final Logger log = LoggerFactory.getLogger(NpmPackageTools.class);

    private final NpmPackageClient npmPackageClient;

    public NpmPackageTools(NpmPackageClient npmPackageClient) {
        this.npmPackageClient = npmPackageClient;
    }

    @Tool(name = "npm_package_info", description = "查询 npm 包元数据，包括 latest、dist-tags、许可证、仓库、依赖和版本摘要。registryBaseUrl 可选，传入时必须是 npm registry 白名单中的镜像地址。")
    public NpmPackageInfo packageInfo(
            @ToolParam(description = "npm 包名，例如 react 或 @types/node") String packageName,
            @ToolParam(required = false, description = "最多返回多少个版本号，默认 20，最大 100") Integer versionLimit,
            @ToolParam(required = false, description = "可选，本次查询使用的 npm registry 镜像地址，例如 https://registry.npmjs.org 或 https://registry.npmmirror.com") String registryBaseUrl) {
        log.info("调用 MCP 工具 npm_package_info，packageName={}, versionLimit={}, registryBaseUrl={}",
                packageName, versionLimit, registryBaseUrl);
        return npmPackageClient.packageInfo(packageName, versionLimit, registryBaseUrl);
    }

    @Tool(name = "npm_package_version_detail", description = "查询 npm 指定版本元数据，包括依赖、peerDependencies、engines、bin、deprecated 和 tarball 信息。version 可选，留空时使用 latest。registryBaseUrl 可选，传入时必须是 npm registry 白名单中的镜像地址。")
    public NpmPackageVersionDetail versionDetail(
            @ToolParam(description = "npm 包名，例如 react 或 @types/node") String packageName,
            @ToolParam(required = false, description = "npm 版本号；留空使用 latest") String version,
            @ToolParam(required = false, description = "可选，本次查询使用的 npm registry 镜像地址，例如 https://registry.npmjs.org 或 https://registry.npmmirror.com") String registryBaseUrl) {
        log.info("调用 MCP 工具 npm_package_version_detail，packageName={}, version={}, registryBaseUrl={}",
                packageName, version, registryBaseUrl);
        return npmPackageClient.versionDetail(packageName, version, registryBaseUrl);
    }

}
