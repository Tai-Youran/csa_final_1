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

## 错误 Prompt 记录（反面教材）

| # | 错误 Prompt | 为什么失败 |
|---|---|---|
| — | "Write the whole project for me" | AI 设计架构，非 Architect 主导 |
| — | "Fix null pointer" | 无 @ 上下文，无架构约束 |
