# PulseWire

PulseWire is a Java industrial terminal application that live-scrapes 50+ Hacker News posts, converts raw HTML into object-oriented Java models, ranks the results with custom algorithms, and displays the data in a polished terminal UI.

This project follows Track B: Silicon Valley Sentiment Tracker, Option 1: Industrial Terminal UI.

## What It Does

- Scrapes live Hacker News data with Jsoup.
- Fetches at least 50+ live data nodes by combining page 1 and page 2.
- Converts scraped rows into Java objects using inheritance and polymorphism.
- Calculates a custom hype / impact score from points, comments, and keyword signals.
- Sorts results with hand-written Selection Sort.
- Builds a 2D category heat matrix.
- Blocks suspicious user input through `InputSanitizer`.
- Shows everything inside a terminal-based industrial data dashboard.

## Is This a Terminal UI?

Yes. This is a pure Java terminal application.

It does not open a browser window and it does not use HTML/CSS as a frontend. The "UI" is the styled console output printed by Java: dashboard panels, aligned tables, box borders, menus, category matrix, and red-team defense output.

That matches the assignment's Option 1: a Java-based application with an Industrial Terminal UI.

## How To Open The UI

Open Terminal, go into the project folder, compile the Java files, then run `Main`.

```bash
cd /Users/peanut/Downloads/csa_final_1-main
rm -rf out
mkdir -p out
javac -cp lib/jsoup-1.17.2.jar -d out $(find src -name '*.java')
java -cp out:lib/jsoup-1.17.2.jar Main
```

After it starts, the app will fetch live Hacker News data and print the PulseWire terminal dashboard.

## Menu Options

```text
[1] Rank by Hype Impact
[2] Rank by HN Points
[3] Rank by Comment Velocity
[4] Filter by Category
[5] Category Heat Matrix
[6] Refresh Live Data
[7] Red Team Defense Demo
[0] Shutdown
```

For the video demo, useful choices are:

1. Start the app and show `NODES: 060` or another 50+ node count.
2. Press `5` to show the category heat matrix.
3. Press `7` to show the red-team defense demo.
4. Press `4`, then type `AI`, to show a normal safe filter.
5. Press `0` to exit.

## Run The Smoke Test

This project includes a no-dependency smoke test that checks offline parsing, polymorphism, sorting, matrix logic, and security blocking.

```bash
cd /Users/peanut/Downloads/csa_final_1-main
javac -cp lib/jsoup-1.17.2.jar -d out $(find src -name '*.java')
javac -cp out:lib/jsoup-1.17.2.jar -d out test/ProjectSmokeTest.java
java -cp out:lib/jsoup-1.17.2.jar ProjectSmokeTest
```

Expected output:

```text
ProjectSmokeTest passed.
```

## Architecture

```text
src/
├── Main.java
├── model/
│   ├── WebData.java
│   ├── TechNewsArticle.java
│   └── ShowHNPost.java
├── scraper/
│   ├── WebScraper.java
│   ├── HackerNewsScraper.java
│   ├── ScraperResult.java
│   ├── ScraperException.java
│   └── InsufficientDataException.java
├── algorithm/
│   ├── HypeWordCounter.java
│   ├── HypeScoreSorter.java
│   └── CategoryMatrixBuilder.java
├── security/
│   ├── InputSanitizer.java
│   └── SecurityViolationException.java
└── ui/
    └── TerminalRenderer.java
```

## Final Project Evidence

- Dynamic Data Acquisition: `HackerNewsScraper` uses Jsoup to fetch live data.
- Object-Oriented Architecture: `WebData` is abstract; `TechNewsArticle` and `ShowHNPost` override behavior.
- Algorithmic Processing: `HypeScoreSorter` uses custom Selection Sort; `CategoryMatrixBuilder` uses a 2D array.
- Aesthetic Output: `TerminalRenderer` prints the industrial terminal dashboard.
- AI Defense: `InputSanitizer` blocks prompt injection, script tags, SQL-like payloads, and overlong input.
- Vibe Coding Mastery: `PROMPT_LEDGER.md` documents architecture-first prompts.
