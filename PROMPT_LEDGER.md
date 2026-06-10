# Prompt Ledger — PulseWire

> 项目路径：`/Users/chenmin/IdeaProjects/coding`  
> 记录每条 Vibe Coding prompt。Architect 先写架构，AI 只填实现。

---

## Prompt #0 — 创建架构空壳（人工，不用 AI）

**操作：** 手动创建 `WebData.java`, `TechNewsArticle.java`, `HackerNewsScraper.java` 等空文件，只写签名 + JavaDoc。

---

## Prompt #1 — Scraper 实现

**日期：** _待填_

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

**结果：** _待填_

---

## Prompt #2 — Hype Word Counter

**日期：** _待填_

**Prompt：**
```
Implement countHypeWords(String title) in @HypeWordCounter.java.
Match keywords case-insensitively: AI, GPT, layoff, startup, open source,
security, Rust, Python, YC, breaking.
Return int count of distinct keyword hits.
```

**结果：** _待填_

---

## Prompt #3 — Selection Sort

**日期：** _待填_

**Prompt：**
```
Implement selectionSortByHypeScore(ArrayList<TechNewsArticle> list)
in @HypeScoreSorter.java.
Sort descending by calculateImpactScore().
Use Selection Sort only — no Collections.sort().
Handle null, empty, and single-element lists safely.
```

**结果：** _待填_

---

## Prompt #4 — Terminal UI

**日期：** _待填_

**Prompt：**
```
Implement printArticleTable in @TerminalRenderer.java.
Use Unicode box-drawing borders.
Align columns: # (2), HEADLINE (46), POINTS (7), HYPE (6), IMPACT (9).
Do not change method signatures.
```

**结果：** _待填_

---

## Prompt #5 — Security

**日期：** _待填_

**Prompt：**
```
Implement sanitize(String raw) in @InputSanitizer.java per @IDEA.md Section 6.
Add tests in @InputSanitizerTest.java for valid input, injection, overlong input.
```

**结果：** _待填_

---

## 错误 Prompt 记录（反面教材）

| # | 错误 Prompt | 为什么失败 |
|---|---|---|
| — | "Write the whole project for me" | AI 设计架构，非 Architect 主导 |
| — | "Fix null pointer" | 无 @ 上下文，无架构约束 |
