package ui;

import algorithm.CategoryMatrixBuilder;
import model.TechNewsArticle;
import scraper.ScraperResult;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class TerminalRenderer {

    private static final int WIDTH = 112;
    private static final int HEADLINE_WIDTH = 45;
    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z")
                    .withZone(ZoneId.of("UTC"));

    private TerminalRenderer() {
    }

    public static void printHeader(ScraperResult result) {
        String time = TIME_FORMAT.format(Instant.ofEpochMilli(result.getScrapedAt()));
        printLine('╔', '═', '╗');
        boxedCenter("PULSEWIRE v1.0  |  LIVE TECH SENTIMENT TERMINAL");
        boxed(String.format("STATUS: LIVE SCRAPE    SOURCE: %-24s    SCRAPED: %-24s    NODES: %03d",
                "Hacker News", time, result.getNodeCount()));
        if (result.getSkippedCount() > 0) {
            boxed(String.format("DEFENSE: skipped malformed rows = %d", result.getSkippedCount()));
        }
        printLine('╚', '═', '╝');
        System.out.println();
    }

    public static void printDashboard(ScraperResult result, List<TechNewsArticle> articles, String viewName) {
        if (articles == null || articles.isEmpty()) {
            return;
        }

        int aiCount = 0;
        int showCount = 0;
        int totalPoints = 0;
        double totalImpact = 0.0;
        TechNewsArticle top = articles.get(0);

        for (TechNewsArticle article : articles) {
            if ("AI".equalsIgnoreCase(article.getCategory())) {
                aiCount++;
            }
            if ("Show HN".equalsIgnoreCase(article.getCategory())) {
                showCount++;
            }
            totalPoints += article.getPoints();
            totalImpact += article.calculateImpactScore();
            if (article.calculateImpactScore() > top.calculateImpactScore()) {
                top = article;
            }
        }

        double averagePoints = totalPoints / (double) articles.size();
        double averageImpact = totalImpact / articles.size();

        printLine('╔', '═', '╗');
        boxed(String.format("VIEW: %-24s  RECORDS: %-3d  LIVE NODES: %-3d  TOP IMPACT: %7.1f",
                viewName, articles.size(), result == null ? articles.size() : result.getNodeCount(),
                top.calculateImpactScore()));
        printLine('╠', '═', '╣');
        boxed(String.format("AVG POINTS: %7.1f    AVG IMPACT: %7.1f    AI SIGNALS: %-3d    SHOW HN LAUNCHES: %-3d",
                averagePoints, averageImpact, aiCount, showCount));
        boxed("LEAD SIGNAL: " + truncate(top.getTitle(), 94));
        printLine('╚', '═', '╝');
        System.out.println();
    }

    public static void printArticleTable(List<TechNewsArticle> articles) {
        if (articles == null || articles.isEmpty()) {
            System.out.println("No articles to display.");
            return;
        }

        printLine('╔', '═', '╗');
        boxed(" # | CATEGORY |  IMPACT | POINTS | CMNTS | HYPE | AGE      | HEADLINE");
        printLine('╠', '═', '╣');

        int limit = Math.min(articles.size(), 25);
        for (int i = 0; i < limit; i++) {
            TechNewsArticle article = articles.get(i);
            boxed(String.format("%02d | %-8s | %7.1f | %6d | %5d | %4d | %-8s | %-" + HEADLINE_WIDTH + "s",
                    i + 1,
                    truncate(article.getCategory(), 8),
                    article.calculateImpactScore(),
                    article.getPoints(),
                    article.getComments(),
                    article.getHypeKeywordCount(),
                    compactAge(article.getAge()),
                    truncate(article.getTitle(), HEADLINE_WIDTH)));
        }

        if (articles.size() > limit) {
            printLine('╠', '═', '╣');
            boxed(String.format("... %d more records loaded in memory. Use filters or alternate sorting to inspect the tail.",
                    articles.size() - limit));
        }

        printLine('╚', '═', '╝');
        System.out.println();
    }

    public static void printCategoryMatrix(CategoryMatrixBuilder.CategoryMatrixResult result) {
        double[][] matrix = result.getMatrix();
        String[] labels = result.getLabels();

        printLine('╔', '═', '╗');
        boxedCenter("CATEGORY HEAT MATRIX");
        boxed("CATEGORY   | COUNT | AVG POINTS | AVG HYPE | MAX IMPACT | SENTIMENT BAR");
        printLine('╠', '═', '╣');

        for (int i = 0; i < labels.length; i++) {
            boxed(String.format("%-10s | %5.0f | %10.1f | %8.1f | %10.1f | %s",
                    labels[i], matrix[i][0], matrix[i][1], matrix[i][2], matrix[i][3],
                    buildBar(matrix[i][3])));
        }

        printLine('╚', '═', '╝');
        System.out.println();
    }

    public static void printSecurityBlock(String reason) {
        System.out.println();
        printLine('╔', '═', '╗');
        boxed("SECURITY BLOCK: " + reason);
        boxed("System integrity preserved. Input was treated as hostile and was not executed.");
        printLine('╚', '═', '╝');
        System.out.println();
    }

    public static void printErrorBanner(String message) {
        System.out.println();
        printLine('╔', '═', '╗');
        boxed("ERROR: " + message);
        printLine('╚', '═', '╝');
        System.out.println();
    }

    public static void printRedTeamAttempt(String rawInput, String result) {
        boxed("ATTACK INPUT: " + truncate(rawInput, 92));
        boxed("RESULT: " + result);
    }

    public static void printRedTeamStart() {
        printLine('╔', '═', '╗');
        boxedCenter("RED TEAM DEFENSE DEMO");
        boxed("Every payload is sanitized before it can touch filtering or display logic.");
        printLine('╠', '═', '╣');
    }

    public static void printRedTeamEnd() {
        printLine('╚', '═', '╝');
        System.out.println();
    }

    public static void printMenu() {
        System.out.println("┌──────────────────────────────────────────────────────────────┐");
        System.out.println("│ [1] Rank by Hype Impact       [5] Category Heat Matrix       │");
        System.out.println("│ [2] Rank by HN Points         [6] Refresh Live Data          │");
        System.out.println("│ [3] Rank by Comment Velocity  [7] Red Team Defense Demo      │");
        System.out.println("│ [4] Filter by Category        [0] Shutdown                   │");
        System.out.println("└──────────────────────────────────────────────────────────────┘");
        System.out.print("Choose option: ");
    }

    private static void printLine(char left, char fill, char right) {
        StringBuilder sb = new StringBuilder();
        sb.append(left);
        for (int i = 0; i < WIDTH - 2; i++) {
            sb.append(fill);
        }
        sb.append(right);
        System.out.println(sb);
    }

    private static void boxedCenter(String text) {
        int innerWidth = WIDTH - 2;
        String safe = truncate(text, innerWidth);
        int left = Math.max(0, (innerWidth - safe.length()) / 2);
        int right = Math.max(0, innerWidth - safe.length() - left);
        System.out.println("║" + " ".repeat(left) + safe + " ".repeat(right) + "║");
    }

    private static void boxed(String text) {
        String safe = truncate(text == null ? "" : text, WIDTH - 4);
        System.out.println("║ " + padRight(safe, WIDTH - 4) + " ║");
    }

    private static String buildBar(double value) {
        int filled = Math.min(24, (int) Math.round(value / 40.0));
        if (filled <= 0) {
            return "·".repeat(24);
        }
        return "█".repeat(filled) + "·".repeat(24 - filled);
    }

    private static String compactAge(String age) {
        if (age == null || age.isBlank()) {
            return "";
        }
        String compact = age.toLowerCase()
                .replace(" minutes ago", "m")
                .replace(" minute ago", "m")
                .replace(" hours ago", "h")
                .replace(" hour ago", "h")
                .replace(" days ago", "d")
                .replace(" day ago", "d");
        return truncate(compact, 8);
    }

    private static String truncate(String text, int max) {
        if (text == null) {
            return "";
        }
        if (text.length() <= max) {
            return text;
        }
        return text.substring(0, max - 3) + "...";
    }

    private static String padRight(String text, int width) {
        if (text.length() >= width) {
            return text.substring(0, width);
        }
        return text + " ".repeat(width - text.length());
    }
}
