package ltphat.inventory.backend.catalog.domain.repository;

import ltphat.inventory.backend.catalog.domain.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface IProductRepository {
    Product save(Product product);
    Optional<Product> findById(Long id);
    Optional<Product> findByProductCode(String productCode);
    boolean existsByProductCode(String productCode);
    Page<Product> findAll(Pageable pageable, Long categoryId, Boolean isActive, String search);
    java.util.List<Product> findAllById(java.util.List<Long> ids);
}
