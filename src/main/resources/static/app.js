function escapeHtml(value) {
  return String(value).replace(/[&<>'"]/g, char => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", "'": "&#39;", '"': "&quot;" }[char]));
}

async function loadRepositories() {
  const grid = document.querySelector("#repositoryGrid");
  if (!grid) {
    return;
  }
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
  const labels = { maven: "Maven", npm: "npm", pypi: "PyPI", cargo: "Cargo" };
  grid.innerHTML = Object.entries(labels).map(([key, label]) => {
    const group = data[key] || { repositories: [] };
    const repositories = Array.isArray(group.repositories) ? group.repositories : Object.values(group.repositories || {});
    const rows = repositories.map((url, index) => {
      const defaultMark = index === 0 ? `<span class="default-tag">默认</span>` : "";
      return `<li><strong>#${index + 1}</strong><code title="${escapeHtml(url)}">${escapeHtml(url)}</code>${defaultMark}</li>`;
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
  if (!status) {
    return;
  }
  const dot = status.querySelector(".status-dot");
  const text = status.querySelector(".status-text");
  const onlineCount = status.querySelector(".online-count");
  try {
    const response = await fetch("/actuator/health", { cache: "no-store" });
    const data = await response.json();
    if (response.ok && data.status === "UP") {
      dot.className = "status-dot status-up";
      text.textContent = "服务可用 · UP";
      await loadOnlineUsers(onlineCount);
      return;
    }
    dot.className = "status-dot status-down";
    text.textContent = `服务异常 · ${data.status || response.status}`;
    hideOnlineUsers(onlineCount);
  } catch (error) {
    dot.className = "status-dot status-down";
    text.textContent = "无法连接健康检查";
    hideOnlineUsers(onlineCount);
  }
}

async function loadOnlineUsers(target) {
  try {
    const response = await fetch("/api/online-users", { cache: "no-store" });
    if (!response.ok) {
      throw new Error(`HTTP ${response.status}`);
    }
    const data = await response.json();
    target.textContent = `在线 ${Number(data.count || 0)} 人`;
    target.hidden = false;
  } catch (error) {
    hideOnlineUsers(target);
  }
}

function hideOnlineUsers(target) {
  target.hidden = true;
  target.textContent = "";
}

loadRepositories();
checkHealth();
setInterval(checkHealth, 30000);
