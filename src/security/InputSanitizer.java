package security;

import java.util.regex.Pattern;

public final class InputSanitizer {

    private static final int MAX_LEN = 40;
    private static final Pattern ALLOWED = Pattern.compile("^[A-Za-z0-9 .\\-]{1,40}$");

    private static final String[] BLOCKLIST = {
            "ignore", "instruction", "system", "prompt", "previous",
            "<script", "javascript:", "drop ", "select ", "--", "/*",
            "${", "or '1'='1", "exec(", "eval("
    };

    private InputSanitizer() {
    }

    public static String sanitize(String raw) throws SecurityViolationException {
        if (raw == null) {
            return "";
        }

        String trimmed = raw.trim();
        if (trimmed.length() > MAX_LEN) {
            throw new SecurityViolationException("INPUT_TOO_LONG");
        }

        String lower = trimmed.toLowerCase();
        for (String signature : BLOCKLIST) {
            if (lower.contains(signature)) {
                throw new SecurityViolationException("INJECTION_DETECTED");
            }
        }

        if (!ALLOWED.matcher(trimmed).matches()) {
            throw new SecurityViolationException("INVALID_CHARS");
        }

        return trimmed;
    }
}
