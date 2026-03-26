package ltphat.inventory.backend.iam.domain.exception;

public class TokenSecurityException extends RuntimeException {
    public TokenSecurityException(String message) {
        super(message);
    }
}
