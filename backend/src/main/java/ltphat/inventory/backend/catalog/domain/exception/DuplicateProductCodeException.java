package ltphat.inventory.backend.catalog.domain.exception;

public class DuplicateProductCodeException extends RuntimeException {
    public DuplicateProductCodeException(String message) {
        super(message);
    }
}
