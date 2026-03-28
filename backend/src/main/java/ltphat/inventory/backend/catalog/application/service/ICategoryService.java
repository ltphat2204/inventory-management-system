package ltphat.inventory.backend.catalog.application.service;

import ltphat.inventory.backend.catalog.application.dto.CategoryResponse;
import ltphat.inventory.backend.catalog.application.dto.CreateCategoryRequest;
import ltphat.inventory.backend.catalog.application.dto.UpdateCategoryRequest;
import org.springframework.data.domain.Page;

public interface ICategoryService {
    CategoryResponse createCategory(CreateCategoryRequest request);
    CategoryResponse updateCategory(Long id, UpdateCategoryRequest request);
    CategoryResponse getCategory(Long id);
    Page<CategoryResponse> listCategories(int page, int limit, String sort);
    void deleteCategory(Long id);
}
