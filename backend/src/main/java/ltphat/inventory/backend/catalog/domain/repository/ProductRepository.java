package ltphat.inventory.backend.catalog.domain.repository;

import ltphat.inventory.backend.catalog.domain.model.Product;
import java.util.Optional;

public interface ProductRepository {
    Product save(Product product);
    Optional<Product> findById(Long id);
    Optional<Product> findByProductCode(String productCode);
    boolean existsByProductCode(String productCode);
    // Pagination and dynamic filtering methods will be handled at the Application layer
    // or through specific queries/specifications based on Spring Data later.
}
