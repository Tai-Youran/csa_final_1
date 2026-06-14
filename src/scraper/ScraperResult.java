package scraper;

import model.TechNewsArticle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ScraperResult {

    private final List<TechNewsArticle> articles;
    private final long scrapedAt;
    private final String sourceUrl;
    private final int skippedCount;

    public ScraperResult(List<TechNewsArticle> articles, String sourceUrl, int skippedCount) {
        this.articles = new ArrayList<>(articles);
        this.scrapedAt = System.currentTimeMillis();
        this.sourceUrl = sourceUrl;
        this.skippedCount = skippedCount;
    }

    public List<TechNewsArticle> getArticles() {
        return Collections.unmodifiableList(articles);
    }

    public int getNodeCount() {
        return articles.size();
    }

    public long getScrapedAt() {
        return scrapedAt;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public int getSkippedCount() {
        return skippedCount;
    }
}
