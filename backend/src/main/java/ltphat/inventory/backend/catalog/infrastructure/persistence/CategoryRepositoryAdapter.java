package ltphat.inventory.backend.catalog.infrastructure.persistence;

import lombok.RequiredArgsConstructor;
import ltphat.inventory.backend.catalog.domain.model.Category;
import ltphat.inventory.backend.catalog.domain.repository.ICategoryRepository;
import ltphat.inventory.backend.catalog.infrastructure.persistence.entity.JpaCategory;
import ltphat.inventory.backend.catalog.infrastructure.persistence.mapper.CategoryMapper;
import ltphat.inventory.backend.catalog.infrastructure.persistence.repository.SpringDataCategoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CategoryRepositoryAdapter implements ICategoryRepository {

    private final SpringDataCategoryRepository springDataCategoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public Category save(Category category) {
        JpaCategory entity = categoryMapper.toEntity(category);
        JpaCategory savedEntity = springDataCategoryRepository.save(entity);
        return categoryMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Category> findById(Long id) {
        return springDataCategoryRepository.findById(id).map(categoryMapper::toDomain);
    }

    @Override
    public Page<Category> findAll(Pageable pageable) {
        return springDataCategoryRepository.findAll(pageable).map(categoryMapper::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        springDataCategoryRepository.deleteById(id);
    }

    @Override
    public boolean hasProducts(Long categoryId) {
        // Temporary stub. Since Product entity doesn't exist yet, we return false.
        // TODO: Implement actual check when Product entity is created.
        return false;
    }
}
