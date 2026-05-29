const tools = [
  {
    name: "maven_latest_version",
    category: "maven",
    description: "用于 Java/JVM 项目的 Maven 依赖坐标，根据 groupId 和 artifactId 查询最新版本。",
    fields: [
      { name: "groupId", label: "groupId", required: true, placeholder: "org.springframework.boot" },
      { name: "artifactId", label: "artifactId", required: true, placeholder: "spring-boot-starter-web" },
      { name: "repositoryBaseUrl", label: "Maven 镜像地址", placeholder: "留空使用默认地址" }
    ]
  },
  {
    name: "maven_version_list",
    category: "maven",
    description: "用于 Java/JVM 项目的 Maven 构件，查询完整版本列表。",
    fields: [
      { name: "groupId", label: "groupId", required: true, placeholder: "junit" },
      { name: "artifactId", label: "artifactId", required: true, placeholder: "junit" },
      { name: "repositoryBaseUrl", label: "Maven 镜像地址", placeholder: "留空使用默认地址" }
    ]
  },
  {
    name: "maven_artifact_search",
    category: "maven",
    description: "用于 Java/JVM 项目，按关键词、groupId 或 artifactId 搜索 Maven Artifact 坐标。",
    fields: [
      { name: "keyword", label: "关键词", placeholder: "spring boot" },
      { name: "groupId", label: "groupId", placeholder: "org.springframework.boot" },
      { name: "artifactId", label: "artifactId", placeholder: "spring-boot-starter-web" },
      { name: "rows", label: "返回条数", type: "number", min: 1, max: 50, placeholder: "10" },
      { name: "repositoryBaseUrl", label: "Maven 镜像地址", placeholder: "留空使用默认地址" }
    ]
  },
  {
    name: "maven_artifact_detail",
    category: "maven",
    description: "用于 Java/JVM 项目，查询 Maven Artifact 详情，包括 POM 描述、许可证、SCM 和开发者信息。",
    fields: [
      { name: "groupId", label: "groupId", required: true, placeholder: "com.fasterxml.jackson.core" },
      { name: "artifactId", label: "artifactId", required: true, placeholder: "jackson-databind" },
      { name: "repositoryBaseUrl", label: "Maven 镜像地址", placeholder: "留空使用默认地址" }
    ]
  },
  {
    name: "maven_dependency_detail",
    category: "maven",
    description: "用于 Java/JVM 项目，查询指定 Maven 坐标和版本的 POM 依赖详情。",
    fields: [
      { name: "groupId", label: "groupId", required: true, placeholder: "org.springframework.boot" },
      { name: "artifactId", label: "artifactId", required: true, placeholder: "spring-boot-starter-web" },
      { name: "version", label: "版本", required: true, placeholder: "3.4.5" },
      { name: "repositoryBaseUrl", label: "Maven 镜像地址", placeholder: "留空使用默认地址" }
    ]
  },
  {
    name: "maven_artifact_java_version",
    category: "maven",
    description: "用于 Java/JVM 项目，只读取 POM 中声明的 Java 版本字段；这些字段表示目标兼容版本或要求的 Java 版本，不一定是实际执行编译的 JDK。",
    fields: [
      { name: "groupId", label: "groupId", required: true, placeholder: "org.springframework.boot" },
      { name: "artifactId", label: "artifactId", required: true, placeholder: "spring-boot-starter-web" },
      { name: "version", label: "版本", placeholder: "留空使用最新版本" },
      { name: "repositoryBaseUrl", label: "Maven 镜像地址", placeholder: "留空使用默认地址" }
    ]
  },
  {
    name: "npm_package_info",
    category: "npm",
    description: "用于 JavaScript/TypeScript/Node.js 项目的 npm 包，查询 registry 元数据，包括 latest、dist-tags、许可证、仓库和版本摘要。",
    fields: [
      { name: "packageName", label: "包名", required: true, placeholder: "@types/node" },
      { name: "versionLimit", label: "版本数量", type: "number", min: 1, max: 100, placeholder: "20" },
      { name: "registryBaseUrl", label: "npm registry", placeholder: "留空使用默认地址" }
    ]
  },
  {
    name: "npm_package_version_detail",
    category: "npm",
    description: "用于 JavaScript/TypeScript/Node.js 项目的 npm 包，查询指定版本元数据，包括依赖、engines、bin、deprecated 和 tarball 信息。",
    fields: [
      { name: "packageName", label: "包名", required: true, placeholder: "react" },
      { name: "version", label: "版本", placeholder: "留空使用 latest" },
      { name: "registryBaseUrl", label: "npm registry", placeholder: "留空使用默认地址" }
    ]
  },
  {
    name: "pypi_package_info",
    category: "pypi",
    description: "用于 Python 项目的 PyPI 包，查询元数据，包括 Python 版本要求、分类器和项目链接。",
    fields: [
      { name: "packageName", label: "包名", required: true, placeholder: "requests" },
      { name: "releaseLimit", label: "release 数量", type: "number", min: 1, max: 100, placeholder: "20" },
      { name: "repositoryBaseUrl", label: "PyPI 仓库地址", placeholder: "留空使用默认地址" }
    ]
  },
  {
    name: "pypi_release_files",
    category: "pypi",
    description: "用于 Python 项目的 PyPI 包，查询指定 release 的 wheel/sdist 文件、哈希、大小、上传时间和 yanked 状态。",
    fields: [
      { name: "packageName", label: "包名", required: true, placeholder: "requests" },
      { name: "version", label: "版本", placeholder: "留空使用最新版本" },
      { name: "repositoryBaseUrl", label: "PyPI 仓库地址", placeholder: "留空使用默认地址" }
    ]
  },
  {
    name: "cargo_crate_search",
    category: "cargo",
    description: "用于 Rust 项目的 Cargo/crates.io crate 搜索；registryBaseUrl 必须是 Cargo registry API 白名单地址。",
    fields: [
      { name: "keyword", label: "关键词", required: true, placeholder: "serde" },
      { name: "perPage", label: "返回条数", type: "number", min: 1, max: 50, placeholder: "20" },
      { name: "registryBaseUrl", label: "Cargo registry API", placeholder: "留空使用默认地址" }
    ]
  },
  {
    name: "cargo_crate_info",
    category: "cargo",
    description: "用于 Rust 项目的 Cargo/crates.io crate，查询元数据，包括最新版本、许可证、仓库、关键词、分类和版本摘要。",
    fields: [
      { name: "crateName", label: "crate 名称", required: true, placeholder: "serde" },
      { name: "versionLimit", label: "版本数量", type: "number", min: 1, max: 100, placeholder: "20" },
      { name: "registryBaseUrl", label: "Cargo registry API", placeholder: "留空使用默认地址" }
    ]
  },
  {
    name: "cargo_crate_version_detail",
    category: "cargo",
    description: "用于 Rust 项目的 Cargo/crates.io crate，查询指定版本详情和依赖；rustVersion 表示最低 Rust 版本或兼容要求，不代表实际编译时使用的 rustc 版本。",
    fields: [
      { name: "crateName", label: "crate 名称", required: true, placeholder: "serde" },
      { name: "version", label: "版本", placeholder: "留空使用最新稳定版本" },
      { name: "registryBaseUrl", label: "Cargo registry API", placeholder: "留空使用默认地址" }
    ]
  },
  {
    name: "osv_vulnerability_lookup",
    category: "security",
    description: "跨生态安全工具，通过 OSV 查询已知漏洞；Maven=Java/JVM，npm=JavaScript/TypeScript/Node.js，PyPI=Python，Cargo/crates.io=Rust。",
    fields: [
      { name: "ecosystem", label: "生态", required: true, placeholder: "Maven / npm / PyPI / cargo" },
      { name: "packageName", label: "包名", required: true, placeholder: "com.fasterxml.jackson.core:jackson-databind" },
      { name: "version", label: "版本", placeholder: "2.9.0" }
    ]
  },
  {
    name: "osv_batch_vulnerability_lookup",
    category: "security",
    description: "跨生态安全工具，批量查询 OSV 已知漏洞；Maven=Java/JVM，npm=JavaScript/TypeScript/Node.js，PyPI=Python，Cargo/crates.io=Rust。",
    fields: [
      {
        name: "packages",
        label: "包列表 JSON",
        required: true,
        multiline: true,
        placeholder: '[{"ecosystem":"npm","packageName":"react","version":"18.2.0"},{"ecosystem":"cargo","packageName":"serde","version":"1.0.0"}]'
      }
    ]
  }
];

let selectedTool = tools[0];
let challengeToken = "";
const toolResults = new Map();

function renderToolList(filter = "all") {
  const list = document.querySelector("#toolList");
  const visibleTools = tools.filter(tool => filter === "all" || tool.category === filter);
  if (!visibleTools.some(tool => tool.name === selectedTool.name) && visibleTools.length > 0) {
    selectedTool = visibleTools[0];
  }
  list.innerHTML = visibleTools.map(tool => `
    <button class="tool-list-item ${tool.name === selectedTool.name ? "active" : ""}" type="button" data-tool="${escapeHtml(tool.name)}">
      <span>${escapeHtml(tool.category.toUpperCase())}</span>
      <strong>${escapeHtml(tool.name)}</strong>
    </button>
  `).join("");
  list.querySelectorAll(".tool-list-item").forEach(item => {
    item.addEventListener("click", () => {
      selectedTool = tools.find(tool => tool.name === item.dataset.tool);
      renderToolList(filter);
      renderSelectedTool();
    });
  });
  renderSelectedTool();
}

function renderSelectedTool() {
  document.querySelector("#selectedCategory").textContent = selectedTool.category.toUpperCase();
  document.querySelector("#selectedToolName").textContent = selectedTool.name;
  document.querySelector("#selectedToolDescription").textContent = selectedTool.description;
  const grid = document.querySelector("#fieldGrid");
  grid.innerHTML = selectedTool.fields.map(renderField).join("");
  renderToolResult();
}

function renderField(field) {
  const required = field.required ? "required" : "";
  if (field.multiline) {
    return `
      <label class="field full-width">
        <span>${escapeHtml(field.label)}${field.required ? " *" : ""}</span>
        <textarea
          name="${escapeHtml(field.name)}"
          ${required}
          placeholder="${escapeHtml(field.placeholder || "")}"></textarea>
      </label>
    `;
  }
  return `
    <label class="field">
      <span>${escapeHtml(field.label)}${field.required ? " *" : ""}</span>
      <input
        name="${escapeHtml(field.name)}"
        type="${field.type || "text"}"
        ${required}
        ${field.min ? `min="${field.min}"` : ""}
        ${field.max ? `max="${field.max}"` : ""}
        placeholder="${escapeHtml(field.placeholder || "")}">
    </label>
  `;
}

function renderToolResult() {
  const output = document.querySelector("#resultOutput");
  const result = toolResults.get(selectedTool.name);
  if (!result) {
    output.textContent = "";
    output.classList.remove("error-text");
    return;
  }
  output.textContent = result.text;
  output.classList.toggle("error-text", result.error);
}

async function refreshChallenge() {
  const question = document.querySelector("#challengeQuestion");
  const answer = document.querySelector("#challengeAnswer");
  question.textContent = "加载中";
  answer.value = "";
  try {
    const response = await fetch("/api/human-challenge", { cache: "no-store" });
    if (!response.ok) {
      throw new Error(`HTTP ${response.status}`);
    }
    const data = await response.json();
    challengeToken = data.token;
    question.textContent = data.question;
  } catch (error) {
    challengeToken = "";
    question.textContent = `加载失败：${error.message}`;
  }
}

function collectArguments(form) {
  const formData = new FormData(form);
  const args = {};
  selectedTool.fields.forEach(field => {
    const rawValue = String(formData.get(field.name) || "").trim();
    if (rawValue === "") {
      return;
    }
    if (field.multiline) {
      args[field.name] = JSON.parse(rawValue);
      return;
    }
    args[field.name] = field.type === "number" ? Number(rawValue) : rawValue;
  });
  return args;
}

async function invokeSelectedTool(event) {
  event.preventDefault();
  const form = event.currentTarget;
  const invokedTool = selectedTool;
  const invokedToolName = invokedTool.name;
  const submitButton = form.querySelector(".invoke-button");
  submitButton.disabled = true;
  saveToolResult(invokedToolName, "调用中", false);
  try {
    const response = await fetch("/api/tool-invocations", {
      method: "POST",
      headers: { "Content-Type": "application/json", "Accept": "application/json" },
      body: JSON.stringify({
        tool: invokedToolName,
        arguments: collectArguments(form),
        challengeToken,
        challengeAnswer: document.querySelector("#challengeAnswer").value
      })
    });
    const data = await response.json();
    saveToolResult(invokedToolName, JSON.stringify(data, null, 2), !response.ok);
  } catch (error) {
    saveToolResult(invokedToolName, JSON.stringify({ message: error.message }, null, 2), true);
  } finally {
    submitButton.disabled = false;
    await refreshChallenge();
  }
}

function saveToolResult(toolName, text, error) {
  toolResults.set(toolName, { text, error });
  if (selectedTool.name === toolName) {
    renderToolResult();
  }
}

async function copyResult() {
  const result = toolResults.get(selectedTool.name);
  if (!result) {
    return;
  }
  await navigator.clipboard.writeText(result.text);
}

document.querySelectorAll(".filter").forEach(button => {
  button.addEventListener("click", () => {
    document.querySelectorAll(".filter").forEach(item => item.classList.remove("active"));
    button.classList.add("active");
    renderToolList(button.dataset.filter);
  });
});

document.querySelector("#invokeForm").addEventListener("submit", invokeSelectedTool);
document.querySelector("#refreshChallenge").addEventListener("click", refreshChallenge);
document.querySelector("#copyResult").addEventListener("click", copyResult);

renderToolList();
refreshChallenge();
