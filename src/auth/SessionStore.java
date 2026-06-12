package auth;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SessionStore {

    private static final long SESSION_TTL_MS = 45L * 60L * 1000L;
    private final SecureRandom random = new SecureRandom();
    private final Map<String, UserSession> sessions = new ConcurrentHashMap<>();

    public String create(UserSession session) {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        sessions.put(token, session);
        return token;
    }

    public UserSession get(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        UserSession session = sessions.get(token);
        if (session == null) {
            return null;
        }
        if (System.currentTimeMillis() - session.getCreatedAt() > SESSION_TTL_MS) {
            sessions.remove(token);
            return null;
        }
        return session;
    }

    public void remove(String token) {
        if (token != null) {
            sessions.remove(token);
        }
    }
}
