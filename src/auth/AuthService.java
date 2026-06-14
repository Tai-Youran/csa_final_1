package auth;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class AuthService {

    private static final Path USER_FILE = Path.of("data/users.yml");
    private static final int ITERATIONS = 120_000;
    private static final int KEY_LENGTH = 256;
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[A-Za-z0-9_]{3,20}$");
    private static final int MIN_PASSWORD_LENGTH = 6;

    private final SecureRandom random = new SecureRandom();
    private final Map<String, UserRecord> users = new HashMap<>();

    public AuthService() {
        try {
            Files.createDirectories(USER_FILE.getParent());
            if (!Files.exists(USER_FILE) || Files.size(USER_FILE) == 0) {
                seedDemoUsers();
                saveUsers();
            } else {
                loadUsers();
                ensureDemoUser("admin", "GridAdmin2026!", UserRole.ADMIN);
                ensureDemoUser("analyst", "StudentRadar2026!", UserRole.USER);
                saveUsers();
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to initialize YAML user store", e);
        }
    }

    public UserSession authenticate(String username, String password) {
        if (username == null || password == null) {
            return null;
        }
        UserRecord record = users.get(normalize(username));
        if (record == null) {
            return null;
        }
        String candidate = hashPassword(password, record.salt);
        if (!constantTimeEquals(record.passwordHash, candidate)) {
            return null;
        }
        return new UserSession(record.username, record.role);
    }

    public SignupResult signup(String username, String password) {
        String normalized = normalize(username);
        if (!USERNAME_PATTERN.matcher(normalized).matches()) {
            return SignupResult.invalid("USERNAME_MUST_BE_3_20_ALNUM_OR_UNDERSCORE");
        }
        String passwordError = validateSignupPassword(password);
        if (passwordError != null) {
            return SignupResult.invalid(passwordError);
        }
        if (users.containsKey(normalized)) {
            return SignupResult.invalid("USERNAME_ALREADY_EXISTS");
        }

        String salt = newSalt();
        UserRecord record = new UserRecord(normalized, UserRole.USER, salt,
                hashPassword(password, salt), System.currentTimeMillis());
        users.put(normalized, record);
        try {
            saveUsers();
        } catch (IOException e) {
            users.remove(normalized);
            return SignupResult.invalid("USER_STORE_WRITE_FAILED");
        }
        return SignupResult.ok(new UserSession(record.username, record.role));
    }

    private String validateSignupPassword(String password) {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            return "PASSWORD_MUST_BE_6_PLUS_WITH_LETTERS_AND_NUMBERS";
        }

        boolean hasLetter = false;
        boolean hasDigit = false;
        for (int i = 0; i < password.length(); i++) {
            char c = password.charAt(i);
            if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z')) {
                hasLetter = true;
            }
            if (Character.isDigit(c)) {
                hasDigit = true;
            }
        }
        if (!hasLetter || !hasDigit) {
            return "PASSWORD_MUST_BE_6_PLUS_WITH_LETTERS_AND_NUMBERS";
        }
        return null;
    }

    private void seedDemoUsers() {
        users.clear();
        addSeedUser("admin", "GridAdmin2026!", UserRole.ADMIN);
        addSeedUser("analyst", "StudentRadar2026!", UserRole.USER);
    }

    private void ensureDemoUser(String username, String password, UserRole role) {
        if (!users.containsKey(username)) {
            addSeedUser(username, password, role);
        }
    }

    private void addSeedUser(String username, String password, UserRole role) {
        String salt = newSalt();
        users.put(username, new UserRecord(username, role, salt, hashPassword(password, salt), System.currentTimeMillis()));
    }

    private void loadUsers() throws IOException {
        users.clear();
        List<String> lines = Files.readAllLines(USER_FILE, StandardCharsets.UTF_8);
        Map<String, String> current = new HashMap<>();

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.startsWith("- username:")) {
                addParsedRecord(current);
                current.clear();
                current.put("username", trimmed.substring("- username:".length()).trim());
            } else if (trimmed.contains(":")) {
                int idx = trimmed.indexOf(':');
                current.put(trimmed.substring(0, idx).trim(), trimmed.substring(idx + 1).trim());
            }
        }
        addParsedRecord(current);
    }

    private void addParsedRecord(Map<String, String> values) {
        if (!values.containsKey("username") || !values.containsKey("passwordHash") || !values.containsKey("salt")) {
            return;
        }
        String username = normalize(values.get("username"));
        UserRole role = "ADMIN".equalsIgnoreCase(values.get("role")) ? UserRole.ADMIN : UserRole.USER;
        long createdAt = parseLong(values.get("createdAt"), System.currentTimeMillis());
        users.put(username, new UserRecord(username, role, values.get("salt"), values.get("passwordHash"), createdAt));
    }

    private void saveUsers() throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add("users:");
        for (UserRecord record : users.values()) {
            lines.add("  - username: " + record.username);
            lines.add("    role: " + record.role);
            lines.add("    salt: " + record.salt);
            lines.add("    passwordHash: " + record.passwordHash);
            lines.add("    createdAt: " + record.createdAt);
        }
        Files.write(USER_FILE, lines, StandardCharsets.UTF_8);
    }

    private String newSalt() {
        byte[] bytes = new byte[18];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashPassword(String password, String salt) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(StandardCharsets.UTF_8),
                    ITERATIONS, KEY_LENGTH);
            byte[] hash = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
            return "pbkdf2_sha256$" + ITERATIONS + "$" + Base64.getEncoder().encodeToString(hash);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Password hashing unavailable", e);
        }
    }

    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return false;
        }
        byte[] left = a.getBytes(StandardCharsets.UTF_8);
        byte[] right = b.getBytes(StandardCharsets.UTF_8);
        int diff = left.length ^ right.length;
        for (int i = 0; i < Math.min(left.length, right.length); i++) {
            diff |= left[i] ^ right[i];
        }
        return diff == 0;
    }

    private long parseLong(String value, long fallback) {
        try {
            return Long.parseLong(value);
        } catch (Exception e) {
            return fallback;
        }
    }

    private String normalize(String username) {
        return username == null ? "" : username.trim().toLowerCase();
    }

    private static class UserRecord {
        private final String username;
        private final UserRole role;
        private final String salt;
        private final String passwordHash;
        private final long createdAt;

        private UserRecord(String username, UserRole role, String salt, String passwordHash, long createdAt) {
            this.username = username;
            this.role = role;
            this.salt = salt;
            this.passwordHash = passwordHash;
            this.createdAt = createdAt;
        }
    }

    public static class SignupResult {
        private final boolean ok;
        private final String reason;
        private final UserSession session;

        private SignupResult(boolean ok, String reason, UserSession session) {
            this.ok = ok;
            this.reason = reason;
            this.session = session;
        }

        public static SignupResult ok(UserSession session) {
            return new SignupResult(true, "", session);
        }

        public static SignupResult invalid(String reason) {
            return new SignupResult(false, reason, null);
        }

        public boolean isOk() {
            return ok;
        }

        public String getReason() {
            return reason;
        }

        public UserSession getSession() {
            return session;
        }
    }
}
