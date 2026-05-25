const tools = [
  {
    name: "maven_latest_version",
    category: "maven",
    description: "根据 Maven groupId 和 artifactId 查询最新版本。",
    inputs: ["groupId：Maven 组织 ID，例如 org.springframework.boot", "artifactId：Maven 构件 ID，例如 spring-boot-starter-web", "repositoryBaseUrl：可选，Maven 镜像地址，必须在白名单内"],
    outputs: ["groupId：实际查询的 groupId", "artifactId：实际查询的 artifactId", "repositoryBaseUrl：解析后的仓库地址", "latestVersion：最新版本"],
    example: { groupId: "org.springframework.boot", artifactId: "spring-boot-starter-web", repositoryBaseUrl: "https://repo1.maven.org/maven2" }
  },
  {
    name: "maven_version_list",
    category: "maven",
    description: "查询 Maven 构件的完整版本列表，适合升级决策和兼容性分析。",
    inputs: ["groupId：Maven 组织 ID", "artifactId：Maven 构件 ID", "repositoryBaseUrl：可选，Maven 镜像地址，必须在白名单内"],
    outputs: ["latestVersion：最新版本", "versions：版本号数组", "repositoryBaseUrl：解析后的仓库地址"],
    example: { groupId: "junit", artifactId: "junit", repositoryBaseUrl: "https://maven.aliyun.com/repository/public" }
  },
  {
    name: "maven_artifact_search",
    category: "maven",
    description: "按关键词、groupId 或 artifactId 搜索 Maven Artifact 坐标。",
    inputs: ["keyword：可选，搜索关键词，例如 spring boot；仅 Maven Central 支持", "groupId：可选，精确或辅助过滤", "artifactId：可选，精确或辅助过滤", "rows：可选，返回条数，默认 10，最大 50", "repositoryBaseUrl：可选，Maven 镜像地址，必须在白名单内；非 Maven Central 镜像仅支持 groupId + artifactId 精确查询"],
    outputs: ["total：匹配总数", "artifacts：坐标摘要列表", "latestVersion：每个 artifact 的最新版本", "versionCount：版本数量"],
    example: { groupId: "org.springframework.boot", artifactId: "spring-boot-starter-web", repositoryBaseUrl: "https://maven.aliyun.com/repository/public" }
  },
  {
    name: "maven_artifact_detail",
    category: "maven",
    description: "查询 Maven Artifact 详情，补充 POM 中的描述、许可证、SCM 和开发者信息。",
    inputs: ["groupId：Maven 组织 ID", "artifactId：Maven 构件 ID", "repositoryBaseUrl：可选，Maven 镜像地址，必须在白名单内"],
    outputs: ["latestVersion：最新版本", "packaging：打包类型", "description：POM 描述", "licenses：许可证列表", "scm：源码仓库信息", "developers：开发者列表"],
    example: { groupId: "com.fasterxml.jackson.core", artifactId: "jackson-databind", repositoryBaseUrl: "https://repo1.maven.org/maven2" }
  },
  {
    name: "npm_package_info",
    category: "npm",
    description: "查询 npm registry 包元数据，包括 latest、dist-tags、仓库、许可证和依赖摘要。",
    inputs: ["packageName：npm 包名，例如 react 或 @types/node", "versionLimit：可选，最多返回多少个版本号，默认 20，最大 100", "registryBaseUrl：可选，npm registry 镜像地址，必须在白名单内"],
    outputs: ["name：包名", "latestVersion：latest dist-tag 指向版本", "registryBaseUrl：实际使用的 registry 地址", "distTags：npm dist-tags", "license：许可证", "repository：源码仓库", "dependencies：latest 版本依赖", "versions：版本摘要"],
    example: { packageName: "@types/node", versionLimit: 10, registryBaseUrl: "https://registry.npmmirror.com" }
  },
  {
    name: "pypi_package_info",
    category: "pypi",
    description: "查询 PyPI JSON API 包元数据，包括 Python 版本要求、分类器和项目链接。",
    inputs: ["packageName：PyPI 包名，例如 requests", "releaseLimit：可选，最多返回多少个 release，默认 20，最大 100", "repositoryBaseUrl：可选，PyPI JSON API 镜像地址，必须在白名单内"],
    outputs: ["name：包名", "latestVersion：最新版本", "repositoryBaseUrl：实际使用的 PyPI 仓库地址", "requiresPython：Python 版本要求", "license：许可证", "projectUrls：项目链接", "classifiers：分类器", "releases：release 摘要"],
    example: { packageName: "requests", releaseLimit: 10, repositoryBaseUrl: "https://pypi.tuna.tsinghua.edu.cn/pypi" }
  },
  {
    name: "osv_vulnerability_lookup",
    category: "security",
    description: "通过 OSV 查询 Maven、npm、PyPI 包的已知漏洞，版本号可选。",
    inputs: ["ecosystem：Maven、npm 或 PyPI", "packageName：包名；Maven 使用 groupId:artifactId", "version：可选，指定版本后查询该版本是否受影响"],
    outputs: ["vulnerabilities：漏洞列表", "id：OSV 漏洞 ID", "aliases：CVE/GHSA 等别名", "references：参考链接", "affectedRanges：影响范围", "fixedVersions：修复版本"],
    example: { ecosystem: "Maven", packageName: "com.fasterxml.jackson.core:jackson-databind", version: "2.9.0" }
  }
];

function renderTools(filter = "all") {
  const grid = document.querySelector("#toolGrid");
  const template = document.querySelector("#toolCardTemplate");
  grid.innerHTML = "";
  tools
    .filter(tool => filter === "all" || tool.category === filter)
    .forEach(tool => {
      const node = template.content.cloneNode(true);
      node.querySelector(".tool-category").textContent = tool.category.toUpperCase();
      node.querySelector("h3").textContent = tool.name;
      node.querySelector(".tool-description").textContent = tool.description;
      node.querySelector(".inputs").innerHTML = tool.inputs.map(formatItem).join("");
      node.querySelector(".outputs").innerHTML = tool.outputs.map(formatItem).join("");
      node.querySelector(".example").textContent = JSON.stringify({ tool: tool.name, arguments: tool.example }, null, 2);
      grid.appendChild(node);
    });
}

function formatItem(text) {
  const [name, ...rest] = text.split("：");
  return `<li><strong>${escapeHtml(name)}</strong>${rest.length ? `：${escapeHtml(rest.join("："))}` : ""}</li>`;
}

function escapeHtml(value) {
  return value.replace(/[&<>'"]/g, char => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", "'": "&#39;", '"': "&quot;" }[char]));
}

async function loadRepositories() {
  const grid = document.querySelector("#repositoryGrid");
  try {
    const response = await fetch("/api/repositories", { cache: "no-store" });
    if (!response.ok) {
      throw new Error(`HTTP ${response.status}`);
    }
    const data = await response.json();
    renderRepositories(data);
  } catch (error) {
    grid.innerHTML = `<article class="repository-card"><h3>配置读取失败</h3><p>${escapeHtml(error.message)}</p></article>`;
  }
}

function renderRepositories(data) {
  const grid = document.querySelector("#repositoryGrid");
  const labels = { maven: "Maven", npm: "npm", pypi: "PyPI" };
  grid.innerHTML = Object.entries(labels).map(([key, label]) => {
    const group = data[key] || { repositories: [] };
    const repositories = Array.isArray(group.repositories) ? group.repositories : Object.values(group.repositories || {});
    const rows = repositories.map((url, index) => {
      const defaultMark = index === 0 ? `<span class="default-tag">默认</span>` : "";
      return `<li><strong>#${index + 1}</strong><code>${escapeHtml(url)}</code>${defaultMark}</li>`;
    }).join("");
    return `
      <article class="repository-card">
        <div class="repository-card-header">
          <h3>${label}</h3>
          <span>${repositories.length} 个地址</span>
        </div>
        <ul class="repository-list">${rows}</ul>
      </article>
    `;
  }).join("");
}

async function checkHealth() {
  const status = document.querySelector("#serviceStatus");
  const dot = status.querySelector(".status-dot");
  const text = status.querySelector("span:last-child");
  try {
    const response = await fetch("/actuator/health", { cache: "no-store" });
    const data = await response.json();
    if (response.ok && data.status === "UP") {
      dot.className = "status-dot status-up";
      text.textContent = "服务可用 · UP";
      return;
    }
    dot.className = "status-dot status-down";
    text.textContent = `服务异常 · ${data.status || response.status}`;
  } catch (error) {
    dot.className = "status-dot status-down";
    text.textContent = "无法连接健康检查";
  }
}

document.querySelectorAll(".filter").forEach(button => {
  button.addEventListener("click", () => {
    document.querySelectorAll(".filter").forEach(item => item.classList.remove("active"));
    button.classList.add("active");
    renderTools(button.dataset.filter);
  });
});

renderTools();
loadRepositories();
checkHealth();
setInterval(checkHealth, 30000);
