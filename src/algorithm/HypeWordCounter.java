package algorithm;

import java.util.regex.Pattern;

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
            if (containsKeyword(lower, word)) {
                count++;
            }
        }

        return count;
    }

    private static boolean containsKeyword(String text, String keyword) {
        if (keyword.length() <= 3) {
            return Pattern.compile("\\b" + Pattern.quote(keyword) + "\\b").matcher(text).find();
        }
        return text.contains(keyword);
    }
}
