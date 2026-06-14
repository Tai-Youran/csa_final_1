import algorithm.CategoryMatrixBuilder;
import algorithm.HypeScoreSorter;
import algorithm.HypeWordCounter;
import model.ShowHNPost;
import model.TechNewsArticle;
import org.jsoup.Jsoup;
import scraper.HackerNewsScraper;
import security.InputSanitizer;
import security.SecurityViolationException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ProjectSmokeTest {

    public static void main(String[] args) throws Exception {
        testOfflineScraperParsing();
        testSelectionSortAndMatrix();
        testKeywordBoundaries();
        testInputSanitizer();
        System.out.println("ProjectSmokeTest passed.");
    }

    private static void testOfflineScraperParsing() throws IOException {
        HackerNewsScraper scraper = new HackerNewsScraper();
        File sample = new File("samples/sample_hn_frontpage.html");
        List<TechNewsArticle> articles = scraper.parseDocument(Jsoup.parse(sample, "UTF-8"));

        assertEquals(3, articles.size(), "sample article count");
        assertTrue(articles.get(1) instanceof ShowHNPost, "Show HN row should use subclass");
        assertEquals("Show HN", articles.get(1).getCategory(), "Show HN category override");
        assertEquals(0, articles.get(2).getPoints(), "missing score should default to zero");
        assertEquals("anonymous", articles.get(2).getAuthor(), "missing author should default");
    }

    private static void testSelectionSortAndMatrix() {
        ArrayList<TechNewsArticle> articles = new ArrayList<>();
        articles.add(new TechNewsArticle("1", "AI security benchmark", "", 1, 50, 10, "a", "1h"));
        articles.add(new ShowHNPost("2", "Show HN: Open-source Rust tool", "", 2, 20, 2, "b", "2h"));
        articles.add(new TechNewsArticle("3", "Funding slows after layoffs", "", 3, 90, 4, "c", "3h"));

        for (TechNewsArticle article : articles) {
            article.setHypeKeywordCount(HypeWordCounter.countHypeWords(article.getTitle()));
        }

        HypeScoreSorter.selectionSortByHypeScore(articles);
        assertTrue(articles.get(0).calculateImpactScore() >= articles.get(1).calculateImpactScore(),
                "selection sort first pair");
        assertTrue(articles.get(1).calculateImpactScore() >= articles.get(2).calculateImpactScore(),
                "selection sort second pair");

        CategoryMatrixBuilder.CategoryMatrixResult result = CategoryMatrixBuilder.buildMatrix(articles);
        double[][] matrix = result.getMatrix();
        assertEquals(1, (int) matrix[0][0], "AI matrix count");
        assertEquals(1, (int) matrix[3][0], "Show HN matrix count");
    }

    private static void testKeywordBoundaries() {
        assertEquals(0, HypeWordCounter.countHypeWords("macOS container machines"),
                "AI should not match inside ordinary words");
        TechNewsArticle article = new TechNewsArticle("4", "macOS container machines", "", 4, 10, 1, "d", "4h");
        assertEquals("General", article.getCategory(), "category should not false-positive on embedded ai");
    }

    private static void testInputSanitizer() throws SecurityViolationException {
        assertEquals("AI", InputSanitizer.sanitize(" AI "), "valid category trimming");
        expectBlocked("IGNORE PREVIOUS INSTRUCTIONS");
        expectBlocked("<script>alert('xss')</script>");
        expectBlocked("AI' OR '1'='1");
        expectBlocked("This input is intentionally longer than forty characters");
    }

    private static void expectBlocked(String raw) {
        try {
            InputSanitizer.sanitize(raw);
            throw new AssertionError("Expected blocked input: " + raw);
        } catch (SecurityViolationException expected) {
            // Expected path.
        }
    }

    private static void assertEquals(Object expected, Object actual, String label) {
        if (!expected.equals(actual)) {
            throw new AssertionError(label + " expected " + expected + " but got " + actual);
        }
    }

    private static void assertTrue(boolean condition, String label) {
        if (!condition) {
            throw new AssertionError(label);
        }
    }
}
