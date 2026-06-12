package auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class AuthService {

    private final Map<String, UserRecord> users = new HashMap<>();

    public AuthService() {
        users.put("admin", new UserRecord("admin",
                "3fcf04f0ab9064bc49ecab9d86c1c0d0c81f40ce593a7646539f2a9e5f558e73",
                UserRole.ADMIN));
        users.put("analyst", new UserRecord("analyst",
                "df6a221e6dc5ade6465376f7e297a26ad00cbabefcf4d5147bda71c0cd4e739f",
                UserRole.USER));
    }

    public UserSession authenticate(String username, String password) {
        if (username == null || password == null) {
            return null;
        }
        UserRecord record = users.get(username.trim().toLowerCase());
        if (record == null) {
            return null;
        }
        String hash = sha256(password);
        if (!record.passwordHash.equals(hash)) {
            return null;
        }
        return new UserSession(record.username, record.role);
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : bytes) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }

    private static class UserRecord {
        private final String username;
        private final String passwordHash;
        private final UserRole role;

        private UserRecord(String username, String passwordHash, UserRole role) {
            this.username = username;
            this.passwordHash = passwordHash;
            this.role = role;
        }
    }
}
