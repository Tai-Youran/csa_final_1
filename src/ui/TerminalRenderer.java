package ui;

import algorithm.CategoryMatrixBuilder;
import model.TechNewsArticle;
import scraper.ScraperResult;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class TerminalRenderer {

    private static final int WIDTH = 78;
    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z")
                    .withZone(ZoneId.of("UTC"));

    private TerminalRenderer() {
    }

    public static void printHeader(ScraperResult result) {
        String time = TIME_FORMAT.format(Instant.ofEpochMilli(result.getScrapedAt()));
        printLine('╔', '═', '╗');
        printlnCenter("PULSEWIRE v1.0    LIVE TECH SENTIMENT TERMINAL");
        printlnRaw(String.format("║  Scraped: %-28s  Nodes: %-3d  Source: Hacker News",
                time, result.getNodeCount()));
        if (result.getSkippedCount() > 0) {
            printlnRaw(String.format("║  Skipped malformed rows: %-52d", result.getSkippedCount()));
        }
        printLine('╚', '═', '╝');
        System.out.println();
    }

    public static void printArticleTable(List<TechNewsArticle> articles) {
        if (articles == null || articles.isEmpty()) {
            System.out.println("No articles to display.");
            return;
        }

        printLine('╔', '═', '╗');
        printlnRaw("║ #  │ HEADLINE                                     │ POINTS│ HYPE │ IMPACT  ║");
        printLine('╠', '═', '╣');

        int limit = Math.min(articles.size(), 25);
        for (int i = 0; i < limit; i++) {
            TechNewsArticle article = articles.get(i);
            String row = String.format("║ %02d │ %-44s │ %5d │ %4d │ %7.1f ║",
                    i + 1,
                    truncate(article.getTitle(), 44),
                    article.getPoints(),
                    article.getHypeKeywordCount(),
                    article.calculateImpactScore());
            printlnRaw(row);
        }

        if (articles.size() > limit) {
            printlnRaw(String.format("║ ... %d more articles (use filter to narrow results)                       ║",
                    articles.size() - limit));
        }

        printLine('╚', '═', '╝');
        System.out.println();
    }

    public static void printCategoryMatrix(CategoryMatrixBuilder.CategoryMatrixResult result) {
        double[][] matrix = result.getMatrix();
        String[] labels = result.getLabels();

        printLine('╔', '═', '╗');
        printlnCenter("CATEGORY HEAT MATRIX");
        printlnRaw("║ CATEGORY   │ COUNT │ AVG PTS │ AVG HYPE │ MAX IMPACT                        ║");
        printLine('╠', '═', '╣');

        for (int i = 0; i < labels.length; i++) {
            printlnRaw(String.format("║ %-10s │ %5.0f │ %7.1f │ %8.1f │ %8.1f                       ║",
                    labels[i], matrix[i][0], matrix[i][1], matrix[i][2], matrix[i][3]));
        }

        printLine('╚', '═', '╝');
        System.out.println();
    }

    public static void printSecurityBlock(String reason) {
        System.out.println();
        printLine('╔', '═', '╗');
        printlnRaw("║  ⛔ SECURITY BLOCK: " + padRight(reason, WIDTH - 22) + "║");
        printlnRaw("║  System integrity preserved. Input was not executed.                      ║");
        printLine('╚', '═', '╝');
        System.out.println();
    }

    public static void printErrorBanner(String message) {
        System.out.println();
        printLine('╔', '═', '╗');
        printlnRaw("║  ERROR: " + padRight(message, WIDTH - 12) + "║");
        printLine('╚', '═', '╝');
        System.out.println();
    }

    public static void printMenu() {
        System.out.println("┌──────────────────────────────────────────┐");
        System.out.println("│ [1] Sort by Hype Score (default)         │");
        System.out.println("│ [2] Sort by Points                       │");
        System.out.println("│ [3] Sort by Comments                     │");
        System.out.println("│ [4] Filter by Category                   │");
        System.out.println("│ [5] Show Category Heat Matrix            │");
        System.out.println("│ [6] Refresh live data                    │");
        System.out.println("│ [0] Exit                                 │");
        System.out.println("└──────────────────────────────────────────┘");
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

    private static void printlnCenter(String text) {
        int padding = Math.max(0, (WIDTH - 4 - text.length()) / 2);
        String line = "║ " + " ".repeat(padding) + text;
        line = padRight(line, WIDTH - 1) + "║";
        System.out.println(line);
    }

    private static void printlnRaw(String line) {
        if (line.length() >= WIDTH) {
            System.out.println(line.substring(0, WIDTH));
        } else {
            System.out.println(padRight(line, WIDTH - 1) + "║");
        }
    }

    private static String truncate(String text, int max) {
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
