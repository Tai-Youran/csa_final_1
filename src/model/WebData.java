package model;

/**
 * Abstract base for all scraped internet data nodes.
 */
public abstract class WebData {

    private final String id;
    private final String title;
    private final String source;
    private final long scrapedAt;

    protected WebData(String id, String title, String source) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title cannot be null or blank");
        }
        this.id = id == null ? "unknown" : id;
        this.title = title;
        this.source = source == null ? "unknown" : source;
        this.scrapedAt = System.currentTimeMillis();
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getSource() {
        return source;
    }

    public long getScrapedAt() {
        return scrapedAt;
    }

    public abstract double calculateImpactScore();

    public abstract String getCategory();

    @Override
    public String toString() {
        return id + " | " + title;
    }
}
