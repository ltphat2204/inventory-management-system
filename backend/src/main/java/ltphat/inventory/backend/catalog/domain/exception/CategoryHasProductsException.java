package ltphat.inventory.backend.catalog.domain.exception;

public class CategoryHasProductsException extends RuntimeException {
    public CategoryHasProductsException(Long id) {
        super("Cannot delete category with id: " + id + " because it has linked products.");
    }
}
