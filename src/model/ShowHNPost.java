package model;

/**
 * A "Show HN" launch post with an open-source visibility bonus.
 */
public class ShowHNPost extends TechNewsArticle {

    private final boolean openSource;

    public ShowHNPost(String id, String title, String url, int rank,
                      int points, int comments, String author, String age) {
        super(id, title, url, rank, points, comments, author, age);
        String lower = title.toLowerCase();
        this.openSource = lower.contains("open source") || lower.contains("open-source");
    }

    public boolean isOpenSource() {
        return openSource;
    }

    @Override
    public double calculateImpactScore() {
        double base = super.calculateImpactScore();
        return base + 15.0 + (openSource ? 10.0 : 0.0);
    }

    @Override
    public String getCategory() {
        return "Show HN";
    }

    @Override
    public String toString() {
        return String.format("[SHOW] %s │ impact=%.1f", getTitle(), calculateImpactScore());
    }
}
