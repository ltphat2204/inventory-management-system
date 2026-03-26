package ltphat.inventory.backend.iam.domain.exception;

public class DuplicateUsernameException extends RuntimeException {
    public DuplicateUsernameException(String message) {
        super(message);
    }
}
