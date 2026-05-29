<p align="center">
  <img src="src/main/resources/static/icon.svg" alt="Evernight logo" width="96" height="96">
</p>

<h1 align="center">Evernight MCP Server</h1>

<p align="center">
  面向智能体的依赖版本、包元数据和漏洞情报 MCP 服务。
</p>

<p align="center">
  <a href="README_EN.md">English</a> ·
  <a href="#快速开始">快速开始</a> ·
  <a href="#docker-compose">Docker Compose</a> ·
  <a href="#许可证">许可证</a>
</p>

## 简介

Evernight 是一个基于 Spring AI MCP Server WebMVC 的 streamable HTTP MCP 服务。它把 Maven、npm、PyPI、Cargo 和 OSV 的常用查询封装成结构化 MCP 工具，适合在 vibe coding、依赖升级、漏洞排查和自动化研发工作流中使用。

服务默认暴露：

- MCP endpoint：`/mcp`
- 工具首页：`/`
- 当前镜像白名单：`/api/repositories`
- 健康检查：`/actuator/health`

## 功能特性

- 支持 Maven、npm、PyPI、Cargo 包信息查询。
- 支持 OSV 已知漏洞查询，包括 Maven、npm、PyPI 和 Cargo ecosystem。
- Maven、npm、PyPI、Cargo 镜像地址均通过白名单校验，降低 SSRF 风险。
- 使用 Caffeine 缓存上游查询结果，可按生态配置 TTL。
- 支持 HTTP/HTTPS/SOCKS5 代理访问上游服务。
- 支持按客户端 IP 限流。
- 提供统一错误响应，便于智能体稳定处理失败。
- 内置静态首页，展示工具目录、参数、示例、服务状态和镜像白名单。

## 工具列表

| 工具 | 说明 |
| --- | --- |
| `maven_latest_version` | 用于 Java/JVM 项目的 Maven 依赖坐标，根据 `groupId` 和 `artifactId` 查询 Maven 仓库最新版本。 |
| `maven_version_list` | 用于 Java/JVM 项目的 Maven 依赖坐标，查询 Maven 仓库中的完整版本列表。 |
| `maven_artifact_search` | 用于 Java/JVM 项目，搜索 Maven Artifact；非 Maven Central 镜像仅支持 `groupId` + `artifactId` 精确查询。 |
| `maven_artifact_detail` | 用于 Java/JVM 项目，查询 Maven Artifact 详情，包括 POM 描述、许可证、SCM 和开发者信息。 |
| `maven_dependency_detail` | 用于 Java/JVM 项目，查询指定 Maven 坐标和版本的 POM 依赖详情，包括 parent、dependencies 和 dependencyManagement。 |
| `maven_artifact_java_version` | 用于 Java/JVM 项目，只读取 Maven Artifact POM 中声明的 Java 版本字段；这些字段表示“目标兼容版本”或“要求的 Java 版本”，不一定是实际执行编译的 JDK。 |
| `npm_package_info` | 用于 JavaScript/TypeScript/Node.js 项目的 npm 包，查询元数据，包括 `latest`、`dist-tags`、许可证、仓库、依赖和版本摘要。 |
| `npm_package_version_detail` | 用于 JavaScript/TypeScript/Node.js 项目的 npm 包，查询指定版本元数据，包括依赖、peerDependencies、engines、bin、deprecated 和 tarball 信息。 |
| `pypi_package_info` | 用于 Python 项目的 PyPI 包，查询元数据，包括最新版本、Python 版本要求、许可证、项目链接和 release 摘要。 |
| `pypi_release_files` | 用于 Python 项目的 PyPI 包，查询指定 release 的 wheel/sdist 文件、哈希、大小、上传时间和 yanked 状态。 |
| `cargo_crate_search` | 用于 Rust 项目的 Cargo/crates.io crate 搜索；`registryBaseUrl` 可选，传入时必须是 Cargo registry API 白名单地址。 |
| `cargo_crate_info` | 用于 Rust 项目的 Cargo/crates.io crate，查询元数据，包括最新版本、许可证、仓库、关键词、分类和版本摘要。 |
| `cargo_crate_version_detail` | 用于 Rust 项目的 Cargo/crates.io crate，查询指定版本详情和依赖；`rustVersion` 表示 crate 声明的最低 Rust 版本或兼容要求，不代表实际编译时使用的 rustc 版本。 |
| `osv_vulnerability_lookup` | 跨生态安全工具，通过 OSV 查询已知漏洞；Maven=Java/JVM，npm=JavaScript/TypeScript/Node.js，PyPI=Python，Cargo/crates.io=Rust。 |
| `osv_batch_vulnerability_lookup` | 跨生态安全工具，批量查询 OSV 已知漏洞；Maven=Java/JVM，npm=JavaScript/TypeScript/Node.js，PyPI=Python，Cargo/crates.io=Rust，最多 50 个包。 |

## 快速开始

环境要求：

- JDK 25+
- Maven 3.9+

本地运行：

```powershell
$env:JAVA_HOME='D:\devProgram\jdk\jdk-26'
$env:Path='D:\devProgram\jdk\jdk-26\bin;D:\devProgram\apache-maven-3.9.6\bin;' + $env:Path
D:\devProgram\apache-maven-3.9.6\bin\mvn.cmd '-Dmaven.repo.local=D:\jarLibrary' spring-boot:run
```

访问：

- 首页：`http://localhost:25924/`
- MCP：`http://localhost:25924/mcp`
- 健康检查：`http://localhost:25924/actuator/health`

## Docker Compose

项目包含从源码构建的 `Dockerfile` 和 `docker-compose.yml`。

```bash
docker compose up --build -d
```

查看状态：

```bash
docker compose ps
curl http://localhost:25924/actuator/health
```

停止服务：

```bash
docker compose down
```

## 配置

### 镜像白名单

工具入参中的 `repositoryBaseUrl` 或 `registryBaseUrl` 都是可选参数。传入时必须是对应生态白名单中的完整镜像地址；不传时使用逗号分隔列表中的第一个地址作为默认镜像。

Cargo 只支持 crates.io API 兼容地址，例如 `https://crates.io/api/v1`。只提供 Cargo sparse index 的镜像地址不兼容本项目的 Cargo 查询工具。

### 常用环境变量

| 变量名 | 默认值 | 说明 |
| --- | --- | --- |
| `SERVER_PORT` | `25924` | HTTP 服务端口。 |
| `MAVEN_REPOSITORY_REPOSITORIES` | `https://repo1.maven.org/maven2,https://maven.aliyun.com/repository/public` | Maven 仓库白名单，逗号分隔，第一项为默认仓库。 |
| `NPM_REGISTRY_REPOSITORIES` | `https://registry.npmjs.org,https://registry.npmmirror.com` | npm registry 白名单，逗号分隔，第一项为默认 registry。 |
| `PYPI_REPOSITORY_REPOSITORIES` | `https://pypi.org/pypi,https://pypi.tuna.tsinghua.edu.cn/pypi` | PyPI JSON API 白名单，逗号分隔，第一项为默认仓库。 |
| `CARGO_REGISTRY_REPOSITORIES` | `https://crates.io/api/v1` | Cargo registry API 白名单，逗号分隔，第一项为默认 registry API；仅支持 crates.io API 兼容地址。 |
| `EVERNIGHT_HTTP_PROXY_URL` | 空 | 上游代理地址，支持 `http://127.0.0.1:7890`、`https://127.0.0.1:7890`、`socks5://127.0.0.1:1080`。 |
| `EVERNIGHT_HTTP_CONNECT_TIMEOUT_MILLIS` | `5000` | 上游 HTTP 连接超时时间，单位毫秒。 |
| `EVERNIGHT_HTTP_READ_TIMEOUT_MILLIS` | `10000` | 上游 HTTP 读取超时时间，单位毫秒。 |
| `EVERNIGHT_RATE_LIMIT_ENABLED` | `true` | 是否启用 IP 限流。 |
| `EVERNIGHT_RATE_LIMIT_CLIENT_IP_HEADER` | `X-Evernight-Client-IP` | CDN 或网关注入的真实客户端 IP 请求头。 |
| `EVERNIGHT_RATE_LIMIT_MAX_REQUESTS` | `120` | 每个客户端 IP 在限流窗口内允许的最大请求数。 |
| `EVERNIGHT_RATE_LIMIT_WINDOW_SECONDS` | `60` | 限流窗口长度，单位秒。 |
| `EVERNIGHT_ONLINE_USERS_ENABLED` | `true` | 是否启用在线人数统计。 |
| `EVERNIGHT_ONLINE_USERS_WINDOW_SECONDS` | `300` | 在线人数活跃窗口长度，单位秒。 |
| `EVERNIGHT_ONLINE_USERS_MAXIMUM_SIZE` | `100000` | 在线人数统计最多保留的客户端标识数量。 |
| `EVERNIGHT_CACHE_ENABLED` | `true` | 是否启用上游查询缓存。 |
| `EVERNIGHT_CACHE_MAXIMUM_SIZE` | `10000` | 查询缓存最大条目数。 |

### HTTP 代理

```yaml
evernight:
  http:
    proxy-url: # http://127.0.0.1:7890, https://127.0.0.1:7890, socks5://127.0.0.1:1080
    connect-timeout-millis: 5000
    read-timeout-millis: 10000
```

### 缓存

```yaml
evernight:
  cache:
    enabled: true
    maximum-size: 10000
    maven-ttl-seconds: 21600
    npm-ttl-seconds: 21600
    pypi-ttl-seconds: 21600
    cargo-ttl-seconds: 21600
    osv-ttl-seconds: 3600
```

### IP 限流

```yaml
evernight:
  rate-limit:
    enabled: true
    client-ip-header: X-Evernight-Client-IP
    max-requests: 120
    window-seconds: 60
```

客户端 IP 解析优先级：`client-ip-header` 配置的请求头、`X-Forwarded-For`、`X-Real-IP`、`remoteAddr`。首页、静态资源、首页支撑接口和健康检查不会参与限流。

### 在线人数

在线人数按最近窗口内有请求的唯一客户端 IP 统计。首页、静态资源、首页支撑接口和 `/actuator/*` 健康检查请求不会计入人数；首页在服务健康状态为 `UP` 时会显示在线人数。

```yaml
evernight:
  online-users:
    enabled: true
    window-seconds: 300
    maximum-size: 100000
```

## 测试

```powershell
$env:JAVA_HOME='D:\devProgram\jdk\jdk-26'
$env:Path='D:\devProgram\jdk\jdk-26\bin;D:\devProgram\apache-maven-3.9.6\bin;' + $env:Path
D:\devProgram\apache-maven-3.9.6\bin\mvn.cmd '-Dmaven.repo.local=D:\jarLibrary' test
```

## 许可证

Evernight 使用 [Apache License 2.0](LICENSE) 开源协议。
