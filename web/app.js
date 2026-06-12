const state = {
  articles: [],
  matrix: [],
  scrapedAt: null
};

const els = {
  nodeCount: document.querySelector("#nodeCount"),
  aiSignals: document.querySelector("#aiSignals"),
  avgImpact: document.querySelector("#avgImpact"),
  leadSignal: document.querySelector("#leadSignal"),
  statusLine: document.querySelector("#statusLine"),
  recordCount: document.querySelector("#recordCount"),
  articleBody: document.querySelector("#articleBody"),
  matrixList: document.querySelector("#matrixList"),
  defenseList: document.querySelector("#defenseList"),
  sortSelect: document.querySelector("#sortSelect"),
  categorySelect: document.querySelector("#categorySelect"),
  refreshBtn: document.querySelector("#refreshBtn"),
  redTeamBtn: document.querySelector("#redTeamBtn")
};

els.refreshBtn.addEventListener("click", loadArticles);
els.redTeamBtn.addEventListener("click", runRedTeam);
els.sortSelect.addEventListener("change", renderArticles);
els.categorySelect.addEventListener("change", renderArticles);

loadArticles();

async function loadArticles() {
  setStatus("Fetching live Hacker News data from Java backend...");
  els.refreshBtn.disabled = true;

  try {
    const response = await fetch("/api/articles");
    if (!response.ok) {
      throw new Error(`Backend returned ${response.status}`);
    }
    const payload = await response.json();
    state.articles = payload.articles || [];
    state.matrix = payload.matrix || [];
    state.scrapedAt = payload.scrapedAt;
    renderDashboard(payload);
    renderArticles();
    renderMatrix();
    setStatus(`Live scrape complete. Source: ${payload.source}`);
  } catch (error) {
    setStatus(`Error: ${error.message}`);
  } finally {
    els.refreshBtn.disabled = false;
  }
}

function renderDashboard(payload) {
  const articles = payload.articles || [];
  const aiCount = articles.filter(article => article.category === "AI").length;
  const avgImpact = average(articles.map(article => article.impact));
  const lead = articles[0];

  els.nodeCount.textContent = payload.nodeCount ?? "--";
  els.aiSignals.textContent = aiCount;
  els.avgImpact.textContent = avgImpact.toFixed(1);
  els.leadSignal.textContent = lead ? lead.title : "No live records";
}

function renderArticles() {
  const category = els.categorySelect.value;
  const sortKey = els.sortSelect.value;
  const filtered = state.articles
    .filter(article => category === "All" || article.category === category)
    .slice()
    .sort((a, b) => Number(b[sortKey]) - Number(a[sortKey]));

  els.recordCount.textContent = `${filtered.length} records`;
  els.articleBody.innerHTML = "";

  filtered.slice(0, 50).forEach((article, index) => {
    const row = document.createElement("tr");
    row.innerHTML = `
      <td class="numeric">${String(index + 1).padStart(2, "0")}</td>
      <td class="headline"><a href="${safeUrl(article.url)}" target="_blank" rel="noreferrer">${escapeHtml(article.title)}</a></td>
      <td><span class="pill">${escapeHtml(article.category)}</span></td>
      <td class="numeric">${article.points}</td>
      <td class="numeric">${article.comments}</td>
      <td class="numeric">${article.hype}</td>
      <td class="numeric">${Number(article.impact).toFixed(1)}</td>
    `;
    els.articleBody.appendChild(row);
  });
}

function renderMatrix() {
  const maxImpact = Math.max(...state.matrix.map(item => item.maxImpact), 1);
  els.matrixList.innerHTML = "";

  state.matrix.forEach(item => {
    const width = Math.max(3, (item.maxImpact / maxImpact) * 100);
    const block = document.createElement("div");
    block.className = "matrix-item";
    block.innerHTML = `
      <div class="matrix-top">
        <span>${escapeHtml(item.category)}</span>
        <span>${item.count} nodes</span>
      </div>
      <div class="bar"><span style="width: ${width}%"></span></div>
      <div class="matrix-meta">
        avg points ${Number(item.avgPoints).toFixed(1)} · avg hype ${Number(item.avgHype).toFixed(1)} · max impact ${Number(item.maxImpact).toFixed(1)}
      </div>
    `;
    els.matrixList.appendChild(block);
  });
}

async function runRedTeam() {
  setStatus("Running payloads through InputSanitizer...");
  const response = await fetch("/api/red-team");
  const payload = await response.json();
  els.defenseList.innerHTML = "";

  payload.tests.forEach(test => {
    const row = document.createElement("div");
    row.className = "defense-row";
    row.innerHTML = `
      <strong>${escapeHtml(test.input)}</strong><br>
      <span class="blocked">${escapeHtml(test.status)}</span>
    `;
    els.defenseList.appendChild(row);
  });

  setStatus("Red-team demo complete. Malicious inputs were blocked before filtering.");
}

function average(values) {
  if (values.length === 0) {
    return 0;
  }
  return values.reduce((sum, value) => sum + Number(value), 0) / values.length;
}

function setStatus(message) {
  els.statusLine.textContent = message;
}

function safeUrl(url) {
  if (!url || url.startsWith("item?id=")) {
    return "https://news.ycombinator.com/" + url;
  }
  return url;
}

function escapeHtml(value) {
  return String(value ?? "")
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}
