package auth;

public class UserSession {

    private final String username;
    private final UserRole role;
    private final long createdAt;

    public UserSession(String username, UserRole role) {
        this.username = username;
        this.role = role;
        this.createdAt = System.currentTimeMillis();
    }

    public String getUsername() {
        return username;
    }

    public UserRole getRole() {
        return role;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }
}
