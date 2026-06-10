package algorithm;

/**
 * Counts distinct hype keywords in a headline.
 */
public final class HypeWordCounter {

    private static final String[] HYPE_WORDS = {
            "ai", "gpt", "llm", "layoff", "startup", "open source",
            "security", "rust", "python", "yc", "breaking"
    };

    private HypeWordCounter() {
    }

    public static int countHypeWords(String title) {
        if (title == null || title.isBlank()) {
            return 0;
        }

        String lower = title.toLowerCase();
        int count = 0;

        for (String word : HYPE_WORDS) {
            if (lower.contains(word)) {
                count++;
            }
        }

        return count;
    }
}
