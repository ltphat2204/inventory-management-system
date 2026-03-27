package ltphat.inventory.backend.catalog.application.service.impl;

import lombok.RequiredArgsConstructor;
import ltphat.inventory.backend.catalog.application.dto.CategoryResponse;
import ltphat.inventory.backend.catalog.application.dto.CreateCategoryRequest;
import ltphat.inventory.backend.catalog.application.dto.UpdateCategoryRequest;
import ltphat.inventory.backend.catalog.application.service.CategoryService;
import ltphat.inventory.backend.catalog.domain.exception.CategoryHasProductsException;
import ltphat.inventory.backend.catalog.domain.exception.CategoryNotFoundException;
import ltphat.inventory.backend.catalog.domain.model.Category;
import ltphat.inventory.backend.catalog.domain.repository.CategoryRepository;
import ltphat.inventory.backend.catalog.infrastructure.persistence.mapper.CategoryMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        Category category = Category.builder()
                .nameVn(request.getNameVn())
                .nameEn(request.getNameEn())
                .description(request.getDescription())
                .build();
        
        Category saved = categoryRepository.save(category);
        return categoryMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(Long id, UpdateCategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
        
        category.setNameVn(request.getNameVn());
        category.setNameEn(request.getNameEn());
        category.setDescription(request.getDescription());
        
        Category updated = categoryRepository.save(category);
        return categoryMapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
        return categoryMapper.toResponse(category);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoryResponse> listCategories(int page, int limit, String sortProperty) {
        // Page in API is 1-indexed, Spring Data is 0-indexed
        int pageNumber = Math.max(0, page - 1);
        Sort sort = Sort.unsorted();
        if (sortProperty != null && !sortProperty.isEmpty()) {
            sort = Sort.by(sortProperty).ascending(); // Default ascending
        }
        Pageable pageable = PageRequest.of(pageNumber, limit, sort);
        Page<Category> categoryPage = categoryRepository.findAll(pageable);
        return categoryPage.map(categoryMapper::toResponse);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        if (!categoryRepository.findById(id).isPresent()) {
            throw new CategoryNotFoundException(id);
        }
        
        if (categoryRepository.hasProducts(id)) {
            throw new CategoryHasProductsException(id);
        }
        
        categoryRepository.deleteById(id);
    }
}
