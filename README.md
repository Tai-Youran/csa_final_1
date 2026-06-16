# PulseWire

## Recommended Final Version: StarScope

StarScope is the more advanced final-project direction in this repo. It is a Java backend web app with login, roles, live dynamic data, OOP models, custom algorithms, a 2D matrix, an interactive 3D constellation dome, and red-team defense.

Theme: **Live Stargazing Opportunity Radar**

Live data flow:

1. The backend starts from a curated China/United States observing-site seed set, then dynamically calls Open-Meteo's geocoding API to expand the live city pool.
2. The backend calls Open-Meteo's forecast API to fetch current weather for 72+ live nodes.
3. Java converts those raw API records into polymorphic `ClimateData` objects.
4. A custom Selection Sort ranks nodes by stargazing score.
5. A 2D region matrix summarizes count, sky quality, average wind, obstruction risk, and precipitation.
6. A login system separates `ADMIN` and `USER` views.
7. The user dashboard requires a country -> province/state -> city selection before recommendations. China and the United States include detailed province/state-level location options.
8. The recommendation engine uses the selected city's live weather as the dynamic baseline, then ranks local observing sites inside that city (e.g., curated Shanghai local spots like Dishui Lake and Dongtan Wetland).
9. The user dashboard includes a Three.js-style 3D constellation dome with 12 educational constellations distributed across realistic approximate sky azimuth and altitude, featuring clickable observing notes, seasons, hemispheres, key stars, and relative display sizes.
10. The interface supports dark and light themes, white default constellation lines, hover/selected constellation highlighting, and a highly interactive, Trae-style high-contrast `STARS` wordmark with pointer-drag glitch motion.
11. Signup is handled by the Java backend. New accounts are written to `data/users.yml` with salted PBKDF2 hashes, not plaintext passwords.
12. Signup passwords must be at least 6 characters and include both letters and numbers. The frontend explains the rule, and the backend rejects weak passwords even if someone bypasses the UI.

### Open StarScope

```bash
cd /Users/peanut/Downloads/csa_final_1-main
rm -rf out
mkdir -p out
javac -cp lib/jsoup-1.17.2.jar -d out $(find src -name '*.java')
java -cp out:lib/jsoup-1.17.2.jar ClimateShieldWebMain
```

Then open:

```text
http://localhost:8090
```

Demo accounts:

```text
Admin:   admin   / GridAdmin2026!
User:    analyst / StudentRadar2026!
```

The backend does not store plaintext passwords. It stores salted PBKDF2-HMAC-SHA256 password hashes and creates session cookies after login.
Current signup storage uses PBKDF2-HMAC-SHA256 with a per-user salt in `data/users.yml`; the frontend only calls backend APIs and never verifies passwords locally.
Signup also enforces a minimum password rule: 6+ characters with both letters and numbers.

Admin can see the restricted audit panel, raw system activity, data source health, and red-team output. User can see the live stargazing dashboard, country/province/city recommendations, weather matrix, and constellation dome but cannot access admin audit logs.

---

## Click-to-browse URL (GitHub Pages Demo)

This repo includes a **static demo** of StarScope under `docs/` so it can be browsed from GitHub Pages without running the Java backend.

After you enable GitHub Pages in the repo settings, the URL will be:

```text
https://Tai-Youran.github.io/csa_final_1/
```

Enable it:

1. Go to GitHub repo **Settings** → **Pages**
2. **Build and deployment** → Source: **Deploy from a branch**
3. Branch: `main` (or `master`) · Folder: `/docs`
4. Save, wait 1–2 minutes, then open the URL above.

---

## Older Version: PulseWire

PulseWire is a Java live tech sentiment application that scrapes 50+ Hacker News posts, converts raw HTML into object-oriented Java models, ranks the results with custom algorithms, and displays the data through either a terminal UI or a browser-based web dashboard.

This project follows Track B: Silicon Valley Sentiment Tracker. It now supports both Option 1, an Industrial Terminal UI, and Option 2, a Java-powered web app.

## What It Does

- Scrapes live Hacker News data with Jsoup.
- Fetches at least 50+ live data nodes by combining page 1 and page 2.
- Converts scraped rows into Java objects using inheritance and polymorphism.
- Calculates a custom hype / impact score from points, comments, and keyword signals.
- Sorts results with hand-written Selection Sort.
- Builds a 2D category heat matrix.
- Blocks suspicious user input through `InputSanitizer`.
- Shows everything inside either a terminal-based industrial dashboard or a minimalist editorial web dashboard.

## UI Options

There are two ways to present the project.

### Option 1: Terminal UI

`Main.java` starts the original Java terminal app. It prints dashboard panels, aligned tables, box borders, menus, a category matrix, and red-team defense output directly inside Terminal.

### Option 2: Web UI

`WebMain.java` starts a tiny Java HTTP server. The browser UI loads live data from Java endpoints:

- `/api/articles` scrapes live Hacker News data and returns ranked JSON.
- `/api/red-team` runs attack payloads through `InputSanitizer`.

The web frontend is in the `web/` folder, but the live data and security logic still come from the Java backend.

## How To Open The Terminal UI

Open Terminal, go into the project folder, compile the Java files, then run `Main`.

```bash
cd /Users/peanut/Downloads/csa_final_1-main
rm -rf out
mkdir -p out
javac -cp lib/jsoup-1.17.2.jar -d out $(find src -name '*.java')
java -cp out:lib/jsoup-1.17.2.jar Main
```

After it starts, the app will fetch live Hacker News data and print the PulseWire terminal dashboard.

## How To Open The Web UI

Open Terminal, go into the project folder, compile the Java files, then run `WebMain`.

```bash
cd /Users/peanut/Downloads/csa_final_1-main
rm -rf out
mkdir -p out
javac -cp lib/jsoup-1.17.2.jar -d out $(find src -name '*.java')
java -cp out:lib/jsoup-1.17.2.jar WebMain
```

Then open this URL in a browser:

```text
http://localhost:8080
```

Keep the Terminal window running while using the web UI. Press `Ctrl+C` in Terminal to stop the server.

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
├── WebMain.java
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
web/
├── index.html
├── styles.css
└── app.js
```

## Final Project Evidence

- Dynamic Data Acquisition: `HackerNewsScraper` uses Jsoup to fetch live data.
- Object-Oriented Architecture: `WebData` is abstract; `TechNewsArticle` and `ShowHNPost` override behavior.
- Algorithmic Processing: `HypeScoreSorter` uses custom Selection Sort; `CategoryMatrixBuilder` uses a 2D array.
- Aesthetic Output: `TerminalRenderer` prints the industrial terminal dashboard; `web/` provides the browser dashboard.
- AI Defense: `InputSanitizer` blocks prompt injection, script tags, SQL-like payloads, and overlong input.
- Vibe Coding Mastery: `PROMPT_LEDGER.md` documents architecture-first prompts.
