package scraper;

import model.ShowHNPost;
import model.TechNewsArticle;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Live scraper for Hacker News front pages.
 */
public class HackerNewsScraper implements WebScraper {

    private static final String BASE_URL = "https://news.ycombinator.com/";
    private static final String PAGE_TWO_URL = "https://news.ycombinator.com/news?p=2";
    private static final int MIN_NODES = 50;
    private static final int TIMEOUT_MS = 10_000;
    private static final String USER_AGENT = "PulseWire/1.0 (Educational Project)";

    @Override
    public List<TechNewsArticle> scrape() throws ScraperException {
        try {
            List<TechNewsArticle> articles = new ArrayList<>();
            int skipped = 0;

            skipped += parsePage(fetch(BASE_URL), articles, 0);
            if (articles.size() < MIN_NODES) {
                skipped += parsePage(fetch(PAGE_TWO_URL), articles, articles.size());
            }

            if (articles.size() < MIN_NODES) {
                throw new InsufficientDataException(articles.size(), MIN_NODES);
            }

            return articles;
        } catch (InsufficientDataException e) {
            throw e;
        } catch (IOException e) {
            throw new ScraperException("Network unavailable: " + e.getMessage(), e);
        }
    }

    public ScraperResult scrapeWithMetadata() throws ScraperException {
        try {
            List<TechNewsArticle> articles = new ArrayList<>();
            int skipped = 0;

            skipped += parsePage(fetch(BASE_URL), articles, 0);
            if (articles.size() < MIN_NODES) {
                skipped += parsePage(fetch(PAGE_TWO_URL), articles, articles.size());
            }

            if (articles.size() < MIN_NODES) {
                throw new InsufficientDataException(articles.size(), MIN_NODES);
            }

            return new ScraperResult(articles, BASE_URL, skipped);
        } catch (InsufficientDataException e) {
            throw e;
        } catch (IOException e) {
            throw new ScraperException("Network unavailable: " + e.getMessage(), e);
        }
    }

    /**
     * Parse a saved HTML document (for offline testing / @ context prompts).
     */
    public List<TechNewsArticle> parseDocument(Document doc) {
        List<TechNewsArticle> articles = new ArrayList<>();
        parsePage(doc, articles, 0);
        return articles;
    }

    private Document fetch(String url) throws IOException {
        return Jsoup.connect(url)
                .timeout(TIMEOUT_MS)
                .userAgent(USER_AGENT)
                .get();
    }

    private int parsePage(Document doc, List<TechNewsArticle> out, int rankOffset) {
        int skipped = 0;
        Elements rows = doc.select("tr.athing");

        for (int i = 0; i < rows.size(); i++) {
            Element row = rows.get(i);
            try {
                TechNewsArticle article = parseRow(row, rankOffset + i + 1);
                if (article != null) {
                    out.add(article);
                } else {
                    skipped++;
                }
            } catch (Exception e) {
                skipped++;
            }
        }

        return skipped;
    }

    private TechNewsArticle parseRow(Element row, int rank) {
        String id = row.attr("id");
        Element titleAnchor = row.selectFirst("span.titleline > a");
        if (titleAnchor == null) {
            return null;
        }

        String title = titleAnchor.text();
        String url = titleAnchor.attr("href");
        if (title.isBlank()) {
            return null;
        }

        Element subtextRow = row.nextElementSibling();
        int points = 0;
        String author = "anonymous";
        int comments = 0;
        String age = "";

        if (subtextRow != null) {
            Element scoreEl = subtextRow.selectFirst("span.score");
            if (scoreEl != null) {
                points = parseLeadingInt(scoreEl.text());
            }

            Element authorEl = subtextRow.selectFirst("a.hnuser");
            if (authorEl != null) {
                author = authorEl.text();
            }

            Element ageEl = subtextRow.selectFirst("span.age");
            if (ageEl != null) {
                age = ageEl.text();
            }

            for (Element link : subtextRow.select("a")) {
                String linkText = link.text().toLowerCase();
                if (linkText.contains("comment")) {
                    comments = parseLeadingInt(link.text());
                    break;
                }
            }
        }

        if (title.startsWith("Show HN:") || title.startsWith("Show HN ")) {
            return new ShowHNPost(id, title, url, rank, points, comments, author, age);
        }

        return new TechNewsArticle(id, title, url, rank, points, comments, author, age);
    }

    private int parseLeadingInt(String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }
        StringBuilder digits = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (Character.isDigit(c)) {
                digits.append(c);
            } else if (digits.length() > 0) {
                break;
            }
        }
        if (digits.length() == 0) {
            return 0;
        }
        try {
            return Integer.parseInt(digits.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
