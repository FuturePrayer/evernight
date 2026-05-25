package cn.suhoan.evernight.npm;

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

}
