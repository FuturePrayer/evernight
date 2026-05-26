package cn.suhoan.evernight.tool;

import cn.suhoan.evernight.client.CargoCrateClient;
import cn.suhoan.evernight.model.CargoCrateInfo;
import cn.suhoan.evernight.model.CargoCrateSearchResult;
import cn.suhoan.evernight.model.CargoCrateVersionDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

@Service
public class CargoCrateTools {

    private static final Logger log = LoggerFactory.getLogger(CargoCrateTools.class);

    private final CargoCrateClient cargoCrateClient;

    public CargoCrateTools(CargoCrateClient cargoCrateClient) {
        this.cargoCrateClient = cargoCrateClient;
    }

    @Tool(name = "cargo_crate_search", description = "搜索 Cargo/crates.io crate；registryBaseUrl 可选，传入时必须是 Cargo registry API 白名单中的地址。")
    public CargoCrateSearchResult search(
            @ToolParam(description = "搜索关键词，例如 serde") String keyword,
            @ToolParam(required = false, description = "返回条数，默认 20，最大 50") Integer perPage,
            @ToolParam(required = false, description = "可选，本次查询使用的 Cargo registry API 地址，例如 https://crates.io/api/v1") String registryBaseUrl) {
        log.info("调用 MCP 工具 cargo_crate_search，keyword={}, perPage={}, registryBaseUrl={}",
                keyword, perPage, registryBaseUrl);
        return cargoCrateClient.search(keyword, perPage, registryBaseUrl);
    }

    @Tool(name = "cargo_crate_info", description = "查询 Cargo/crates.io crate 元数据，包括最新版本、许可证、仓库、关键词、分类和版本摘要。registryBaseUrl 可选，传入时必须是 Cargo registry API 白名单中的地址。")
    public CargoCrateInfo crateInfo(
            @ToolParam(description = "crate 名称，例如 serde") String crateName,
            @ToolParam(required = false, description = "最多返回多少个版本摘要，默认 20，最大 100") Integer versionLimit,
            @ToolParam(required = false, description = "可选，本次查询使用的 Cargo registry API 地址，例如 https://crates.io/api/v1") String registryBaseUrl) {
        log.info("调用 MCP 工具 cargo_crate_info，crateName={}, versionLimit={}, registryBaseUrl={}",
                crateName, versionLimit, registryBaseUrl);
        return cargoCrateClient.crateInfo(crateName, versionLimit, registryBaseUrl);
    }

    @Tool(name = "cargo_crate_version_detail", description = "查询 Cargo/crates.io crate 指定版本详情和依赖。version 可选，留空时使用最新稳定版本。rustVersion 表示 crate 声明的最低 Rust 版本或兼容要求，不代表实际编译时使用的 rustc 版本。registryBaseUrl 可选，传入时必须是 Cargo registry API 白名单中的地址。")
    public CargoCrateVersionDetail versionDetail(
            @ToolParam(description = "crate 名称，例如 serde") String crateName,
            @ToolParam(required = false, description = "crate 版本号；留空使用最新稳定版本") String version,
            @ToolParam(required = false, description = "可选，本次查询使用的 Cargo registry API 地址，例如 https://crates.io/api/v1") String registryBaseUrl) {
        log.info("调用 MCP 工具 cargo_crate_version_detail，crateName={}, version={}, registryBaseUrl={}",
                crateName, version, registryBaseUrl);
        return cargoCrateClient.versionDetail(crateName, version, registryBaseUrl);
    }

}
