# Prompt Ledger — PulseWire

> 项目路径：`/Users/peanut/Downloads/csa_final_1-main`  
> 记录每条 Vibe Coding prompt。Architect 先写架构，AI 只填实现。

---

## Prompt #0 — 创建架构空壳（人工，不用 AI）

**操作：** 手动创建 `WebData.java`, `TechNewsArticle.java`, `HackerNewsScraper.java` 等空文件，只写签名 + JavaDoc。

---

## Prompt #1 — Scraper 实现

**日期：** 2026-06-10

**Prompt：**
```
Implement parseDocument(Document doc) in @HackerNewsScraper.java.
Use HTML structure from @samples/sample_hn_frontpage.html.
For each .athing row, create @TechNewsArticle.java (extends @WebData.java).
If title starts with "Show HN:", instantiate @ShowHNPost.java instead.
Skip malformed rows; never crash on missing .score or .hnuser.
Do NOT change existing method signatures.
Do NOT use hardcoded title arrays.
```

**结果：** Implemented Jsoup parsing with missing-tag defense, Show HN polymorphism, and malformed-row skipping.

---

## Prompt #2 — Hype Word Counter

**日期：** 2026-06-10

**Prompt：**
```
Implement countHypeWords(String title) in @HypeWordCounter.java.
Match keywords case-insensitively: AI, GPT, layoff, startup, open source,
security, Rust, Python, YC, breaking.
Return int count of distinct keyword hits.
```

**结果：** Implemented case-insensitive distinct keyword counting for headline hype scoring.

---

## Prompt #3 — Selection Sort

**日期：** 2026-06-10

**Prompt：**
```
Implement selectionSortByHypeScore(ArrayList<TechNewsArticle> list)
in @HypeScoreSorter.java.
Sort descending by calculateImpactScore().
Use Selection Sort only — no Collections.sort().
Handle null, empty, and single-element lists safely.
```

**结果：** Implemented custom Selection Sort for hype impact, points, and comments without `Collections.sort()`.

---

## Prompt #4 — Terminal UI

**日期：** 2026-06-10

**Prompt：**
```
Implement printArticleTable in @TerminalRenderer.java.
Use Unicode box-drawing borders.
Align columns: # (2), HEADLINE (46), POINTS (7), HYPE (6), IMPACT (9).
Do not change method signatures.
```

**结果：** Built an industrial terminal table with aligned metric columns and truncation for long headlines.

---

## Prompt #5 — Security

**日期：** 2026-06-10

**Prompt：**
```
Implement sanitize(String raw) in @InputSanitizer.java per @IDEA.md Section 6.
Add tests in @InputSanitizerTest.java for valid input, injection, overlong input.
```

**结果：** Implemented length limits, character allowlist, blocklist detection, and a red-teamable console defense path.

---

## Prompt #6 — Industrial Dashboard + Red Team Demo

**日期：** 2026-06-10

**Prompt：**
```
Upgrade @TerminalRenderer.java and @Main.java only.
Keep the Java terminal app architecture intact.
Add a dashboard-style live data summary before the article table.
Add a menu option that demonstrates red-team inputs flowing through @InputSanitizer.java.
Do not replace the scraper, model hierarchy, or custom Selection Sort algorithm.
```

**结果：** Added a dashboard summary, denser aligned article grid, category heat bars, and menu option `[7] Red Team Defense Demo`.

---

## Prompt #7 — Stable Smoke Tests

**日期：** 2026-06-10

**Prompt：**
```
Create a lightweight test harness in @test/ProjectSmokeTest.java.
Use @samples/sample_hn_frontpage.html to verify offline parsing.
Test @HypeScoreSorter.java, @CategoryMatrixBuilder.java, and @InputSanitizer.java.
Do not introduce JUnit or new dependencies.
```

**结果：** Added a no-dependency smoke test that validates scraper parsing, polymorphism, sorting, matrix counts, and security blocking.

---

## Prompt #8 — Java-Powered Web UI

**日期：** 2026-06-12

**Prompt：**
```
Add a browser-based UI without deleting the existing terminal app.
Use Java's built-in HTTP server in @WebMain.java; do not add Spring Boot or new dependencies.
Expose @HackerNewsScraper.java data through /api/articles and @InputSanitizer.java through /api/red-team.
Create @web/index.html, @web/styles.css, and @web/app.js for a minimalist editorial dashboard.
Keep the Java scraper, OOP model hierarchy, custom Selection Sort, and 2D matrix as the backend source of truth.
```

**结果：** Added a Java-backed web dashboard at `http://localhost:8080` with live article ranking, category matrix, refresh controls, and red-team defense output.

---

## Prompt #9 — ClimateShield Role-Based Backend

**日期：** 2026-06-12

**Prompt：**
```
Pitch and implement a more complex final-project theme than live news.
Create @ClimateShieldWebMain.java as a Java backend web app with login and role-based pages.
Do not store plaintext passwords; use SHA-256 password hashes and session cookies.
Use live API acquisition instead of hardcoded data: geocode city nodes dynamically, then fetch current weather for 60+ nodes.
Create OOP classes under @climate/model: abstract ClimateData plus Urban, Coastal, and Highland node subclasses.
Create custom Selection Sort and a 2D region risk matrix under @climate/algorithm.
Admin should see audit/security panels; user should see only the analyst dashboard.
```

**结果：** Added ClimateShield, a role-based climate risk web app at `http://localhost:8090` with live Open-Meteo data, hashed login, admin/user separation, custom risk sorting, 2D region matrix, and sanitizer-based red-team demo.

---

## Prompt #10 — StarScope Stargazing Pivot + 3D Dome

**日期：** 2026-06-12

**Prompt：**
```
Pivot the role-based weather site into @StarScope: Live Stargazing Opportunity Radar.
Keep the ADMIN features: API health, all observing nodes, red-team/audit log, malicious input blocking, and raw forecast node visibility.
For USER, show today's weather-driven stargazing recommendations instead of campus risk language.
Add a 3D star dome like an observation app: a large sky hemisphere with constellation positions, lines, and clickable explanations.
Use the live weather backend to calculate stargazing score and viewing advice.
Keep login, hashed passwords, sessions, role separation, dynamic data, OOP, custom sorting, and 2D matrix evidence.
```

**结果：** Rebranded the app as StarScope, added backend stargazing score/advice fields, added `StargazingWindowSorter`, converted the dashboard to viewing bands and best observing sites, and added a Three.js constellation dome with clickable constellation explanations.

---

## Prompt #11 — High-Tech UI Polish

**日期：** 2026-06-12

**Prompt：**
```
Improve @web-climate UI so it feels like a high-tech astronomy product, not a plain dashboard.
Research Trae-style large-letter treatment and dark SaaS visual patterns.
Add dark/light theme switching.
Keep constellation geometry white by default; only color and enlarge it on hover or selected state.
Add a large STARS typography band inspired by high-tech product sites.
Do not remove the admin audit/red-team features.
```

**结果：** Added theme toggles, dark/light CSS variables, glassy technical panels, a decorative STARS word band, and constellation hover/selected highlighting with default white constellation lines.

---

## Prompt #12 — Location-Weighted Stargazing Recommendations

**日期：** 2026-06-12

**Prompt：**
```
Move the STARS typography band to the bottom of @web-climate/index.html.
Before showing recommendations, require the user to choose their country and city.
Use backend latitude/longitude in @ClimateShieldWebMain.java so @web-climate/app.js can rank nearby or same-country observing sites.
Show only the top 10 matched sites, not all 60 nodes.
Make the top few matches visually larger and place them before the table.
```

**结果：** Added country/city selection, latitude/longitude API fields, location-weighted match scoring, Top 3 recommendation cards, a Top 10 matched table, and moved the STARS band to the bottom of the page.

---

## Prompt #13 — Backend Signup + Trae-Style STARS Tiles

**日期：** 2026-06-12

**Prompt：**
```
Add a signup UI and backend signup endpoint.
Store signup data in YAML, but do not leak plaintext passwords.
Use encryption-style protection: per-user salt plus PBKDF2-HMAC-SHA256 password hashes.
JS should call backend APIs to confirm login/signup instead of checking passwords locally.
Rebuild the bottom STARS section to mimic the Trae-style block-letter interaction:
black background, blue rectangular tiles, letters normally subtle, mouse hover causes nearby rectangles to slide horizontally with distance falloff.
```

**结果：** Added `/api/signup`, YAML-backed user persistence in `data/users.yml`, salted PBKDF2 password hashes, auth mode tabs, and a tile-based interactive STARS wordmark with mouse-distance motion.

---

## Prompt #14 — Educational Constellation Dome

**日期：** 2026-06-12

**Prompt：**
```
Improve @web-climate/app.js constellation dome so it has real educational value.
Add more constellations instead of only a few examples.
Make constellation display sizes less uniform by using relative scale values.
When a constellation is selected, show season, hemisphere, best months, key stars, relative size, and a science explanation.
Keep the default white constellation styling and hover/selected color highlight.
```

**结果：** Expanded the dome to 12 constellations, added relative size scaling, variable marker sizes, scrollable constellation controls, and an educational detail panel for each selected constellation.

---

## Prompt #15 — Country / Province / City Location Hierarchy

**日期：** 2026-06-12

**Prompt：**
```
Improve @ClimateDataScraper.java and @web-climate/app.js so locations are not just a tiny flat city list.
The user should choose Country first, then a province/state/admin area, then a city.
Make China and the United States much more detailed, with curated province/state-level observing nodes.
Expose adminArea from @ClimateShieldWebMain.java JSON and use it in the recommendation algorithm.
Keep recommendations limited to the best 10, but rank same-province/state locations before expanding to same-country matches.
Do not break the login, admin audit, red-team, theme, or constellation dome features.
```

**结果：** Added `adminArea` to Java climate nodes and API JSON, expanded the curated China/US observing-site seed set, changed the UI to Country -> Province/State -> City, and updated location matching to prioritize same administrative area before same-country results.

---

## Prompt #16 — Signup Password Policy

**日期：** 2026-06-12

**Prompt：**
```
Update @AuthService.java and @web-climate/app.js so signup passwords cannot be weak.
Require every new signup password to be at least 6 characters and include both letters and numbers.
Reject invalid passwords in the Java backend, not only in the browser.
Show the password rule on @web-climate/index.html during signup.
Keep PBKDF2 password hashing and YAML user storage unchanged.
```

**结果：** Added backend password-policy validation, frontend signup pre-checks, a visible signup password rule, and friendly error messages for rejected weak passwords.

---

## Prompt #17 — High-Contrast Interactive STARS Wordmark

**日期：** 2026-06-12

**Prompt：**
```
Improve the bottom @web-climate STARS wordmark because the letters are not readable enough.
Use the TRAE-style reference: huge high-contrast block letters, neon background, and rectangular chunks that slide apart when the mouse drags across the word.
Do not keep the low-resolution 5x9 pixel letters.
Make the letters clear before interaction, then add stronger drag/glitch motion around the pointer.
```

**结果：** Replaced the small manual pixel alphabet with canvas-sampled high-resolution STARS tiles, changed the section to a neon green field with black block-letter tiles, and added pointer-drag distortion with distance falloff.

---

## Prompt #18 — City-Local Stargazing Site Recommendations

**日期：** 2026-06-14

**Prompt：**
```
Fix @web-climate/app.js recommendation logic.
If the user selects Shanghai, the Top 3 should be Shanghai observing places, not other cities like Wuhan or Hefei.
Use the selected city's live weather as the dynamic baseline, then rank local observing sites inside that city.
Add curated Shanghai local sites such as Dishui Lake, Dongtan Wetland Park, Sheshan Observatory Area, Chongming/Dongping, and Nanhuizui.
Keep the dashboard, login, admin, red-team, and dynamic weather scraper intact.
```

**结果：** Changed the recommendation layer from cross-city weather nodes to city-local observing sites, added a curated Shanghai site set, kept live Shanghai weather as the dynamic forecast baseline, and updated UI labels to show local site recommendations.

---

## Prompt #19 — Spread Constellations Across The Dome

**日期：** 2026-06-15

**Prompt：**
```
Fix @web-climate/app.js constellation dome positions.
The constellations visually appear on the same side of the dome, which makes the sky map look incorrect.
Keep this as an educational interactive dome, not a real-time astronomical RA/Dec projection.
Distribute constellation centers across approximate sky azimuth and altitude so the dome looks like a full sky instead of one clustered patch.
Update UI copy so it honestly says approximate educational sky direction.
```

**结果：** Added a `skyCenter(azimuth, altitude)` helper, moved all constellation centers from one negative-z cluster into a wider dome distribution, and clarified the dome as an educational approximate sky map.

---

## 错误 Prompt 记录（反面教材）

| # | 错误 Prompt | 为什么失败 |
|---|---|---|
| — | "Write the whole project for me" | AI 设计架构，非 Architect 主导 |
| — | "Fix null pointer" | 无 @ 上下文，无架构约束 |
