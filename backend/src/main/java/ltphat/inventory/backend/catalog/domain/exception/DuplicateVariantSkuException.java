package ltphat.inventory.backend.catalog.domain.exception;

public class DuplicateVariantSkuException extends RuntimeException {
    public DuplicateVariantSkuException(String message) {
        super(message);
    }
}
