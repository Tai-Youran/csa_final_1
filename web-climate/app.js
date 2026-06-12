const state = { user: null, nodes: [], matrix: [] };

const $ = selector => document.querySelector(selector);

const els = {
  loginView: $("#loginView"),
  appView: $("#appView"),
  loginForm: $("#loginForm"),
  loginError: $("#loginError"),
  usernameLabel: $("#usernameLabel"),
  roleBadge: $("#roleBadge"),
  logoutBtn: $("#logoutBtn"),
  nodeCount: $("#nodeCount"),
  geoCount: $("#geoCount"),
  criticalCount: $("#criticalCount"),
  leadRisk: $("#leadRisk"),
  sortSelect: $("#sortSelect"),
  riskSelect: $("#riskSelect"),
  refreshBtn: $("#refreshBtn"),
  redTeamBtn: $("#redTeamBtn"),
  statusLine: $("#statusLine"),
  recordCount: $("#recordCount"),
  nodeBody: $("#nodeBody"),
  matrixList: $("#matrixList"),
  adminPanel: $("#adminPanel"),
  userPanel: $("#userPanel"),
  auditList: $("#auditList"),
  defenseList: $("#defenseList")
};

els.loginForm.addEventListener("submit", login);
els.logoutBtn.addEventListener("click", logout);
els.refreshBtn.addEventListener("click", loadClimate);
els.redTeamBtn.addEventListener("click", runRedTeam);
els.sortSelect.addEventListener("change", renderNodes);
els.riskSelect.addEventListener("change", renderNodes);

checkSession();

async function checkSession() {
  const response = await fetch("/api/me");
  if (response.ok) {
    state.user = await response.json();
    showApp();
    await loadClimate();
  }
}

async function login(event) {
  event.preventDefault();
  els.loginError.textContent = "";
  const form = new FormData(els.loginForm);
  const body = new URLSearchParams(form);
  const response = await fetch("/api/login", {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body
  });
  if (!response.ok) {
    els.loginError.textContent = "Invalid credentials.";
    return;
  }
  state.user = await response.json();
  showApp();
  await loadClimate();
}

async function logout() {
  await fetch("/api/logout", { method: "POST" });
  state.user = null;
  state.nodes = [];
  state.matrix = [];
  els.appView.classList.add("hidden");
  els.loginView.classList.remove("hidden");
}

function showApp() {
  els.loginView.classList.add("hidden");
  els.appView.classList.remove("hidden");
  els.usernameLabel.textContent = state.user.username;
  els.roleBadge.textContent = state.user.role;
  els.adminPanel.classList.toggle("hidden", state.user.role !== "ADMIN");
  els.userPanel.classList.toggle("hidden", state.user.role === "ADMIN");
}

async function loadClimate() {
  setStatus("Fetching geocoded live city nodes and current weather...");
  els.refreshBtn.disabled = true;
  try {
    const response = await fetch("/api/climate");
    if (!response.ok) throw new Error(`Backend ${response.status}`);
    const payload = await response.json();
    state.nodes = payload.nodes || [];
    state.matrix = payload.matrix || [];
    renderMetrics(payload);
    renderNodes();
    renderMatrix();
    if (state.user.role === "ADMIN") await loadAudit();
    setStatus("Live climate scrape complete.");
  } catch (error) {
    setStatus(`Error: ${error.message}`);
  } finally {
    els.refreshBtn.disabled = false;
  }
}

function renderMetrics(payload) {
  const critical = state.nodes.filter(node => node.riskBand === "Critical").length;
  els.nodeCount.textContent = payload.nodeCount;
  els.geoCount.textContent = payload.geocodedCount;
  els.criticalCount.textContent = critical;
  els.leadRisk.textContent = state.nodes[0] ? `${state.nodes[0].city}, ${state.nodes[0].country}` : "No nodes";
}

function renderNodes() {
  const band = els.riskSelect.value;
  const sortKey = els.sortSelect.value;
  const rows = state.nodes
    .filter(node => band === "All" || node.riskBand === band)
    .slice()
    .sort((a, b) => Number(b[sortKey]) - Number(a[sortKey]));

  els.recordCount.textContent = `${rows.length} records`;
  els.nodeBody.innerHTML = "";
  rows.forEach((node, index) => {
    const tr = document.createElement("tr");
    tr.innerHTML = `
      <td class="numeric">${String(index + 1).padStart(2, "0")}</td>
      <td class="city">${escapeHtml(node.city)}<br><span>${escapeHtml(node.country)} · ${escapeHtml(node.region)}</span></td>
      <td>${escapeHtml(node.nodeType)}</td>
      <td><span class="pill ${escapeHtml(node.riskBand)}">${escapeHtml(node.riskBand)}</span></td>
      <td class="numeric">${Number(node.risk).toFixed(1)}</td>
      <td class="numeric">${Number(node.temperature).toFixed(1)}°C</td>
      <td class="numeric">${Number(node.wind).toFixed(1)}</td>
      <td class="numeric">${Number(node.precipitation).toFixed(2)}</td>
    `;
    els.nodeBody.appendChild(tr);
  });
}

function renderMatrix() {
  const maxRisk = Math.max(...state.matrix.map(row => Number(row.maxRisk)), 1);
  els.matrixList.innerHTML = "";
  state.matrix.forEach(row => {
    const width = Math.max(3, (Number(row.maxRisk) / maxRisk) * 100);
    const div = document.createElement("div");
    div.className = "matrix-item";
    div.innerHTML = `
      <div class="matrix-top"><span>${escapeHtml(row.region)}</span><span>${row.count} nodes</span></div>
      <div class="bar"><span style="width:${width}%"></span></div>
      avg risk ${Number(row.avgRisk).toFixed(1)} · avg wind ${Number(row.avgWind).toFixed(1)} · max risk ${Number(row.maxRisk).toFixed(1)}
    `;
    els.matrixList.appendChild(div);
  });
}

async function loadAudit() {
  const response = await fetch("/api/admin/audit");
  if (!response.ok) return;
  const payload = await response.json();
  els.auditList.innerHTML = "";
  payload.events.forEach(event => {
    const row = document.createElement("div");
    row.className = "audit-row";
    row.textContent = event;
    els.auditList.appendChild(row);
  });
}

async function runRedTeam() {
  const response = await fetch("/api/red-team");
  const payload = await response.json();
  els.defenseList.innerHTML = "";
  payload.tests.forEach(test => {
    const row = document.createElement("div");
    row.className = "defense-row";
    row.innerHTML = `<strong>${escapeHtml(test.input)}</strong><br><span class="Critical">${escapeHtml(test.status)}</span>`;
    els.defenseList.appendChild(row);
  });
  if (state.user.role === "ADMIN") await loadAudit();
  setStatus("Red-team payloads were routed through InputSanitizer.");
}

function setStatus(message) {
  els.statusLine.textContent = message;
}

function escapeHtml(value) {
  return String(value ?? "")
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}
