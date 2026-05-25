package cn.suhoan.evernight.tool;


import cn.suhoan.evernight.client.PypiPackageClient;
import cn.suhoan.evernight.model.PypiPackageInfo;
import cn.suhoan.evernight.model.PypiReleaseFiles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

@Service
public class PypiPackageTools {

    private static final Logger log = LoggerFactory.getLogger(PypiPackageTools.class);

    private final PypiPackageClient pypiPackageClient;

    public PypiPackageTools(PypiPackageClient pypiPackageClient) {
        this.pypiPackageClient = pypiPackageClient;
    }

    @Tool(name = "pypi_package_info", description = "查询 PyPI 包元数据，包括最新版本、Python 版本要求、许可证、项目链接、分类器和版本摘要。repositoryBaseUrl 可选，传入时必须是 PyPI 仓库白名单中的镜像地址。")
    public PypiPackageInfo packageInfo(
            @ToolParam(description = "PyPI 包名，例如 requests") String packageName,
            @ToolParam(required = false, description = "最多返回多少个 release 版本号，默认 20，最大 100") Integer releaseLimit,
            @ToolParam(required = false, description = "可选，本次查询使用的 PyPI 仓库镜像地址，例如 https://pypi.org/pypi 或 https://pypi.tuna.tsinghua.edu.cn/pypi") String repositoryBaseUrl) {
        log.info("调用 MCP 工具 pypi_package_info，packageName={}, releaseLimit={}, repositoryBaseUrl={}",
                packageName, releaseLimit, repositoryBaseUrl);
        return pypiPackageClient.packageInfo(packageName, releaseLimit, repositoryBaseUrl);
    }

    @Tool(name = "pypi_release_files", description = "查询 PyPI 指定 release 的文件列表，包括 wheel/sdist 文件名、Python tag、大小、上传时间、哈希和 yanked 状态。version 可选，留空时使用最新版本。repositoryBaseUrl 可选，传入时必须是 PyPI 仓库白名单中的镜像地址。")
    public PypiReleaseFiles releaseFiles(
            @ToolParam(description = "PyPI 包名，例如 requests") String packageName,
            @ToolParam(required = false, description = "PyPI release 版本号；留空使用最新版本") String version,
            @ToolParam(required = false, description = "可选，本次查询使用的 PyPI 仓库镜像地址，例如 https://pypi.org/pypi 或 https://pypi.tuna.tsinghua.edu.cn/pypi") String repositoryBaseUrl) {
        log.info("调用 MCP 工具 pypi_release_files，packageName={}, version={}, repositoryBaseUrl={}",
                packageName, version, repositoryBaseUrl);
        return pypiPackageClient.releaseFiles(packageName, version, repositoryBaseUrl);
    }

}
