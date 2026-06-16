import * as THREE from "https://unpkg.com/three@0.160.0/build/three.module.js";

const state = {
  user: { username: "demo", role: "USER" },
  lang: localStorage.getItem("starscope-lang") || "zh",
  nodes: [],
  matrix: [],
  selectedCountry: "",
  selectedAdminArea: "",
  selectedCity: "",
  selectedConstellation: "Orion",
  dome: null
};

const $ = selector => document.querySelector(selector);

const els = {
  loginView: $("#loginView"),
  appView: $("#appView"),
  loginForm: $("#loginForm"),
  loginError: $("#loginError"),
  loginModeBtn: $("#loginModeBtn"),
  signupModeBtn: $("#signupModeBtn"),
  authTitle: $("#authTitle"),
  confirmPasswordLabel: $("#confirmPasswordLabel"),
  passwordRule: $("#passwordRule"),
  authSubmitBtn: $("#authSubmitBtn"),
  loginThemeToggle: $("#loginThemeToggle"),
  usernameLabel: $("#usernameLabel"),
  roleBadge: $("#roleBadge"),
  dashboardView: $("#dashboardView"),
  profileView: $("#profileView"),
  dashboardNavBtn: $("#dashboardNavBtn"),
  profileNavBtn: $("#profileNavBtn"),
  themeToggle: $("#themeToggle"),
  logoutBtn: $("#logoutBtn"),
  nodeCount: $("#nodeCount"),
  geoCount: $("#geoCount"),
  primeCount: $("#criticalCount"),
  bestTonight: $("#leadRisk"),
  countrySelect: $("#countrySelect"),
  adminAreaSelect: $("#adminAreaSelect"),
  citySelect: $("#citySelect"),
  locationHint: $("#locationHint"),
  recommendationCards: $("#recommendationCards"),
  sortSelect: $("#sortSelect"),
  riskSelect: $("#riskSelect"),
  refreshBtn: $("#refreshBtn"),
  exportBtn: $("#exportBtn"),
  redTeamBtn: $("#redTeamBtn"),
  statusLine: $("#statusLine"),
  recordCount: $("#recordCount"),
  nodeBody: $("#nodeBody"),
  matrixList: $("#matrixList"),
  matrixLegend: $("#matrixLegend"),
  defenseList: $("#defenseList"),
  constellationInfo: $("#constellationInfo"),
  constellationButtons: $("#constellationButtons"),
  starDome: $("#starDome"),
  langToggle: $("#langToggle"),
  loginLangToggle: $("#loginLangToggle"),
  loadingOverlay: $("#loadingOverlay"),
  loadingText: $("#loadingText"),
  loadingBar: $("#loadingBar"),
  loadingPercent: $("#loadingPercent")
};

const I18N = {
  zh: {
    navDashboard: "观测台",
    navProfile: "个人主页",
    roleUser: "用户",
    roleAdmin: "管理员",
    themeLight: "浅色",
    themeDark: "深色",
    statusDone: "演示数据已加载（GitHub Pages 静态版）",
    statusFetching: "正在加载演示数据…",
    pickLocation: "请选择国家、省/州和城市，系统将推荐附近最佳观星点。",
    pickAdmin: "请选择省/州以缩小范围。",
    pickCity: "请选择城市。",
    pickReady: "已基于演示数据生成本地观测点。",
    emptyTable: "请选择国家、省/州和城市以生成本地观测点。",
    emptyPicks: "选择国家、省/州和城市后解锁前三推荐。",
    exportReady: "CSV 已开始下载。"
  },
  en: {
    navDashboard: "Radar",
    navProfile: "Profile",
    roleUser: "USER",
    roleAdmin: "ADMIN",
    themeLight: "Light",
    themeDark: "Dark",
    statusDone: "Demo data loaded (GitHub Pages static)",
    statusFetching: "Loading demo data…",
    pickLocation: "Select country, province/state, and city.",
    pickAdmin: "Select a province/state.",
    pickCity: "Select a city.",
    pickReady: "Local observing sites computed from demo data.",
    emptyTable: "Select country, province/state, and city first.",
    emptyPicks: "Select location to unlock Top 3 picks.",
    exportReady: "CSV download started."
  }
};

function t(key) {
  return I18N[state.lang]?.[key] ?? I18N.en[key] ?? key;
}

function setStatus(message) {
  if (els.statusLine) els.statusLine.textContent = message;
}

function escapeHtml(value) {
  return String(value ?? "")
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}

function roleLabel(role) {
  return role === "ADMIN" ? t("roleAdmin") : t("roleUser");
}

function setTheme(theme) {
  document.body.dataset.theme = theme;
  localStorage.setItem("starscope-theme", theme);
  const label = theme === "dark" ? t("themeLight") : t("themeDark");
  els.themeToggle.textContent = label;
  els.loginThemeToggle.textContent = label;
  applyDomeTheme();
}

function toggleTheme() {
  setTheme(document.body.dataset.theme === "dark" ? "light" : "dark");
}

function toggleLang() {
  state.lang = state.lang === "zh" ? "en" : "zh";
  localStorage.setItem("starscope-lang", state.lang);
  els.langToggle.textContent = state.lang === "zh" ? "EN" : "中文";
  els.loginLangToggle.textContent = els.langToggle.textContent;
  els.dashboardNavBtn.textContent = t("navDashboard");
  els.profileNavBtn.textContent = t("navProfile");
  els.roleBadge.textContent = roleLabel(state.user.role);
  updateLocationHint();
  renderMatrixLegend();
  renderNodes();
  renderMatrix();
  setTheme(document.body.dataset.theme || "dark");
  selectConstellation(state.selectedConstellation);
}

function showApp() {
  els.loginView.classList.add("hidden");
  els.appView.classList.remove("hidden");
  els.usernameLabel.textContent = state.user.username;
  els.roleBadge.textContent = roleLabel(state.user.role);
  els.langToggle.textContent = state.lang === "zh" ? "EN" : "中文";
  els.loginLangToggle.textContent = els.langToggle.textContent;
  els.dashboardNavBtn.textContent = t("navDashboard");
  els.profileNavBtn.textContent = t("navProfile");
  setView("dashboard");
  requestAnimationFrame(initStarDome);
}

function setView(view) {
  const isProfile = view === "profile";
  els.dashboardView.classList.toggle("hidden", isProfile);
  els.profileView.classList.toggle("hidden", !isProfile);
  els.dashboardNavBtn.classList.toggle("is-active", !isProfile);
  els.profileNavBtn.classList.toggle("is-active", isProfile);
}

async function loadDemoClimate() {
  setStatus(t("statusFetching"));
  const progress = startLoadingProgress();
  try {
    const response = await fetch("./sample/climate.json", { cache: "no-store" });
    const payload = await response.json();
    state.nodes = payload.nodes || [];
    state.matrix = payload.matrix || [];
    finishLoadingProgress(progress);
    renderMetrics(payload);
    populateLocationSelectors();
    renderMatrixLegend();
    renderNodes();
    renderMatrix();
    setStatus(t("statusDone"));
  } catch (error) {
    stopLoadingProgress(progress);
    setStatus(`Load failed: ${error.message}`);
  }
}

function renderMetrics(payload) {
  const prime = state.nodes.filter(node => node.viewingBand === "Prime").length;
  els.nodeCount.textContent = payload.nodeCount ?? state.nodes.length;
  els.geoCount.textContent = payload.geocodedCount ?? "--";
  els.primeCount.textContent = prime;
  els.bestTonight.textContent = state.nodes[0]?.displayName || "--";
}

function populateLocationSelectors() {
  const countries = [...new Set(state.nodes.map(n => n.country).filter(Boolean))].sort();
  els.countrySelect.innerHTML = `<option value="">${escapeHtml(state.lang === "zh" ? "选择国家" : "Select country")}</option>`;
  countries.forEach(country => {
    const option = document.createElement("option");
    option.value = country;
    option.textContent = country;
    els.countrySelect.appendChild(option);
  });
  // Default-select first country for smoother demo
  if (!state.selectedCountry && countries.length) {
    state.selectedCountry = countries[0];
    els.countrySelect.value = state.selectedCountry;
  }
  populateAdminAreaSelect();
  populateCitySelect();
  updateLocationHint();
}

function populateAdminAreaSelect() {
  const areas = state.nodes
    .filter(n => n.country === state.selectedCountry)
    .map(n => String(n.adminArea || ""))
    .filter(Boolean)
    .sort();
  const uniqueAreas = [...new Set(areas)];
  els.adminAreaSelect.innerHTML = `<option value="">${escapeHtml(state.lang === "zh" ? "选择省/州" : "Select province/state")}</option>`;
  uniqueAreas.forEach(area => {
    const option = document.createElement("option");
    option.value = area;
    option.textContent = area;
    els.adminAreaSelect.appendChild(option);
  });
  els.adminAreaSelect.disabled = !state.selectedCountry;
  if (!state.selectedAdminArea && uniqueAreas.length) {
    state.selectedAdminArea = uniqueAreas[0];
    els.adminAreaSelect.value = state.selectedAdminArea;
  }
}

function populateCitySelect() {
  const cities = state.nodes
    .filter(n => n.country === state.selectedCountry && n.adminArea === state.selectedAdminArea)
    .map(n => String(n.city || ""))
    .filter(Boolean)
    .sort();
  const uniqueCities = [...new Set(cities)];
  els.citySelect.innerHTML = `<option value="">${escapeHtml(state.lang === "zh" ? "选择城市" : "Select city")}</option>`;
  uniqueCities.forEach(city => {
    const option = document.createElement("option");
    option.value = city;
    option.textContent = city;
    els.citySelect.appendChild(option);
  });
  els.citySelect.disabled = !state.selectedCountry || !state.selectedAdminArea;
  if (!state.selectedCity && uniqueCities.length) {
    state.selectedCity = uniqueCities[0];
    els.citySelect.value = state.selectedCity;
  }
}

function updateLocationHint() {
  if (!state.selectedCountry) {
    els.locationHint.textContent = t("pickLocation");
    return;
  }
  if (!state.selectedAdminArea) {
    els.locationHint.textContent = t("pickAdmin");
    return;
  }
  if (!state.selectedCity) {
    els.locationHint.textContent = t("pickCity");
    return;
  }
  els.locationHint.textContent = t("pickReady");
}

function getRecommendedNodes() {
  if (!state.selectedCountry || !state.selectedAdminArea || !state.selectedCity) {
    return [];
  }
  return state.nodes
    .filter(n => n.country === state.selectedCountry && n.adminArea === state.selectedAdminArea && n.city === state.selectedCity)
    .slice()
    .sort((a, b) => Number(b.matchScore) - Number(a.matchScore));
}

function renderNodes() {
  const band = els.riskSelect.value;
  const sortKey = els.sortSelect.value;
  const recommended = getRecommendedNodes();
  const rows = recommended
    .filter(n => band === "All" || n.viewingBand === band)
    .slice()
    .sort((a, b) => Number(b[sortKey]) - Number(a[sortKey]))
    .slice(0, 10);

  els.bestTonight.textContent = rows[0]?.displayName || (state.lang === "zh" ? "暂无窗口" : "No window");
  renderRecommendationCards(rows.slice(0, 3));
  els.recordCount.textContent = state.lang === "zh" ? `${rows.length} 个本地站点` : `${rows.length} local sites`;
  els.nodeBody.innerHTML = "";

  if (!state.selectedCountry || !state.selectedAdminArea || !state.selectedCity) {
    els.nodeBody.innerHTML = `<tr><td colspan="8">${escapeHtml(t("emptyTable"))}</td></tr>`;
    return;
  }

  rows.forEach((node, index) => {
    const tr = document.createElement("tr");
    tr.innerHTML = `
      <td class="numeric">${String(index + 1).padStart(2, "0")}</td>
      <td class="city">${escapeHtml(node.displayName || node.city)}<br><span>${escapeHtml([node.country, node.adminArea, node.displayDistrict || node.district].filter(Boolean).join(" · "))}</span><br><em>${escapeHtml(node.advice || "")}</em></td>
      <td>${escapeHtml(node.nodeType || "")}</td>
      <td><span class="pill ${escapeHtml(node.viewingBand)}">${escapeHtml(node.viewingBand)}</span></td>
      <td class="numeric">${Number(node.stargazingScore).toFixed(1)}</td>
      <td class="numeric">${Number(node.temperature).toFixed(1)}°C</td>
      <td class="numeric">${Number(node.wind).toFixed(1)}</td>
      <td class="numeric">${Number(node.precipitation).toFixed(2)}</td>
    `;
    els.nodeBody.appendChild(tr);
  });
}

function renderRecommendationCards(cards) {
  els.recommendationCards.innerHTML = "";
  if (!state.selectedCountry || !state.selectedAdminArea || !state.selectedCity) {
    els.recommendationCards.innerHTML = `<div class="empty-recommendation">${escapeHtml(t("emptyPicks"))}</div>`;
    return;
  }
  cards.forEach((node, index) => {
    const card = document.createElement("article");
    card.className = `recommendation-card rank-${index + 1}`;
    card.innerHTML = `
      <span class="rank">#${index + 1}</span>
      <strong>${escapeHtml(node.displayName || node.city)}</strong>
      <small>${escapeHtml([node.country, node.adminArea, node.displayDistrict || node.district].filter(Boolean).join(" · "))} · ${escapeHtml(node.nodeType || "")}</small>
      <div class="score">${Number(node.stargazingScore).toFixed(1)}</div>
      <div class="weather-chips">
        <span>🌡 ${Number(node.temperature).toFixed(1)}°C</span>
        <span>💨 ${Number(node.wind).toFixed(1)} km/h</span>
        <span>🌧 ${Number(node.precipitation).toFixed(2)} mm</span>
      </div>
      <p>${escapeHtml(node.advice || "")}</p>
      <span class="pill ${escapeHtml(node.viewingBand)}">${escapeHtml(node.viewingBand)}</span>
    `;
    els.recommendationCards.appendChild(card);
  });
}

function heatColor(score) {
  if (score >= 82) return "var(--green)";
  if (score >= 65) return "var(--accent)";
  if (score >= 45) return "var(--warning)";
  return "var(--danger)";
}

function renderMatrixLegend() {
  if (!els.matrixLegend) return;
  const labels = state.lang === "zh"
    ? ["较差", "一般", "良好", "极佳"]
    : ["Poor", "Marginal", "Good", "Prime"];
  els.matrixLegend.innerHTML = `
    <span><i style="background:var(--danger)"></i>${labels[0]}</span>
    <span><i style="background:var(--warning)"></i>${labels[1]}</span>
    <span><i style="background:var(--accent)"></i>${labels[2]}</span>
    <span><i style="background:var(--green)"></i>${labels[3]}</span>
  `;
}

function renderMatrix() {
  if (!els.matrixList) return;
  const maxRisk = Math.max(...state.matrix.map(row => Number(row.maxRisk)), 1);
  els.matrixList.innerHTML = "";
  state.matrix.forEach(row => {
    const avgSky = Math.max(0, 100 - Number(row.avgRisk));
    const riskPct = Number(row.maxRisk / maxRisk * 100);
    const cell = document.createElement("article");
    cell.className = "heatmap-cell";
    cell.style.setProperty("--heat", heatColor(avgSky));
    cell.innerHTML = `
      <strong>${escapeHtml(row.region)}</strong>
      <div class="heat-swatch" style="background:${heatColor(avgSky)}">${avgSky.toFixed(0)}</div>
      <span>${row.count} ${escapeHtml(state.lang === "zh" ? "节点" : "nodes")}</span>
      <small>sky ${avgSky.toFixed(1)} · wind ${Number(row.avgWind).toFixed(1)} · risk ${riskPct.toFixed(0)}%</small>
    `;
    els.matrixList.appendChild(cell);
  });
}

async function runRedTeam() {
  const response = await fetch("./sample/red-team.json", { cache: "no-store" });
  const payload = await response.json();
  els.defenseList.innerHTML = "";
  payload.tests.forEach(test => {
    const row = document.createElement("div");
    row.className = "defense-row";
    row.innerHTML = `<strong>${escapeHtml(test.input)}</strong><br><span class="Poor">${escapeHtml(test.status)}</span>`;
    els.defenseList.appendChild(row);
  });
  els.defenseList.scrollIntoView({ behavior: "smooth", block: "nearest" });
}

function exportClimate() {
  const rows = getRecommendedNodes();
  const headers = ["name", "country", "adminArea", "district", "score", "tempC", "wind", "precip", "band"];
  const lines = [
    headers.join(","),
    ...rows.map(n => ([
      n.displayName || n.city,
      n.country,
      n.adminArea,
      n.displayDistrict || n.district || "",
      Number(n.stargazingScore).toFixed(1),
      Number(n.temperature).toFixed(1),
      Number(n.wind).toFixed(1),
      Number(n.precipitation).toFixed(2),
      n.viewingBand
    ].map(v => `"${String(v).replaceAll('"', '""')}"`).join(",")))
  ];
  const blob = new Blob([lines.join("\n")], { type: "text/csv;charset=utf-8" });
  const url = URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.href = url;
  link.download = "starscope_demo_export.csv";
  document.body.appendChild(link);
  link.click();
  link.remove();
  URL.revokeObjectURL(url);
  setStatus(t("exportReady"));
}

function startLoadingProgress() {
  els.loadingOverlay.classList.remove("hidden");
  let percent = 0;
  const timer = setInterval(() => {
    percent = Math.min(92, percent + Math.random() * 10 + 4);
    updateLoadingBar(percent);
  }, 260);
  return {
    timer,
    setComplete() {
      clearInterval(timer);
      updateLoadingBar(100);
      setTimeout(() => els.loadingOverlay.classList.add("hidden"), 350);
    },
    stop() {
      clearInterval(timer);
      els.loadingOverlay.classList.add("hidden");
    }
  };
}

function finishLoadingProgress(progress) { progress.setComplete(); }
function stopLoadingProgress(progress) { progress.stop(); }

function updateLoadingBar(percent) {
  const value = Math.round(percent);
  els.loadingBar.style.width = `${value}%`;
  els.loadingPercent.textContent = `${value}%`;
  els.loadingText.textContent = t("statusFetching");
}

function skyCenter(azimuthDeg, altitudeDeg, radius = 82) {
  const azimuth = azimuthDeg * Math.PI / 180;
  const altitude = altitudeDeg * Math.PI / 180;
  const horizontal = Math.cos(altitude) * radius;
  return [
    Math.sin(azimuth) * horizontal,
    Math.sin(altitude) * radius - 8,
    -Math.cos(azimuth) * horizontal
  ];
}

function makeConstellation(config) {
  return {
    ...config,
    points: config.pattern.map(([x, y, z = 0]) => [
      config.center[0] + x * config.size,
      config.center[1] + y * config.size,
      config.center[2] + z * config.size * 0.5
    ])
  };
}

const constellations = [
  makeConstellation({
    name: { zh: "猎户座", en: "Orion" },
    story: {
      zh: "腰带三颗星排成直线，腰带下方可见猎户座大星云。",
      en: "Orion is a beginner-friendly target. The belt stars form a straight line."
    },
    science: {
      zh: "参宿四偏红、参宿七偏蓝，适合讲解恒星温度差异。",
      en: "Use Betelgeuse vs Rigel to teach stellar temperatures."
    },
    color: 0xffc66d,
    size: 5.4,
    center: skyCenter(-8, 22, 78),
    pattern: [[-3, 3], [-1, 1], [0, 0.7], [1, 1], [3, 3], [-1.5, -1], [1.5, -1], [-3, -4], [3, -4]],
    lines: [[0, 1], [1, 2], [2, 3], [3, 4], [1, 5], [3, 6], [5, 7], [6, 8]]
  }),
  makeConstellation({
    name: { zh: "天蝎座", en: "Scorpius" },
    story: {
      zh: "弯曲的长尾与红色亮星心宿二是最醒目的特征。",
      en: "Scorpius has a long curved tail and the bright red star Antares."
    },
    science: {
      zh: "心宿二是红超巨星，低空目标更受地平线云雾影响。",
      en: "Antares is a red supergiant; horizon haze matters a lot."
    },
    color: 0xff7d7d,
    size: 5.8,
    center: skyCenter(92, 14, 82),
    pattern: [[-2, 3], [-1, 1.3], [0, 0], [1, -1.5], [0.5, -3.5], [-1, -4.7], [-2.8, -4], [-3.3, -2.2]],
    lines: [[0, 1], [1, 2], [2, 3], [3, 4], [4, 5], [5, 6], [6, 7]]
  }),
  makeConstellation({
    name: { zh: "天鹅座", en: "Cygnus" },
    story: {
      zh: "北十字星群沿银河分布，暗空下尤其美丽。",
      en: "Cygnus forms the Northern Cross along the Milky Way."
    },
    science: {
      zh: "天津四与织女星、牛郎星组成夏季大三角。",
      en: "Deneb is part of the Summer Triangle with Vega and Altair."
    },
    color: 0xa6f0c6,
    size: 4.9,
    center: skyCenter(16, 66, 75),
    pattern: [[0, 3], [0, 1], [0, -1.5], [-2.8, 0.7], [2.8, 0.7]],
    lines: [[0, 1], [1, 2], [1, 3], [1, 4]]
  })
];

function localizedConstellationName(c) {
  return state.lang === "zh" ? c.name.zh : c.name.en;
}

function localizedConstellationText(c, field) {
  return state.lang === "zh" ? c[field].zh : c[field].en;
}

function constellationButtons() {
  els.constellationButtons.innerHTML = "";
  constellations.forEach(c => {
    const button = document.createElement("button");
    button.type = "button";
    button.dataset.constellationId = c.name.en;
    button.textContent = localizedConstellationName(c);
    button.addEventListener("click", () => selectConstellation(c.name.en));
    els.constellationButtons.appendChild(button);
  });
}

function selectConstellation(nameEn) {
  const c = constellations.find(item => item.name.en === nameEn) || constellations[0];
  state.selectedConstellation = c.name.en;
  highlightConstellation(c.name.en);
  [...els.constellationButtons.querySelectorAll("button")].forEach(btn => {
    btn.classList.toggle("is-active", btn.dataset.constellationId === c.name.en);
  });
  els.constellationInfo.innerHTML = `
    <strong>${escapeHtml(localizedConstellationName(c))}</strong>
    <span>${escapeHtml(localizedConstellationText(c, "story"))}</span>
    <p>${escapeHtml(localizedConstellationText(c, "science"))}</p>
  `;
}

function initStarDome() {
  if (state.dome || !els.starDome?.offsetWidth) return;

  const scene = new THREE.Scene();
  const camera = new THREE.PerspectiveCamera(58, els.starDome.clientWidth / els.starDome.clientHeight, 0.1, 1000);
  camera.position.set(0, 0, 178);

  const renderer = new THREE.WebGLRenderer({ canvas: els.starDome, antialias: true, alpha: true });
  renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));
  renderer.setSize(els.starDome.clientWidth, els.starDome.clientHeight, false);

  const root = new THREE.Group();
  root.position.y = -18;
  root.scale.setScalar(0.9);
  scene.add(root);

  const dome = new THREE.Mesh(
    new THREE.SphereGeometry(104, 48, 24, 0, Math.PI * 2, 0, Math.PI / 2),
    new THREE.MeshBasicMaterial({ color: 0x132235, transparent: true, opacity: 0.32, wireframe: true })
  );
  dome.rotation.x = Math.PI;
  root.add(dome);

  const starGeometry = new THREE.BufferGeometry();
  const positions = [];
  for (let i = 0; i < 360; i++) {
    const theta = Math.random() * Math.PI * 2;
    const phi = Math.random() * Math.PI * 0.46;
    const radius = 96 + Math.random() * 4;
    positions.push(
      Math.cos(theta) * Math.sin(phi) * radius,
      Math.cos(phi) * radius - 18,
      Math.sin(theta) * Math.sin(phi) * radius
    );
  }
  starGeometry.setAttribute("position", new THREE.Float32BufferAttribute(positions, 3));
  root.add(new THREE.Points(starGeometry, new THREE.PointsMaterial({ color: 0xffffff, size: 1.25, sizeAttenuation: true })));

  const rayTargets = [];
  const constellationObjects = new Map();
  constellationButtons();

  constellations.forEach(constellation => {
    const objects = { color: constellation.color, lines: [], markers: [] };
    const lineMaterial = new THREE.LineBasicMaterial({ color: 0xffffff, transparent: true, opacity: 0.56 });
    const markerMaterial = new THREE.MeshBasicMaterial({ color: 0xffffff, transparent: true, opacity: 0.86 });

    constellation.lines.forEach(([a, b]) => {
      const geometry = new THREE.BufferGeometry().setFromPoints([
        new THREE.Vector3(...constellation.points[a]),
        new THREE.Vector3(...constellation.points[b])
      ]);
      const line = new THREE.Line(geometry, lineMaterial.clone());
      line.userData.constellation = constellation.name.en;
      objects.lines.push(line);
      root.add(line);
    });

    constellation.points.forEach(point => {
      const marker = new THREE.Mesh(
        new THREE.SphereGeometry(Math.max(1.25, Math.min(2.7, constellation.size * 0.38)), 16, 16),
        markerMaterial.clone()
      );
      marker.position.set(...point);
      marker.userData.constellation = constellation.name.en;
      rayTargets.push(marker);
      objects.markers.push(marker);
      root.add(marker);
    });

    constellationObjects.set(constellation.name.en, objects);
  });

  let dragging = false;
  let lastX = 0;
  const raycaster = new THREE.Raycaster();
  const mouse = new THREE.Vector2();

  els.starDome.addEventListener("pointerdown", event => {
    dragging = true;
    lastX = event.clientX;
  });
  window.addEventListener("pointerup", () => dragging = false);
  window.addEventListener("pointermove", event => {
    if (!dragging) return;
    root.rotation.y += (event.clientX - lastX) * 0.006;
    lastX = event.clientX;
  });

  els.starDome.addEventListener("pointermove", event => {
    if (dragging) return;
    const rect = els.starDome.getBoundingClientRect();
    mouse.set(((event.clientX - rect.left) / rect.width) * 2 - 1, -((event.clientY - rect.top) / rect.height) * 2 + 1);
    raycaster.setFromCamera(mouse, camera);
    const hit = raycaster.intersectObjects(rayTargets)[0];
    highlightConstellation(hit ? hit.object.userData.constellation : state.selectedConstellation);
    els.starDome.style.cursor = hit ? "pointer" : "grab";
  });

  els.starDome.addEventListener("mouseleave", () => highlightConstellation(state.selectedConstellation));

  els.starDome.addEventListener("click", event => {
    const rect = els.starDome.getBoundingClientRect();
    mouse.set(((event.clientX - rect.left) / rect.width) * 2 - 1, -((event.clientY - rect.top) / rect.height) * 2 + 1);
    raycaster.setFromCamera(mouse, camera);
    const hit = raycaster.intersectObjects(rayTargets)[0];
    if (hit) selectConstellation(hit.object.userData.constellation);
  });

  window.addEventListener("resize", () => {
    if (!els.starDome.clientWidth) return;
    camera.aspect = els.starDome.clientWidth / els.starDome.clientHeight;
    camera.updateProjectionMatrix();
    renderer.setSize(els.starDome.clientWidth, els.starDome.clientHeight, false);
  });

  function animate() {
    root.rotation.y += 0.0012;
    renderer.render(scene, camera);
    requestAnimationFrame(animate);
  }
  animate();

  state.dome = { scene, camera, renderer, dome, constellationObjects };
  applyDomeTheme();
  selectConstellation(state.selectedConstellation);
}

function highlightConstellation(nameEn) {
  if (!state.dome) return;
  for (const [id, group] of state.dome.constellationObjects.entries()) {
    const active = id === nameEn || id === state.selectedConstellation;
    const color = active ? group.color : 0xffffff;
    group.lines.forEach(line => {
      line.material.color.setHex(color);
      line.material.opacity = active ? 1 : 0.5;
    });
    group.markers.forEach(marker => {
      marker.material.color.setHex(color);
      marker.material.opacity = active ? 1 : 0.78;
      marker.scale.setScalar(id === nameEn ? 1.48 : id === state.selectedConstellation ? 1.22 : 1);
    });
  }
}

function applyDomeTheme() {
  if (!state.dome) return;
  const isLight = document.body.dataset.theme === "light";
  state.dome.renderer.setClearColor(isLight ? 0xf8fbff : 0x02040a, isLight ? 1 : 0);
  state.dome.dome.material.color.setHex(isLight ? 0x8fb4e8 : 0x132235);
  state.dome.dome.material.opacity = isLight ? 0.22 : 0.32;
  state.dome.scene.background = isLight ? new THREE.Color(0xf8fbff) : null;
}

// Wire up UI
setTheme(localStorage.getItem("starscope-theme") || "dark");
els.themeToggle.addEventListener("click", toggleTheme);
els.loginThemeToggle.addEventListener("click", toggleTheme);
els.langToggle.addEventListener("click", toggleLang);
els.loginLangToggle.addEventListener("click", toggleLang);
els.refreshBtn.addEventListener("click", loadDemoClimate);
els.exportBtn.addEventListener("click", exportClimate);
els.redTeamBtn.addEventListener("click", runRedTeam);
els.dashboardNavBtn.addEventListener("click", () => setView("dashboard"));
els.profileNavBtn.addEventListener("click", () => setView("profile"));
els.logoutBtn.addEventListener("click", () => {
  // In demo, "logout" just returns to login view.
  els.appView.classList.add("hidden");
  els.loginView.classList.remove("hidden");
});

els.countrySelect.addEventListener("change", () => {
  state.selectedCountry = els.countrySelect.value;
  state.selectedAdminArea = "";
  state.selectedCity = "";
  populateAdminAreaSelect();
  populateCitySelect();
  updateLocationHint();
  renderNodes();
});

els.adminAreaSelect.addEventListener("change", () => {
  state.selectedAdminArea = els.adminAreaSelect.value;
  state.selectedCity = "";
  populateCitySelect();
  updateLocationHint();
  renderNodes();
});

els.citySelect.addEventListener("change", () => {
  state.selectedCity = els.citySelect.value;
  updateLocationHint();
  renderNodes();
});

els.sortSelect.addEventListener("change", renderNodes);
els.riskSelect.addEventListener("change", renderNodes);

els.loginModeBtn.addEventListener("click", () => {
  els.loginModeBtn.classList.add("is-active");
  els.signupModeBtn.classList.remove("is-active");
  els.confirmPasswordLabel.classList.add("hidden");
  els.passwordRule.classList.add("hidden");
  els.authTitle.textContent = state.lang === "zh" ? "安全登录" : "Secure Login";
  els.authSubmitBtn.textContent = state.lang === "zh" ? "进入仪表盘" : "Enter Dashboard";
});

els.signupModeBtn.addEventListener("click", () => {
  els.signupModeBtn.classList.add("is-active");
  els.loginModeBtn.classList.remove("is-active");
  els.confirmPasswordLabel.classList.remove("hidden");
  els.passwordRule.classList.remove("hidden");
  els.authTitle.textContent = state.lang === "zh" ? "创建账号" : "Create Account";
  els.authSubmitBtn.textContent = state.lang === "zh" ? "进入仪表盘" : "Enter Dashboard";
});

els.loginForm.addEventListener("submit", async event => {
  event.preventDefault();
  // Demo mode: any username/password enters.
  const data = new FormData(els.loginForm);
  const username = String(data.get("username") || "demo").trim() || "demo";
  state.user = { username, role: "USER" };
  els.loginError.textContent = "";
  showApp();
  await loadDemoClimate();
});

// Start in login view, but pre-fill a smoother demo path.
els.loginForm.querySelector("input[name='username']").value = "demo";
showApp();
loadDemoClimate();
