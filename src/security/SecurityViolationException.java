package security;

public class SecurityViolationException extends Exception {

    private final String reasonCode;

    public SecurityViolationException(String reasonCode) {
        super(reasonCode);
        this.reasonCode = reasonCode;
    }

    public String getReasonCode() {
        return reasonCode;
    }
}
