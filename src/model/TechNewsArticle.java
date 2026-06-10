package model;

/**
 * A standard Hacker News story with points, comments, and hype keyword density.
 */
public class TechNewsArticle extends WebData {

    private final String url;
    private final int rank;
    private final int points;
    private final int comments;
    private final String author;
    private final String age;
    private int hypeKeywordCount;

    public TechNewsArticle(String id, String title, String url, int rank,
                           int points, int comments, String author, String age) {
        super(id, title, "Hacker News");
        this.url = url == null ? "" : url;
        this.rank = Math.max(rank, 0);
        this.points = Math.max(points, 0);
        this.comments = Math.max(comments, 0);
        this.author = author == null || author.isBlank() ? "anonymous" : author;
        this.age = age == null ? "" : age;
        this.hypeKeywordCount = 0;
    }

    public String getUrl() {
        return url;
    }

    public int getRank() {
        return rank;
    }

    public int getPoints() {
        return points;
    }

    public int getComments() {
        return comments;
    }

    public String getAuthor() {
        return author;
    }

    public String getAge() {
        return age;
    }

    public int getHypeKeywordCount() {
        return hypeKeywordCount;
    }

    public void setHypeKeywordCount(int hypeKeywordCount) {
        this.hypeKeywordCount = Math.max(hypeKeywordCount, 0);
    }

    @Override
    public double calculateImpactScore() {
        return points + (comments * 0.5) + (hypeKeywordCount * 10.0);
    }

    @Override
    public String getCategory() {
        String lower = getTitle().toLowerCase();
        if (containsAny(lower, "ai", "gpt", "llm", "machine learning", "openai")) {
            return "AI";
        }
        if (containsAny(lower, "layoff", "laid off", "fired", "rif")) {
            return "Layoffs";
        }
        if (containsAny(lower, "funding", "series a", "series b", "raised", "valuation")) {
            return "Funding";
        }
        return "General";
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("▲ %3d │ %s │ %s", points, truncate(getTitle(), 40), getCategory());
    }

    private String truncate(String text, int max) {
        if (text.length() <= max) {
            return text;
        }
        return text.substring(0, max - 3) + "...";
    }
}
