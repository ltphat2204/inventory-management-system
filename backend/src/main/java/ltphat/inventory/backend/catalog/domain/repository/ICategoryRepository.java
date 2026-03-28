package ltphat.inventory.backend.catalog.domain.repository;

import ltphat.inventory.backend.catalog.domain.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ICategoryRepository {
    Category save(Category category);
    Optional<Category> findById(Long id);
    Page<Category> findAll(Pageable pageable);
    void deleteById(Long id);
    
    // Stub definition for product relation
    boolean hasProducts(Long categoryId);
}
