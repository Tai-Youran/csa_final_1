import * as THREE from "https://unpkg.com/three@0.160.0/build/three.module.js";

const state = {
  user: null,
  nodes: [],
  matrix: [],
  dome: null,
  authMode: "login",
  selectedCountry: "",
  selectedAdminArea: "",
  selectedCity: "",
  lang: localStorage.getItem("starscope-lang") || "zh"
};

const LOCATION_KEY = "starscope-location";

const I18N = {
  zh: {
    loginEyebrow: "实时天文观测规划",
    loginLede: "基于角色的观星雷达，把实时天气转化为今晚最佳观测窗口。",
    heroWeather: "动态天气", heroRoles: "角色权限", heroDome: "3D 星座穹顶",
    tabLogin: "登录", tabSignup: "注册", authLogin: "安全登录", authSignup: "创建账号",
    labelUsername: "用户名", labelPassword: "密码", labelConfirm: "确认密码",
    passwordRule: "密码至少 6 位，且同时包含字母和数字。",
    btnEnter: "进入仪表盘", btnSignup: "创建普通用户", btnLogout: "退出",
    demoTitle: "演示账号", demoNote: "新注册用户默认为普通用户。",
    appEyebrow: "StarScope 观测雷达", appTitle: "今晚观星窗口",
    metricNodes: "预报节点", metricGeo: "地理编码池", metricPrime: "优质窗口", metricBest: "今晚最佳",
    locEyebrow: "从位置开始", locTitle: "你在哪里观测？",
    labelCountry: "国家", labelAdmin: "省 / 州", labelCity: "城市",
    optCountry: "选择国家", optAdmin: "选择省/州", optCity: "选择城市",
    topPicks: "本地 Top 3 推荐", topPicksTag: "同城",
    labelSort: "排序", labelBand: "观星等级",
    sortMatch: "最佳匹配", sortScore: "观星评分", sortRisk: "天气阻碍",
    sortWind: "风速", sortRain: "降水量", sortTemp: "温度",
    bandAll: "全部", bandPrime: "极佳", bandGood: "良好", bandMarginal: "一般", bandPoor: "较差",
    btnRefresh: "刷新预报", btnRedTeam: "红队测试",
    domeEyebrow: "交互式星座穹顶", domeTitle: "3D 夜空地图",
    domeDesc: "拖动旋转穹顶。星座按相对大小显示，点击可查看观测说明。",
    tableTitle: "推荐观测点", thSite: "地点", thType: "类型", thViewing: "等级",
    thScore: "评分", thTemp: "温度", thWind: "风速", thRain: "降水",
    matrixTitle: "天空质量热力图", matrixTag: "2D 矩阵",
    loadingGeo: "正在地理编码…", loadingWeather: "正在获取 72+ 节点天气…",
    loadingScore: "正在计算观星评分…", loadingMatrix: "正在生成热力矩阵…",
    loadingDone: "预报更新完成", loadingError: "加载失败",
    statusWait: "等待登录…", statusFetching: "正在获取实时天气…", statusDone: "观星评分已更新",
    statusRedTeam: "红队测试已通过 InputSanitizer 过滤",
    noWindows: "暂无窗口", selectLocation: "请选择位置",
    pickLocation: "请先选择国家、省/州和城市，系统将推荐附近最佳观星点。",
    pickAdmin: "请选择 {country} 的省/州以缩小范围。",
    pickCity: "请选择 {admin}（{country}）的城市。",
    pickReady: "已基于 {city} 的实时天气推荐本地观测点。",
    emptyPicks: "选择国家、省/州和城市后解锁 Top 3 推荐。",
    emptyTable: "请先选择国家、省/州和城市以生成本地观测点。",
    localSites: "{count} 个本地站点", nodes: "节点",
    bestWindowPrime: "21:00 – 23:30 最佳", bestWindowGood: "20:30 – 22:30 较好",
    bestWindowMarginal: "22:00 – 23:00 勉强可看", bestWindowPoor: "今晚条件较差",
    moonNew: "新月", moonWax: "盈月", moonFull: "满月", moonWan: "亏月",
    strengthWeak: "弱", strengthMedium: "中", strengthStrong: "强",
    legendPoor: "较差", legendMid: "一般", legendGood: "良好", legendPrime: "极佳",
    matrixSky: "天空分", matrixWind: "均风", matrixRisk: "阻碍",
    cMetaSeason: "季节", cMetaHemisphere: "半球", cMetaBestMonths: "最佳月份",
    cMetaKeyStars: "主要恒星", cMetaRelativeSize: "相对大小", cSelectHint: "点击星座查看观测说明",
    cSizeScale: "倍穹顶比例"
  },
  en: {
    loginEyebrow: "Live astronomy planning intelligence",
    loginLede: "A role-based stargazing radar that turns live weather into tonight's best observation windows.",
    heroWeather: "Dynamic weather", heroRoles: "Role-based backend", heroDome: "3D constellation dome",
    tabLogin: "Login", tabSignup: "Sign Up", authLogin: "Secure Login", authSignup: "Create Account",
    labelUsername: "Username", labelPassword: "Password", labelConfirm: "Confirm Password",
    passwordRule: "Password must be at least 6 characters and include both letters and numbers.",
    btnEnter: "Enter Dashboard", btnSignup: "Create USER Account", btnLogout: "Logout",
    demoTitle: "Demo accounts", demoNote: "New signups become USER accounts.",
    appEyebrow: "StarScope Observation Radar", appTitle: "Tonight's Sky Window",
    metricNodes: "Forecast Nodes", metricGeo: "Geocoded Pool", metricPrime: "Prime Windows", metricBest: "Best Tonight",
    locEyebrow: "Start with your location", locTitle: "Where are you observing from?",
    labelCountry: "Country", labelAdmin: "Province / State", labelCity: "City",
    optCountry: "Select country", optAdmin: "Select province/state", optCity: "Select city",
    topPicks: "Top 3 Local Picks", topPicksTag: "City-local",
    labelSort: "Sort", labelBand: "Viewing Band",
    sortMatch: "Best Match", sortScore: "Stargazing Score", sortRisk: "Weather Obstruction",
    sortWind: "Wind Speed", sortRain: "Precipitation", sortTemp: "Temperature",
    bandAll: "All", bandPrime: "Prime", bandGood: "Good", bandMarginal: "Marginal", bandPoor: "Poor",
    btnRefresh: "Refresh Forecast", btnRedTeam: "Red Team",
    domeEyebrow: "Interactive constellation dome", domeTitle: "3D Night Sky Map",
    domeDesc: "Drag to rotate the dome. Constellations scale by size — click for observing notes.",
    tableTitle: "Recommended Observation Sites", thSite: "Site", thType: "Type", thViewing: "Viewing",
    thScore: "Score", thTemp: "Temp", thWind: "Wind", thRain: "Rain",
    matrixTitle: "Sky Quality Heatmap", matrixTag: "2D array",
    loadingGeo: "Geocoding observation pool…", loadingWeather: "Fetching 72+ weather nodes…",
    loadingScore: "Calculating stargazing scores…", loadingMatrix: "Building heat matrix…",
    loadingDone: "Forecast update complete", loadingError: "Load failed",
    statusWait: "Waiting for login…", statusFetching: "Fetching live weather…", statusDone: "Stargazing scores updated",
    statusRedTeam: "Red-team payloads routed through InputSanitizer",
    noWindows: "No windows", selectLocation: "Select location",
    pickLocation: "Choose country, province/state, and city first.",
    pickAdmin: "Choose a province/state in {country}.",
    pickCity: "Choose a city in {admin}, {country}.",
    pickReady: "Recommendations use live {city} weather as baseline.",
    emptyPicks: "Select country, province/state, and city to unlock Top 3 picks.",
    emptyTable: "Select country, province/state, and city first.",
    localSites: "{count} local sites", nodes: "nodes",
    bestWindowPrime: "21:00 – 23:30 best", bestWindowGood: "20:30 – 22:30 good",
    bestWindowMarginal: "22:00 – 23:00 marginal", bestWindowPoor: "Poor conditions tonight",
    moonNew: "New moon", moonWax: "Waxing", moonFull: "Full moon", moonWan: "Waning",
    strengthWeak: "Weak", strengthMedium: "Medium", strengthStrong: "Strong",
    legendPoor: "Poor", legendMid: "Marginal", legendGood: "Good", legendPrime: "Prime",
    matrixSky: "sky", matrixWind: "wind", matrixRisk: "risk",
    cMetaSeason: "Season", cMetaHemisphere: "Hemisphere", cMetaBestMonths: "Best months",
    cMetaKeyStars: "Key stars", cMetaRelativeSize: "Relative size", cSelectHint: "Select a constellation to view its observing notes",
    cSizeScale: "x dome scale"
  }
};

function t(key, vars = {}) {
  let text = I18N[state.lang]?.[key] ?? I18N.en[key] ?? key;
  Object.entries(vars).forEach(([k, v]) => {
    text = text.replace(`{${k}}`, v);
  });
  return text;
}

function applyI18n() {
  document.querySelectorAll("[data-i18n]").forEach(el => {
    const key = el.getAttribute("data-i18n");
    if (key) el.textContent = t(key);
  });
  const langLabel = state.lang === "zh" ? "EN" : "中文";
  if (els.langToggle) els.langToggle.textContent = langLabel;
  if (els.loginLangToggle) els.loginLangToggle.textContent = langLabel;
  if (state.authMode === "signup") {
    els.authTitle.textContent = t("authSignup");
    els.authSubmitBtn.textContent = t("btnSignup");
  } else {
    els.authTitle.textContent = t("authLogin");
    els.authSubmitBtn.textContent = t("btnEnter");
  }
  updateLocationHint();
  renderMatrixLegend();
  if (state.nodes.length) renderNodes();
  if (!state.user && els.statusLine) els.statusLine.textContent = t("statusWait");
  constellationButtons();
  if (state.selectedConstellation) {
    selectConstellation(state.selectedConstellation);
  } else if (els.constellationInfo) {
    els.constellationInfo.innerHTML = `
      <strong>Orion</strong>
      <span>${escapeHtml(t("cSelectHint"))}</span>
    `;
  }
}

function toggleLang() {
  state.lang = state.lang === "zh" ? "en" : "zh";
  localStorage.setItem("starscope-lang", state.lang);
  applyI18n();
}

function bandLabel(band) {
  const map = { Prime: t("bandPrime"), Good: t("bandGood"), Marginal: t("bandMarginal"), Poor: t("bandPoor"), All: t("bandAll") };
  return map[band] || band;
}

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
  redTeamBtn: $("#redTeamBtn"),
  statusLine: $("#statusLine"),
  recordCount: $("#recordCount"),
  nodeBody: $("#nodeBody"),
  matrixList: $("#matrixList"),
  matrixLegend: $("#matrixLegend"),
  adminPanel: $("#adminPanel"),
  userPanel: $("#userPanel"),
  auditList: $("#auditList"),
  defenseList: $("#defenseList"),
  constellationInfo: $("#constellationInfo"),
  constellationButtons: $("#constellationButtons"),
  starDome: $("#starDome"),
  starsWord: $("#starsWord"),
  starsGrid: $("#starsGrid"),
  langToggle: $("#langToggle"),
  loginLangToggle: $("#loginLangToggle"),
  loadingOverlay: $("#loadingOverlay"),
  loadingText: $("#loadingText"),
  loadingBar: $("#loadingBar"),
  loadingPercent: $("#loadingPercent"),
  passwordInput: $("#passwordInput"),
  passwordStrength: $("#passwordStrength"),
  strengthFill: $("#strengthFill"),
  strengthLabel: $("#strengthLabel")
};

setTheme(localStorage.getItem("starscope-theme") || "dark");

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
    name: "Orion",
    story: "Orion is one of the easiest beginner targets. The three belt stars form a straight line, and the Orion Nebula sits below the belt.",
    science: "Use Orion to teach stellar color: red Betelgeuse and blue Rigel show different star temperatures.",
    season: "Winter",
    hemisphere: "Both",
    bestMonths: "December to February",
    keyStars: "Betelgeuse, Rigel, Bellatrix",
    color: 0xffc66d,
    size: 5.4,
    center: [-16, 8, -78],
    pattern: [[-3, 3], [-1, 1], [0, 0.7], [1, 1], [3, 3], [-1.5, -1], [1.5, -1], [-3, -4], [3, -4]],
    lines: [[0, 1], [1, 2], [2, 3], [3, 4], [1, 5], [3, 6], [5, 7], [6, 8]]
  }),
  makeConstellation({
    name: "Ursa Major",
    story: "Ursa Major contains the Big Dipper asterism. The two outer bowl stars point toward Polaris, the North Star.",
    science: "Great for navigation: students can use Dubhe and Merak as pointer stars to find north.",
    season: "Spring",
    hemisphere: "Northern",
    bestMonths: "March to June",
    keyStars: "Dubhe, Merak, Alioth, Mizar",
    color: 0x8fd3ff,
    size: 6.6,
    center: [-45, 34, -55],
    pattern: [[-4, 0], [-2, 1], [0, 0.7], [2, -0.2], [4, 1.3], [2.4, 3.1], [0, 2.8]],
    lines: [[0, 1], [1, 2], [2, 3], [3, 4], [4, 5], [5, 6], [6, 2]]
  }),
  makeConstellation({
    name: "Cassiopeia",
    story: "Cassiopeia is a compact W-shaped constellation opposite the Big Dipper around Polaris.",
    science: "Its W shape is useful when Ursa Major is low; it helps students keep orientation in the northern sky.",
    season: "Autumn",
    hemisphere: "Northern",
    bestMonths: "September to November",
    keyStars: "Schedar, Caph, Ruchbah",
    color: 0xd7b7ff,
    size: 4.3,
    center: [50, 45, -54],
    pattern: [[-4, 1], [-2, -1], [0, 1.1], [2, -1], [4, 0.9]],
    lines: [[0, 1], [1, 2], [2, 3], [3, 4]]
  }),
  makeConstellation({
    name: "Scorpius",
    story: "Scorpius has a long curved tail and the bright red star Antares near its heart.",
    science: "Antares is a red supergiant. The constellation sits low for many observers, so haze and horizon clouds matter.",
    season: "Summer",
    hemisphere: "Southern / low Northern",
    bestMonths: "June to August",
    keyStars: "Antares, Shaula, Sargas",
    color: 0xff7d7d,
    size: 5.8,
    center: [32, -28, -72],
    pattern: [[-2, 3], [-1, 1.3], [0, 0], [1, -1.5], [0.5, -3.5], [-1, -4.7], [-2.8, -4], [-3.3, -2.2]],
    lines: [[0, 1], [1, 2], [2, 3], [3, 4], [4, 5], [5, 6], [6, 7]]
  }),
  makeConstellation({
    name: "Cygnus",
    story: "Cygnus forms the Northern Cross and lies along the Milky Way, making it beautiful under dark skies.",
    science: "Its bright star Deneb is one corner of the Summer Triangle with Vega and Altair.",
    season: "Summer",
    hemisphere: "Northern",
    bestMonths: "July to September",
    keyStars: "Deneb, Sadr, Albireo",
    color: 0xa6f0c6,
    size: 4.9,
    center: [-4, 46, -66],
    pattern: [[0, 3], [0, 1], [0, -1.5], [-2.8, 0.7], [2.8, 0.7]],
    lines: [[0, 1], [1, 2], [1, 3], [1, 4]]
  }),
  makeConstellation({
    name: "Leo",
    story: "Leo is recognized by the sickle-shaped head and the bright star Regulus.",
    science: "Leo is a good spring target and helps introduce the ecliptic, where the Sun, Moon, and planets appear to travel.",
    season: "Spring",
    hemisphere: "Both / Northern best",
    bestMonths: "March to May",
    keyStars: "Regulus, Denebola, Algieba",
    color: 0xffdf80,
    size: 4.8,
    center: [6, 20, -86],
    pattern: [[-4, -1], [-2, 0], [0, -0.5], [2, 0.7], [4, 0], [1.2, 2], [0, 3], [-1.4, 2.3]],
    lines: [[0, 1], [1, 2], [2, 3], [3, 4], [3, 5], [5, 6], [6, 7], [7, 2]]
  }),
  makeConstellation({
    name: "Taurus",
    story: "Taurus includes the V-shaped Hyades cluster and points toward the Pleiades nearby.",
    science: "Aldebaran appears in front of the Hyades cluster; it is not physically part of the cluster.",
    season: "Winter",
    hemisphere: "Both",
    bestMonths: "November to February",
    keyStars: "Aldebaran, Elnath, Hyades",
    color: 0xffa987,
    size: 4.1,
    center: [-58, -8, -68],
    pattern: [[-3, 1], [-1, 0], [0, -1], [1, 0], [3, 1], [-4, 3], [4, 3]],
    lines: [[0, 1], [1, 2], [2, 3], [3, 4], [1, 5], [3, 6]]
  }),
  makeConstellation({
    name: "Gemini",
    story: "Gemini shows two nearly parallel star chains representing the twins Castor and Pollux.",
    science: "Castor is actually a multiple-star system, which is a great topic for explaining binary stars.",
    season: "Winter",
    hemisphere: "Northern",
    bestMonths: "December to March",
    keyStars: "Castor, Pollux, Alhena",
    color: 0x9ecbff,
    size: 3.9,
    center: [-32, -24, -78],
    pattern: [[-2, 2], [-2, 0.5], [-1.8, -1], [-1.4, -2.6], [2, 2], [1.8, 0.5], [1.4, -1], [1.1, -2.6]],
    lines: [[0, 1], [1, 2], [2, 3], [4, 5], [5, 6], [6, 7], [1, 5]]
  }),
  makeConstellation({
    name: "Lyra",
    story: "Lyra is small but important because it contains Vega, one of the brightest stars in the sky.",
    science: "Vega is part of the Summer Triangle and was once used as a standard for measuring stellar brightness.",
    season: "Summer",
    hemisphere: "Northern",
    bestMonths: "June to September",
    keyStars: "Vega, Sheliak, Sulafat",
    color: 0xc0e7ff,
    size: 2.6,
    center: [28, 25, -88],
    pattern: [[0, 3], [-1.2, 0.8], [1.2, 0.8], [1.1, -1.2], [-1.1, -1.2]],
    lines: [[0, 1], [0, 2], [1, 4], [4, 3], [3, 2]]
  }),
  makeConstellation({
    name: "Aquila",
    story: "Aquila contains Altair, another corner of the Summer Triangle.",
    science: "Altair rotates very quickly, making it slightly flattened rather than perfectly spherical.",
    season: "Summer",
    hemisphere: "Both",
    bestMonths: "July to September",
    keyStars: "Altair, Tarazed, Alshain",
    color: 0xb8f7dc,
    size: 3.8,
    center: [58, 4, -70],
    pattern: [[0, 3], [-1, 1], [0, 0], [1, 1], [0, -2.5], [-2, -1.2], [2, -1.2]],
    lines: [[0, 1], [1, 2], [2, 3], [3, 0], [2, 4], [4, 5], [4, 6]]
  }),
  makeConstellation({
    name: "Pegasus",
    story: "Pegasus is known for the Great Square, a large autumn asterism.",
    science: "The Great Square is a useful sky ruler: its size helps students understand angular distance.",
    season: "Autumn",
    hemisphere: "Northern",
    bestMonths: "September to November",
    keyStars: "Markab, Scheat, Algenib, Alpheratz",
    color: 0xbda7ff,
    size: 6.1,
    center: [0, -45, -62],
    pattern: [[-2, 2], [2, 2], [2, -2], [-2, -2], [4, -3.3], [5, -4.8]],
    lines: [[0, 1], [1, 2], [2, 3], [3, 0], [2, 4], [4, 5]]
  }),
  makeConstellation({
    name: "Crux",
    story: "Crux, the Southern Cross, is small but iconic in the southern sky.",
    science: "Southern observers use Crux and nearby pointer stars to estimate the direction of the south celestial pole.",
    season: "Autumn / Southern",
    hemisphere: "Southern",
    bestMonths: "April to June",
    keyStars: "Acrux, Mimosa, Gacrux",
    color: 0xffffff,
    size: 2.5,
    center: [70, -42, -58],
    pattern: [[0, 2], [0, -2], [-1.2, 0], [1.2, 0]],
    lines: [[0, 1], [2, 3]]
  })
];

const CONSTELLATION_I18N = {
  Orion: {
    name: "猎户座",
    story: "猎户座是最适合初学者的目标之一。腰带三颗星排成直线，腰带下方可见猎户座大星云。",
    science: "可用猎户座讲解恒星颜色：红色参宿四与蓝色参宿七代表不同恒星温度。",
    season: "冬季", hemisphere: "南北半球均可", bestMonths: "12 月 – 2 月", keyStars: "参宿四、参宿七、参宿五"
  },
  "Ursa Major": {
    name: "大熊座",
    story: "大熊座包含北斗七星。勺口外侧两颗指极星可用于寻找北极星。",
    science: "适合讲解导航：可用天枢、天璇作为指极星找到北方。",
    season: "春季", hemisphere: "北半球", bestMonths: "3 月 – 6 月", keyStars: "天枢、天璇、玉衡、开阳"
  },
  Cassiopeia: {
    name: "仙后座",
    story: "仙后座呈紧凑的 W 形，与北斗七星围绕北极星相对分布。",
    science: "当大熊座位置较低时，W 形有助于在北天保持方向感。",
    season: "秋季", hemisphere: "北半球", bestMonths: "9 月 – 11 月", keyStars: "王良四、阁道三、阁道二"
  },
  Scorpius: {
    name: "天蝎座",
    story: "天蝎座有弯曲的长尾，心宿二这颗明亮红星位于“心脏”位置。",
    science: "心宿二是红超巨星。该星座地平线较低，雾霾和地平线云影响很大。",
    season: "夏季", hemisphere: "南半球 / 低纬度北半球", bestMonths: "6 月 – 8 月", keyStars: "心宿二、尾宿八、尾宿五"
  },
  Cygnus: {
    name: "天鹅座",
    story: "天鹅座形成北十字，沿银河分布，暗空下尤其壮观。",
    science: "天津四是夏季大三角的顶点之一，另外两颗是织女星和牛郎星。",
    season: "夏季", hemisphere: "北半球", bestMonths: "7 月 – 9 月", keyStars: "天津四、天津一、辇道增七"
  },
  Leo: {
    name: "狮子座",
    story: "狮子座以镰刀形头部和亮星轩辕十四为标志。",
    science: "狮子座是春季观测好目标，有助于介绍黄道——太阳、月亮和行星看似经过的路径。",
    season: "春季", hemisphere: "以北半球为佳", bestMonths: "3 月 – 5 月", keyStars: "轩辕十四、五帝座一、轩辕十二"
  },
  Taurus: {
    name: "金牛座",
    story: "金牛座包含 V 形的毕宿星团，附近还有昴宿星团。",
    science: "毕宿五看似在毕宿星团前方，实际上并非该星团成员。",
    season: "冬季", hemisphere: "南北半球均可", bestMonths: "11 月 – 2 月", keyStars: "毕宿五、五车五、毕宿星团"
  },
  Gemini: {
    name: "双子座",
    story: "双子座有两条近乎平行的星链，象征双胞胎北河二与北河三。",
    science: "北河二实际上是聚星系统，适合讲解双星与聚星概念。",
    season: "冬季", hemisphere: "北半球", bestMonths: "12 月 – 3 月", keyStars: "北河二、北河三、井宿三"
  },
  Lyra: {
    name: "天琴座",
    story: "天琴座虽小，但拥有织女星——天空中最亮的恒星之一。",
    science: "织女星是夏季大三角成员，曾用作测量恒星亮度的标准星。",
    season: "夏季", hemisphere: "北半球", bestMonths: "6 月 – 9 月", keyStars: "织女星、渐台二、渐台一"
  },
  Aquila: {
    name: "天鹰座",
    story: "天鹰座包含牛郎星，是夏季大三角的又一顶点。",
    science: "牛郎星自转极快，因此呈扁球形而非完美球体。",
    season: "夏季", hemisphere: "南北半球均可", bestMonths: "7 月 – 9 月", keyStars: "牛郎星、河鼓一、河鼓二"
  },
  Pegasus: {
    name: "飞马座",
    story: "飞马座以飞马大四边形著称，是秋季标志性星群。",
    science: "大四边形可作为天空标尺，帮助学生理解角距离概念。",
    season: "秋季", hemisphere: "北半球", bestMonths: "9 月 – 11 月", keyStars: "室宿一、壁宿二、室宿二、壁宿一"
  },
  Crux: {
    name: "南十字座",
    story: "南十字座虽小，却是南天最具代表性的星座之一。",
    science: "南半球观测者常用南十字座及附近指极星估算南天极方向。",
    season: "秋季 / 南半球", hemisphere: "南半球", bestMonths: "4 月 – 6 月", keyStars: "十字架二、十字架三、十字架一"
  }
};

function localizedConstellation(name) {
  const base = constellations.find(item => item.name === name);
  if (!base) return null;
  const zh = CONSTELLATION_I18N[name];
  if (state.lang === "zh" && zh) {
    return { ...base, ...zh };
  }
  return base;
}

const LOCAL_OBSERVING_SITES = {
  "China|Shanghai|Shanghai": [
    { name: "Dishui Lake", district: "Pudong / Lingang", type: "Lake Horizon", distanceKm: 70, darkness: 76, horizon: 94, access: 88, reason: "open water horizon and fewer skyline obstructions than central Shanghai" },
    { name: "Dongtan Wetland Park", district: "Chongming", type: "Wetland Reserve", distanceKm: 63, darkness: 82, horizon: 86, access: 74, reason: "lower light spill and broad eastern sky views" },
    { name: "Sheshan Observatory Area", district: "Songjiang", type: "Observatory Hill", distanceKm: 35, darkness: 68, horizon: 78, access: 92, reason: "historic astronomy site with slightly elevated terrain" },
    { name: "Dongping National Forest Park", district: "Chongming", type: "Forest Edge", distanceKm: 75, darkness: 80, horizon: 72, access: 78, reason: "darker park surroundings away from the urban core" },
    { name: "Nanhuizui Sea-Viewing Park", district: "Pudong / Lingang", type: "Coastal Edge", distanceKm: 73, darkness: 74, horizon: 96, access: 84, reason: "clear southeast horizon over open sea" },
    { name: "Fengxian Bay Forest Park", district: "Fengxian", type: "Coastal Forest", distanceKm: 49, darkness: 70, horizon: 82, access: 84, reason: "coastal air exposure and less dense high-rise lighting" },
    { name: "Qingxi Country Park", district: "Qingpu", type: "Country Park", distanceKm: 50, darkness: 72, horizon: 70, access: 82, reason: "suburban park setting with calmer local light conditions" },
    { name: "Changxing Island Country Park", district: "Chongming", type: "Island Park", distanceKm: 39, darkness: 66, horizon: 76, access: 82, reason: "river-island spacing reduces some central-city glare" },
    { name: "Jinshan City Beach", district: "Jinshan", type: "Coastal Horizon", distanceKm: 64, darkness: 69, horizon: 92, access: 80, reason: "wide southern horizon for Moon, planets, and bright constellations" },
    { name: "Yangshan Coast Viewpoint", district: "Pudong / Offshore", type: "Deep-Water Coast", distanceKm: 92, darkness: 84, horizon: 98, access: 58, reason: "best darkness and sea horizon, but farther from the city center" }
  ],
  "China|Beijing|Beijing": [
    { name: "Miyun Reservoir North Shore", district: "Miyun", type: "Reservoir Horizon", distanceKm: 88, darkness: 86, horizon: 88, access: 72, reason: "waterfront horizon and darker northern suburbs" },
    { name: "Huairou Mountain Edge", district: "Huairou", type: "Mountain Edge", distanceKm: 72, darkness: 82, horizon: 78, access: 76, reason: "mountain-air setting with lower urban glow" },
    { name: "Yanqing Wild Duck Lake", district: "Yanqing", type: "Wetland Reserve", distanceKm: 82, darkness: 84, horizon: 86, access: 74, reason: "open wetland sky and less dense lighting" },
    { name: "Mentougou Tanzhe Area", district: "Mentougou", type: "Western Hills", distanceKm: 48, darkness: 74, horizon: 70, access: 82, reason: "nearby western escape from central light pollution" }
  ]
};

els.loginForm.addEventListener("submit", login);
els.loginModeBtn.addEventListener("click", () => setAuthMode("login"));
els.signupModeBtn.addEventListener("click", () => setAuthMode("signup"));
els.logoutBtn.addEventListener("click", logout);
els.refreshBtn.addEventListener("click", loadClimate);
els.redTeamBtn.addEventListener("click", runRedTeam);
els.sortSelect.addEventListener("change", renderNodes);
els.riskSelect.addEventListener("change", renderNodes);
els.countrySelect.addEventListener("change", handleCountryChange);
els.adminAreaSelect.addEventListener("change", handleAdminAreaChange);
els.citySelect.addEventListener("change", handleCityChange);
els.themeToggle.addEventListener("click", toggleTheme);
els.loginThemeToggle.addEventListener("click", toggleTheme);
els.langToggle?.addEventListener("click", toggleLang);
els.loginLangToggle?.addEventListener("click", toggleLang);
els.passwordInput?.addEventListener("input", updatePasswordStrength);

applyI18n();
checkSession();
initStarsWord();

async function checkSession() {
  const response = await fetch("/api/me");
  if (response.ok) {
    state.user = await response.json();
    showApp();
    await loadClimate();
  }
}

function setAuthMode(mode) {
  state.authMode = mode;
  const signup = mode === "signup";
  els.loginModeBtn.classList.toggle("is-active", !signup);
  els.signupModeBtn.classList.toggle("is-active", signup);
  els.confirmPasswordLabel.classList.toggle("hidden", !signup);
  els.passwordRule.classList.toggle("hidden", !signup);
  els.passwordStrength.classList.toggle("hidden", !signup);
  els.authTitle.textContent = signup ? t("authSignup") : t("authLogin");
  els.authSubmitBtn.textContent = signup ? t("btnSignup") : t("btnEnter");
  els.loginError.textContent = "";
  if (!signup) updatePasswordStrength();
}

async function login(event) {
  event.preventDefault();
  els.loginError.textContent = "";
  const form = new FormData(els.loginForm);
  if (state.authMode === "signup") {
    const password = String(form.get("password") || "");
    if (!isValidSignupPassword(password)) {
      els.loginError.textContent = "Password must be at least 6 characters and include both letters and numbers.";
      return;
    }
    if (password !== form.get("confirmPassword")) {
      els.loginError.textContent = "Passwords do not match.";
      return;
    }
  }
  const body = new URLSearchParams(form);
  const endpoint = state.authMode === "signup" ? "/api/signup" : "/api/login";
  const response = await fetch(endpoint, {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body
  });
  if (!response.ok) {
    const payload = await response.json().catch(() => ({}));
    els.loginError.textContent = authErrorMessage(payload.error);
    return;
  }
  state.user = await response.json();
  showApp();
  await loadClimate();
}

function isValidSignupPassword(password) {
  return password.length >= 6 && /[A-Za-z]/.test(password) && /\d/.test(password);
}

function authErrorMessage(error) {
  const messages = {
    PASSWORD_MUST_BE_6_PLUS_WITH_LETTERS_AND_NUMBERS: "Password must be at least 6 characters and include both letters and numbers.",
    USERNAME_MUST_BE_3_20_ALNUM_OR_UNDERSCORE: "Username must be 3-20 characters using letters, numbers, or underscore.",
    USERNAME_ALREADY_EXISTS: "That username already exists.",
    INVALID_CREDENTIALS: "Invalid credentials."
  };
  return messages[error] || error || "Invalid credentials.";
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
  requestAnimationFrame(initStarDome);
}

function toggleTheme() {
  setTheme(document.body.dataset.theme === "dark" ? "light" : "dark");
}

function setTheme(theme) {
  document.body.dataset.theme = theme;
  localStorage.setItem("starscope-theme", theme);
  const label = theme === "dark" ? "Light" : "Dark";
  if (els.themeToggle) {
    els.themeToggle.textContent = label;
  }
  if (els.loginThemeToggle) {
    els.loginThemeToggle.textContent = label;
  }
}

async function loadClimate() {
  setStatus(t("statusFetching"));
  els.refreshBtn.disabled = true;
  const progress = startLoadingProgress();
  try {
    const response = await fetch("/api/climate");
    if (!response.ok) throw new Error(`Backend ${response.status}`);
    const payload = await response.json();
    state.nodes = payload.nodes || [];
    state.matrix = payload.matrix || [];
    finishLoadingProgress(progress);
    restoreSavedLocation();
    populateLocationSelectors();
    renderMetrics(payload);
    renderNodes();
    renderMatrix();
    if (state.user.role === "ADMIN") await loadAudit();
    setStatus(t("statusDone"));
  } catch (error) {
    stopLoadingProgress(progress);
    setStatus(`${t("loadingError")}: ${error.message}`);
  } finally {
    els.refreshBtn.disabled = false;
  }
}

function startLoadingProgress() {
  els.loadingOverlay.classList.remove("hidden");
  let step = 0;
  let percent = 0;
  const stepKeys = ["loadingGeo", "loadingWeather", "loadingScore", "loadingMatrix"];
  els.loadingText.textContent = t(stepKeys[0]);
  const timer = setInterval(() => {
    percent = Math.min(88, percent + Math.random() * 9 + 2);
    if (percent > 20 + step * 18 && step < stepKeys.length - 1) {
      step += 1;
      els.loadingText.textContent = t(stepKeys[step]);
    }
    updateLoadingBar(percent);
  }, 380);
  return { timer, setComplete() {
    clearInterval(timer);
    els.loadingText.textContent = t("loadingDone");
    updateLoadingBar(100);
    setTimeout(() => els.loadingOverlay.classList.add("hidden"), 450);
  }, stop() {
    clearInterval(timer);
    els.loadingOverlay.classList.add("hidden");
  }};
}

function finishLoadingProgress(progress) {
  progress.setComplete();
}

function stopLoadingProgress(progress) {
  progress.stop();
}

function updateLoadingBar(percent) {
  const value = Math.round(percent);
  els.loadingBar.style.width = `${value}%`;
  els.loadingPercent.textContent = `${value}%`;
}

function saveLocation() {
  if (!state.selectedCountry) return;
  localStorage.setItem(LOCATION_KEY, JSON.stringify({
    country: state.selectedCountry,
    adminArea: state.selectedAdminArea,
    city: state.selectedCity
  }));
}

function restoreSavedLocation() {
  try {
    const saved = JSON.parse(localStorage.getItem(LOCATION_KEY) || "null");
    if (!saved?.country) return;
    state.selectedCountry = saved.country;
    state.selectedAdminArea = saved.adminArea || "";
    state.selectedCity = saved.city || "";
  } catch {
    // ignore invalid cache
  }
}

function updatePasswordStrength() {
  if (state.authMode !== "signup" || !els.passwordInput) return;
  const password = els.passwordInput.value || "";
  if (!password) {
    els.passwordStrength.classList.add("hidden");
    return;
  }
  els.passwordStrength.classList.remove("hidden");
  let score = 0;
  if (password.length >= 6) score += 1;
  if (password.length >= 10) score += 1;
  if (/[A-Za-z]/.test(password) && /\d/.test(password)) score += 1;
  if (/[^A-Za-z0-9]/.test(password)) score += 1;
  const levels = [
    { cls: "weak", label: t("strengthWeak"), width: "33%" },
    { cls: "medium", label: t("strengthMedium"), width: "66%" },
    { cls: "strong", label: t("strengthStrong"), width: "100%" }
  ];
  const level = levels[Math.min(levels.length - 1, Math.max(0, score - 1))];
  els.strengthFill.className = level.cls;
  els.strengthFill.style.width = level.width;
  els.strengthLabel.textContent = level.label;
}

function moonPhaseLabel() {
  const ref = Date.UTC(2000, 0, 6);
  const days = (Date.now() - ref) / 86400000;
  const phase = (days % 29.53) / 29.53;
  if (phase < 0.1 || phase > 0.9) return { icon: "🌑", label: t("moonNew") };
  if (phase < 0.45) return { icon: "🌓", label: t("moonWax") };
  if (phase < 0.55) return { icon: "🌕", label: t("moonFull") };
  return { icon: "🌗", label: t("moonWan") };
}

function bestWindowLabel(node) {
  const score = Number(node.stargazingScore) || 0;
  if (score >= 82) return t("bestWindowPrime");
  if (score >= 65) return t("bestWindowGood");
  if (score >= 45) return t("bestWindowMarginal");
  return t("bestWindowPoor");
}

function weatherChips(node) {
  const moon = moonPhaseLabel();
  return `
    <div class="weather-chips">
      <span>🌡 ${Number(node.temperature).toFixed(1)}°C</span>
      <span>💨 ${Number(node.wind).toFixed(1)} km/h</span>
      <span>🌧 ${Number(node.precipitation).toFixed(2)} mm</span>
      <span>${moon.icon} ${moon.label}</span>
      <span>🕐 ${bestWindowLabel(node)}</span>
    </div>
  `;
}

function renderMetrics(payload) {
  const prime = state.nodes.filter(node => node.viewingBand === "Prime").length;
  const best = getRecommendedNodes()[0] || state.nodes[0];
  els.nodeCount.textContent = payload.nodeCount;
  els.geoCount.textContent = payload.geocodedCount;
  els.primeCount.textContent = prime;
  els.bestTonight.textContent = best ? locationTitle(best) : t("noWindows");
}

function renderNodes() {
  const band = els.riskSelect.value;
  const sortKey = els.sortSelect.value;
  const recommended = getRecommendedNodes();
  const rows = recommended
    .filter(node => band === "All" || node.viewingBand === band)
    .slice()
    .sort((a, b) => Number(b[sortKey]) - Number(a[sortKey]))
    .slice(0, 10);

  els.bestTonight.textContent = rows[0] ? locationTitle(rows[0]) : t("selectLocation");
  renderRecommendationCards(rows.slice(0, 3));
  els.recordCount.textContent = t("localSites", { count: rows.length });
  els.nodeBody.innerHTML = "";
  if (!state.selectedCountry || !state.selectedAdminArea || !state.selectedCity) {
    els.nodeBody.innerHTML = `<tr><td colspan="8">${escapeHtml(t("emptyTable"))}</td></tr>`;
    return;
  }
  rows.forEach((node, index) => {
    const tr = document.createElement("tr");
    tr.innerHTML = `
      <td class="numeric">${String(index + 1).padStart(2, "0")}</td>
      <td class="city">${escapeHtml(node.city)}<br><span>${escapeHtml(locationMeta(node))}</span><br><em>${escapeHtml(node.advice)}</em></td>
      <td>${escapeHtml(node.nodeType)}</td>
      <td><span class="pill ${escapeHtml(node.viewingBand)}">${escapeHtml(bandLabel(node.viewingBand))}</span></td>
      <td class="numeric">${Number(node.stargazingScore).toFixed(1)}</td>
      <td class="numeric">${Number(node.temperature).toFixed(1)}°C</td>
      <td class="numeric">${Number(node.wind).toFixed(1)}</td>
      <td class="numeric">${Number(node.precipitation).toFixed(2)}</td>
    `;
    els.nodeBody.appendChild(tr);
  });
}

function populateLocationSelectors() {
  const countries = [...new Set(state.nodes.map(node => node.country).filter(Boolean))].sort();
  const previousCountry = state.selectedCountry;
  els.countrySelect.innerHTML = `<option value="">${escapeHtml(t("optCountry"))}</option>`;
  countries.forEach(country => {
    const option = document.createElement("option");
    option.value = country;
    option.textContent = country;
    els.countrySelect.appendChild(option);
  });
  if (previousCountry && countries.includes(previousCountry)) {
    els.countrySelect.value = previousCountry;
  } else {
    state.selectedCountry = "";
    state.selectedAdminArea = "";
    state.selectedCity = "";
  }
  populateAdminAreaSelect();
  populateCitySelect();
  updateLocationHint();
}

function populateAdminAreaSelect() {
  const areas = state.nodes
    .filter(node => node.country === state.selectedCountry)
    .map(node => normalizedAdminArea(node) || "Unspecified")
    .filter(Boolean)
    .sort();
  const uniqueAreas = [...new Set(areas)];
  const previousArea = state.selectedAdminArea;
  els.adminAreaSelect.innerHTML = `<option value="">${escapeHtml(t("optAdmin"))}</option>`;
  uniqueAreas.forEach(area => {
    const option = document.createElement("option");
    option.value = area;
    option.textContent = area;
    els.adminAreaSelect.appendChild(option);
  });
  if (previousArea && uniqueAreas.includes(previousArea)) {
    els.adminAreaSelect.value = previousArea;
  } else {
    state.selectedAdminArea = "";
    state.selectedCity = "";
  }
  els.adminAreaSelect.disabled = !state.selectedCountry;
}

function populateCitySelect() {
  const cities = state.nodes
    .filter(node => node.country === state.selectedCountry)
    .filter(node => (normalizedAdminArea(node) || "Unspecified") === state.selectedAdminArea)
    .map(node => node.city)
    .filter(Boolean)
    .sort();
  const uniqueCities = [...new Set(cities)];
  const previousCity = state.selectedCity;
  els.citySelect.innerHTML = `<option value="">${escapeHtml(t("optCity"))}</option>`;
  uniqueCities.forEach(city => {
    const option = document.createElement("option");
    option.value = city;
    option.textContent = city;
    els.citySelect.appendChild(option);
  });
  if (previousCity && uniqueCities.includes(previousCity)) {
    els.citySelect.value = previousCity;
  } else {
    state.selectedCity = "";
  }
  els.citySelect.disabled = !state.selectedCountry || !state.selectedAdminArea;
}

function handleCountryChange() {
  state.selectedCountry = els.countrySelect.value;
  state.selectedAdminArea = "";
  state.selectedCity = "";
  populateAdminAreaSelect();
  populateCitySelect();
  updateLocationHint();
  saveLocation();
  renderNodes();
}

function handleAdminAreaChange() {
  state.selectedAdminArea = els.adminAreaSelect.value;
  state.selectedCity = "";
  populateCitySelect();
  updateLocationHint();
  saveLocation();
  renderNodes();
}

function handleCityChange() {
  state.selectedCity = els.citySelect.value;
  updateLocationHint();
  saveLocation();
  renderNodes();
}

function updateLocationHint() {
  if (!state.selectedCountry) {
    els.locationHint.textContent = t("pickLocation");
    return;
  }
  if (!state.selectedAdminArea) {
    els.locationHint.textContent = t("pickAdmin", { country: state.selectedCountry });
    return;
  }
  if (!state.selectedCity) {
    els.locationHint.textContent = t("pickCity", { admin: state.selectedAdminArea, country: state.selectedCountry });
    return;
  }
  els.locationHint.textContent = t("pickReady", { city: state.selectedCity });
}

function getRecommendedNodes() {
  if (!state.selectedCountry || !state.selectedAdminArea || !state.selectedCity) {
    return [];
  }

  const base = state.nodes.find(node => node.country === state.selectedCountry
    && (normalizedAdminArea(node) || "Unspecified") === state.selectedAdminArea
    && node.city === state.selectedCity);
  if (!base) {
    return [];
  }

  return getLocalSites(base)
    .map(site => buildLocalSiteNode(site, base))
    .sort((a, b) => b.matchScore - a.matchScore)
    .slice(0, 10);
}

function getLocalSites(base) {
  const key = `${base.country}|${normalizedAdminArea(base) || "Unspecified"}|${base.city}`;
  return LOCAL_OBSERVING_SITES[key] || genericLocalSites(base);
}

function genericLocalSites(base) {
  const city = base.city;
  return [
    { name: `${city} Reservoir Horizon`, district: "outer water edge", type: "Water Horizon", distanceKm: 32, darkness: 72, horizon: 90, access: 78, reason: "open water gives a cleaner horizon than dense downtown blocks" },
    { name: `${city} Country Park Edge`, district: "suburban park", type: "Country Park", distanceKm: 28, darkness: 70, horizon: 76, access: 84, reason: "park surroundings reduce direct streetlight glare" },
    { name: `${city} Northern Outskirts`, district: "outer district", type: "Suburban Edge", distanceKm: 42, darkness: 76, horizon: 72, access: 70, reason: "farther from the urban light dome while still reachable" },
    { name: `${city} Coastal Viewpoint`, district: "coastal / river edge", type: "Coastal Horizon", distanceKm: 38, darkness: 68, horizon: 92, access: 78, reason: "wide horizon helps for planets and low constellations" },
    { name: `${city} Observatory Area`, district: "science campus", type: "Education Site", distanceKm: 18, darkness: 62, horizon: 74, access: 92, reason: "easy access and good for public observing demos" },
    { name: `${city} Forest Park`, district: "forest park", type: "Forest Edge", distanceKm: 34, darkness: 74, horizon: 68, access: 78, reason: "tree cover blocks local lights, best for high-altitude targets" },
    { name: `${city} Riverside Open Space`, district: "riverfront", type: "Riverfront", distanceKm: 16, darkness: 58, horizon: 88, access: 94, reason: "not the darkest, but easy to reach and has open sky" },
    { name: `${city} Western Hills Edge`, district: "higher ground", type: "Highland Edge", distanceKm: 52, darkness: 80, horizon: 70, access: 62, reason: "higher terrain can escape haze and central light spill" },
    { name: `${city} Agricultural Belt`, district: "rural edge", type: "Rural Edge", distanceKm: 58, darkness: 82, horizon: 78, access: 58, reason: "darker surroundings with fewer commercial lights" },
    { name: `${city} Campus Sports Field`, district: "campus / school", type: "Campus Field", distanceKm: 8, darkness: 50, horizon: 82, access: 96, reason: "good for quick student demos when travel time matters" }
  ];
}

function buildLocalSiteNode(site, base) {
  const weatherScore = Number(base.stargazingScore) || 0;
  const localQuality = site.darkness * 0.46 + site.horizon * 0.34 + site.access * 0.2;
  const distancePenalty = Math.min(18, site.distanceKm * 0.16);
  const score = clamp(weatherScore * 0.68 + localQuality * 0.32 - distancePenalty, 0, 100);
  const band = viewingBandFromScore(score);
  return {
    ...base,
    city: site.name,
    parentCity: base.city,
    district: site.district,
    nodeType: site.type,
    stargazingScore: score,
    matchScore: score + Math.max(0, 18 - site.distanceKm * 0.12) + site.darkness * 0.08 + site.horizon * 0.05,
    viewingBand: band,
    risk: Math.max(0, Number(base.risk) + distancePenalty * 0.4 - site.darkness * 0.08),
    advice: `${site.reason}. Uses live ${base.city} weather as the dynamic forecast baseline.`,
    localDistanceKm: site.distanceKm
  };
}

function viewingBandFromScore(score) {
  if (score >= 82) {
    return "Prime";
  }
  if (score >= 65) {
    return "Good";
  }
  if (score >= 45) {
    return "Marginal";
  }
  return "Poor";
}

function clamp(value, min, max) {
  return Math.max(min, Math.min(max, value));
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
      <strong>${escapeHtml(node.city)}</strong>
      <small>${escapeHtml(locationMeta(node))} · ${escapeHtml(node.nodeType)}</small>
      <div class="score">${Number(node.stargazingScore).toFixed(1)}</div>
      ${weatherChips(node)}
      <p>${escapeHtml(node.advice)}</p>
      <span class="pill ${escapeHtml(node.viewingBand)}">${escapeHtml(bandLabel(node.viewingBand))}</span>
    `;
    els.recommendationCards.appendChild(card);
  });
}

function normalizedAdminArea(node) {
  const area = String(node?.adminArea ?? "").trim();
  return area && area !== "Unspecified" ? area : "";
}

function locationTitle(node) {
  if (node.parentCity) {
    return `${node.city}, ${node.parentCity}`;
  }
  const area = normalizedAdminArea(node);
  return area ? `${node.city}, ${area}` : `${node.city}, ${node.country}`;
}

function locationMeta(node) {
  if (node.parentCity) {
    return [node.country, normalizedAdminArea(node), node.parentCity, node.district].filter(Boolean).join(" · ");
  }
  return [node.country, normalizedAdminArea(node), node.region].filter(Boolean).join(" · ");
}

function haversineKm(lat1, lon1, lat2, lon2) {
  const radius = 6371;
  const dLat = toRad(lat2 - lat1);
  const dLon = toRad(lon2 - lon1);
  const a = Math.sin(dLat / 2) ** 2
    + Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) * Math.sin(dLon / 2) ** 2;
  return radius * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
}

function toRad(value) {
  return value * Math.PI / 180;
}

function heatColor(score) {
  if (score >= 82) return "var(--green)";
  if (score >= 65) return "var(--accent)";
  if (score >= 45) return "var(--warning)";
  return "var(--danger)";
}

function renderMatrixLegend() {
  if (!els.matrixLegend) return;
  els.matrixLegend.innerHTML = `
    <span><i style="background:var(--danger)"></i>${t("legendPoor")}</span>
    <span><i style="background:var(--warning)"></i>${t("legendMid")}</span>
    <span><i style="background:var(--accent)"></i>${t("legendGood")}</span>
    <span><i style="background:var(--green)"></i>${t("legendPrime")}</span>
  `;
}

function renderMatrix() {
  const maxRisk = Math.max(...state.matrix.map(row => Number(row.maxRisk)), 1);
  els.matrixList.innerHTML = "";
  renderMatrixLegend();
  state.matrix.forEach(row => {
    const avgSky = Math.max(0, 100 - Number(row.avgRisk));
    const riskPct = Number(row.maxRisk / maxRisk * 100);
    const cell = document.createElement("article");
    cell.className = "heatmap-cell";
    cell.style.setProperty("--heat", heatColor(avgSky));
    cell.innerHTML = `
      <strong>${escapeHtml(row.region)}</strong>
      <div class="heat-swatch" style="background:${heatColor(avgSky)}">${avgSky.toFixed(0)}</div>
      <span>${row.count} ${t("nodes")}</span>
      <small>${t("matrixSky")} ${avgSky.toFixed(1)} · ${t("matrixWind")} ${Number(row.avgWind).toFixed(1)} · ${t("matrixRisk")} ${riskPct.toFixed(0)}%</small>
    `;
    els.matrixList.appendChild(cell);
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
    row.innerHTML = `<strong>${escapeHtml(test.input)}</strong><br><span class="Poor">${escapeHtml(test.status)}</span>`;
    els.defenseList.appendChild(row);
  });
  if (state.user.role === "ADMIN") await loadAudit();
  setStatus(t("statusRedTeam"));
}

function initStarDome() {
  if (state.dome || !els.starDome.offsetWidth) return;

  const scene = new THREE.Scene();
  const camera = new THREE.PerspectiveCamera(48, els.starDome.clientWidth / els.starDome.clientHeight, 0.1, 1000);
  camera.position.set(0, 8, 150);

  const renderer = new THREE.WebGLRenderer({ canvas: els.starDome, antialias: true, alpha: true });
  renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));
  renderer.setSize(els.starDome.clientWidth, els.starDome.clientHeight, false);

  const root = new THREE.Group();
  scene.add(root);

  const dome = new THREE.Mesh(
    new THREE.SphereGeometry(104, 48, 24, 0, Math.PI * 2, 0, Math.PI / 2),
    new THREE.MeshBasicMaterial({ color: 0x132235, transparent: true, opacity: 0.32, wireframe: true })
  );
  dome.rotation.x = Math.PI;
  root.add(dome);

  const starGeometry = new THREE.BufferGeometry();
  const positions = [];
  for (let i = 0; i < 420; i++) {
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
      line.userData.constellation = constellation.name;
      objects.lines.push(line);
      root.add(line);
    });

    constellation.points.forEach(point => {
      const marker = new THREE.Mesh(
        new THREE.SphereGeometry(Math.max(1.25, Math.min(2.7, constellation.size * 0.38)), 16, 16),
        markerMaterial.clone()
      );
      marker.position.set(...point);
      marker.userData.constellation = constellation.name;
      rayTargets.push(marker);
      objects.markers.push(marker);
      root.add(marker);
    });
    constellationObjects.set(constellation.name, objects);
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
  state.dome = { scene, camera, renderer, constellationObjects };
  selectConstellation("Orion");
}

function constellationButtons() {
  els.constellationButtons.innerHTML = "";
  constellations.forEach(constellation => {
    const copy = localizedConstellation(constellation.name);
    const button = document.createElement("button");
    button.type = "button";
    button.dataset.constellationId = constellation.name;
    button.textContent = copy.name;
    button.addEventListener("click", () => selectConstellation(constellation.name));
    els.constellationButtons.appendChild(button);
  });
}

function selectConstellation(name) {
  const constellation = localizedConstellation(name);
  if (!constellation) return;
  state.selectedConstellation = name;
  highlightConstellation(name);
  [...els.constellationButtons.querySelectorAll("button")].forEach(button => {
    button.classList.toggle("is-active", button.dataset.constellationId === name);
  });
  els.constellationInfo.innerHTML = `
    <strong>${escapeHtml(constellation.name)}</strong>
    <span>${escapeHtml(constellation.story)}</span>
    <div class="constellation-meta">
      <div><b>${escapeHtml(t("cMetaSeason"))}</b><em>${escapeHtml(constellation.season)}</em></div>
      <div><b>${escapeHtml(t("cMetaHemisphere"))}</b><em>${escapeHtml(constellation.hemisphere)}</em></div>
      <div><b>${escapeHtml(t("cMetaBestMonths"))}</b><em>${escapeHtml(constellation.bestMonths)}</em></div>
      <div><b>${escapeHtml(t("cMetaKeyStars"))}</b><em>${escapeHtml(constellation.keyStars)}</em></div>
      <div><b>${escapeHtml(t("cMetaRelativeSize"))}</b><em>${constellation.size.toFixed(1)}${t("cSizeScale")}</em></div>
    </div>
    <p>${escapeHtml(constellation.science)}</p>
  `;
}

function highlightConstellation(name) {
  if (!state.dome) return;
  for (const [constellationName, group] of state.dome.constellationObjects.entries()) {
    const active = constellationName === name || constellationName === state.selectedConstellation;
    const color = active ? group.color : 0xffffff;
    group.lines.forEach(line => {
      line.material.color.setHex(color);
      line.material.opacity = active ? 1 : 0.5;
    });
    group.markers.forEach(marker => {
      marker.material.color.setHex(color);
      marker.material.opacity = active ? 1 : 0.78;
      marker.scale.setScalar(constellationName === name ? 1.48 : constellationName === state.selectedConstellation ? 1.22 : 1);
    });
  }
}

function initStarsWord() {
  const columns = 68;
  const rows = 14;
  els.starsGrid.innerHTML = "";
  els.starsGrid.style.setProperty("--cols", columns);
  els.starsGrid.style.setProperty("--rows", rows);

  const mask = buildStarsMask(columns, rows);
  mask.forEach(({ col, row }) => {
    const tile = document.createElement("span");
    tile.className = "stars-tile";
    tile.dataset.col = String(col);
    tile.dataset.row = String(row);
    tile.dataset.groupCol = String(Math.floor(col / 2));
    tile.dataset.groupRow = String(Math.floor(row / 2));
    tile.dataset.seed = String(((Math.floor(col / 2) * 17 + Math.floor(row / 2) * 31) % 19) - 9);
    tile.style.gridColumn = `${col + 1}`;
    tile.style.gridRow = `${row + 1}`;
    els.starsGrid.appendChild(tile);
  });

  let pointerDown = false;
  let lastPointer = null;

  els.starsWord.addEventListener("pointerdown", event => {
    pointerDown = true;
    lastPointer = { x: event.clientX, y: event.clientY };
    els.starsWord.classList.add("is-dragging");
    els.starsWord.setPointerCapture?.(event.pointerId);
    distortStars(event, true);
  });

  els.starsWord.addEventListener("pointerup", event => {
    pointerDown = false;
    els.starsWord.classList.remove("is-dragging");
    els.starsWord.releasePointerCapture?.(event.pointerId);
  });

  els.starsWord.addEventListener("pointermove", event => {
    distortStars(event, pointerDown);
  });

  els.starsWord.addEventListener("mouseleave", () => {
    lastPointer = null;
    pointerDown = false;
    els.starsWord.classList.remove("is-dragging");
    resetStarsTiles();
  });

  function distortStars(event, dragging) {
    const rect = els.starsGrid.getBoundingClientRect();
    const pointerCol = ((event.clientX - rect.left) / rect.width) * (columns - 1);
    const pointerRow = ((event.clientY - rect.top) / rect.height) * (rows - 1);
    const velocityX = lastPointer ? Math.max(-70, Math.min(70, event.clientX - lastPointer.x)) : 0;
    const velocityY = lastPointer ? Math.max(-40, Math.min(40, event.clientY - lastPointer.y)) : 0;
    const dragPower = dragging ? 1.8 : 1.0;
    lastPointer = { x: event.clientX, y: event.clientY };

    els.starsGrid.querySelectorAll(".stars-tile").forEach(tile => {
      const col = Number(tile.dataset.groupCol) * 2 + 0.5;
      const row = Number(tile.dataset.groupRow) * 2 + 0.5;
      const seed = Number(tile.dataset.seed);
      const dx = col - pointerCol;
      const dy = (row - pointerRow) * 1.8;
      const distance = Math.sqrt(dx * dx + dy * dy);
      const energy = Math.max(0, 1 - distance / 12);
      const side = dx < 0 ? -1 : 1;
      const rowSlice = row % 2 === 0 ? 1 : -1;
      const shiftX = energy * dragPower * (side * 82 + velocityX * 1.65 + rowSlice * seed * 4.6);
      const shiftY = energy * dragPower * (velocityY * 0.32 - Math.sign(dy || 1) * 10);
      tile.style.setProperty("--shift-x", `${shiftX.toFixed(1)}px`);
      tile.style.setProperty("--shift-y", `${shiftY.toFixed(1)}px`);
      tile.style.setProperty("--energy", energy.toFixed(3));
    });
  }

  function resetStarsTiles() {
    els.starsGrid.querySelectorAll(".stars-tile").forEach(tile => {
      tile.style.setProperty("--shift-x", "0px");
      tile.style.setProperty("--shift-y", "0px");
      tile.style.setProperty("--energy", "0");
    });
  }
}

function buildStarsMask(columns, rows) {
  const canvas = document.createElement("canvas");
  const width = 1800;
  const height = 520;
  canvas.width = width;
  canvas.height = height;

  const context = canvas.getContext("2d", { willReadFrequently: true });
  context.clearRect(0, 0, width, height);
  context.fillStyle = "#000";
  context.textAlign = "center";
  context.textBaseline = "middle";

  let fontSize = 430;
  do {
    context.font = `950 ${fontSize}px Arial Black, Impact, Inter, sans-serif`;
    fontSize -= 8;
  } while (context.measureText("STARS").width > width * 0.98 && fontSize > 240);
  context.fillText("STARS", width / 2, height * 0.59);

  const image = context.getImageData(0, 0, width, height).data;
  const cellWidth = width / columns;
  const cellHeight = height / rows;
  const mask = [];

  for (let row = 0; row < rows; row++) {
    for (let col = 0; col < columns; col++) {
      let alphaTotal = 0;
      const samples = 6;
      for (let y = 0; y < samples; y++) {
        for (let x = 0; x < samples; x++) {
          const px = Math.min(width - 1, Math.floor((col + (x + 0.5) / samples) * cellWidth));
          const py = Math.min(height - 1, Math.floor((row + (y + 0.5) / samples) * cellHeight));
          alphaTotal += image[(py * width + px) * 4 + 3];
        }
      }
      if (alphaTotal / (samples * samples) > 34) {
        mask.push({ col, row });
      }
    }
  }

  return mask;
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
