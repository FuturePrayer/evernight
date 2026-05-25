<p align="center">
  <img src="src/main/resources/static/icon.svg" alt="Evernight logo" width="96" height="96">
</p>

<h1 align="center">Evernight MCP Server</h1>

<p align="center">
  Dependency version, package metadata, and vulnerability intelligence for coding agents.
</p>

<p align="center">
  <a href="README.md">中文</a> ·
  <a href="#quick-start">Quick Start</a> ·
  <a href="#docker-compose">Docker Compose</a> ·
  <a href="#license">License</a>
</p>

## Overview

Evernight is a streamable HTTP MCP server built with Spring AI MCP Server WebMVC. It exposes common Maven, npm, PyPI, and OSV queries as structured MCP tools for vibe coding, dependency upgrades, vulnerability triage, and automated engineering workflows.

Default endpoints:

- MCP endpoint: `/mcp`
- Web console: `/`
- Repository whitelist: `/api/repositories`
- Health check: `/actuator/health`

## Features

- Query Maven, npm, and PyPI package metadata.
- Query known vulnerabilities through OSV.
- Validate Maven, npm, and PyPI mirror URLs against explicit whitelists to reduce SSRF risk.
- Cache upstream responses with Caffeine and per-ecosystem TTL settings.
- Support HTTP, HTTPS, and SOCKS5 proxies for upstream requests.
- Rate-limit requests by client IP.
- Return a consistent HTTP error contract for agent-friendly failure handling.
- Include a static web console for tools, parameters, examples, service status, and repository whitelists.

## MCP Tools

| Tool | Description |
| --- | --- |
| `maven_latest_version` | Query the latest Maven version by `groupId` and `artifactId`. |
| `maven_version_list` | Query the full Maven version list. |
| `maven_artifact_search` | Search Maven artifacts; non-Maven Central mirrors support exact `groupId` + `artifactId` lookup only. |
| `maven_artifact_detail` | Query Maven artifact details, including POM description, licenses, SCM, and developers. |
| `npm_package_info` | Query npm package metadata, including `latest`, `dist-tags`, license, repository, dependencies, and version summary. |
| `pypi_package_info` | Query PyPI package metadata, including latest version, Python requirements, license, project URLs, and release summary. |
| `osv_vulnerability_lookup` | Query known vulnerabilities for Maven, npm, and PyPI packages through OSV. |

## Quick Start

Requirements:

- JDK 25+
- Maven 3.9+

Run locally:

```bash
mvn spring-boot:run
```

Open:

- Web console: `http://localhost:25924/`
- MCP: `http://localhost:25924/mcp`
- Health check: `http://localhost:25924/actuator/health`

## Docker Compose

This repository includes a source-building `Dockerfile` and `docker-compose.yml`.

```bash
docker compose up --build -d
```

Check status:

```bash
docker compose ps
curl http://localhost:25924/actuator/health
```

Stop:

```bash
docker compose down
```

## Configuration

### Repository Whitelist

Tool parameters named `repositoryBaseUrl` or `registryBaseUrl` are optional. When provided, the URL must match the whitelist for its ecosystem. When omitted, the first URL in the comma-separated list is used as the default mirror.

### Common Environment Variables

| Variable | Default | Description |
| --- | --- | --- |
| `SERVER_PORT` | `25924` | HTTP server port. |
| `MAVEN_REPOSITORY_REPOSITORIES` | `https://repo1.maven.org/maven2,https://maven.aliyun.com/repository/public` | Maven repository whitelist, comma-separated; the first item is the default repository. |
| `NPM_REGISTRY_REPOSITORIES` | `https://registry.npmjs.org,https://registry.npmmirror.com` | npm registry whitelist, comma-separated; the first item is the default registry. |
| `PYPI_REPOSITORY_REPOSITORIES` | `https://pypi.org/pypi,https://pypi.tuna.tsinghua.edu.cn/pypi` | PyPI JSON API whitelist, comma-separated; the first item is the default repository. |
| `EVERNIGHT_HTTP_PROXY_URL` | empty | Upstream proxy URL. Supports `http://127.0.0.1:7890`, `https://127.0.0.1:7890`, and `socks5://127.0.0.1:1080`. |
| `EVERNIGHT_HTTP_CONNECT_TIMEOUT_MILLIS` | `5000` | Upstream HTTP connection timeout in milliseconds. |
| `EVERNIGHT_HTTP_READ_TIMEOUT_MILLIS` | `10000` | Upstream HTTP read timeout in milliseconds. |
| `EVERNIGHT_RATE_LIMIT_ENABLED` | `true` | Enables IP rate limiting. |
| `EVERNIGHT_RATE_LIMIT_CLIENT_IP_HEADER` | `X-Evernight-Client-IP` | Header that carries the real client IP from a CDN or gateway. |
| `EVERNIGHT_RATE_LIMIT_MAX_REQUESTS` | `120` | Maximum requests per client IP within the rate-limit window. |
| `EVERNIGHT_RATE_LIMIT_WINDOW_SECONDS` | `60` | Rate-limit window length in seconds. |
| `EVERNIGHT_ONLINE_USERS_ENABLED` | `true` | Enables online user counting. |
| `EVERNIGHT_ONLINE_USERS_WINDOW_SECONDS` | `300` | Online user activity window in seconds. |
| `EVERNIGHT_ONLINE_USERS_MAXIMUM_SIZE` | `100000` | Maximum retained client identifiers for online user counting. |
| `EVERNIGHT_CACHE_ENABLED` | `true` | Enables upstream query caching. |
| `EVERNIGHT_CACHE_MAXIMUM_SIZE` | `10000` | Maximum cache entries. |

### HTTP Proxy

```yaml
evernight:
  http:
    proxy-url: # http://127.0.0.1:7890, https://127.0.0.1:7890, socks5://127.0.0.1:1080
    connect-timeout-millis: 5000
    read-timeout-millis: 10000
```

### Cache

```yaml
evernight:
  cache:
    enabled: true
    maximum-size: 10000
    maven-ttl-seconds: 21600
    npm-ttl-seconds: 21600
    pypi-ttl-seconds: 21600
    osv-ttl-seconds: 3600
```

### Rate Limit

```yaml
evernight:
  rate-limit:
    enabled: true
    client-ip-header: X-Evernight-Client-IP
    max-requests: 120
    window-seconds: 60
```

Client IP resolution order: configured `client-ip-header`, `X-Forwarded-For`, `X-Real-IP`, then `remoteAddr`. The home page, static assets, home-page support APIs, and health checks are excluded from rate limiting.

### Online Users

Online users are counted as unique client IPs that sent requests within the recent activity window. The home page, static assets, home-page support APIs, and `/actuator/*` health-check requests are excluded. The web console shows the online count only when the service health status is `UP`.

```yaml
evernight:
  online-users:
    enabled: true
    window-seconds: 300
    maximum-size: 100000
```

## Tests

```bash
mvn test
```

## License

Evernight is licensed under the [Apache License 2.0](LICENSE).
