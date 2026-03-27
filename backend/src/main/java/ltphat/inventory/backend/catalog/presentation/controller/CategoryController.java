package ltphat.inventory.backend.catalog.presentation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ltphat.inventory.backend.catalog.application.dto.CategoryResponse;
import ltphat.inventory.backend.catalog.application.dto.CreateCategoryRequest;
import ltphat.inventory.backend.catalog.application.dto.UpdateCategoryRequest;
import ltphat.inventory.backend.catalog.application.service.CategoryService;
import ltphat.inventory.backend.shared.api.response.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<CategoryResponse>>> listCategories(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) String sort) {
        
        Page<CategoryResponse> categories = categoryService.listCategories(page, limit, sort);
        return ResponseEntity.ok(ApiResponse.success("Categories retrieved", categories));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(@Valid @RequestBody CreateCategoryRequest request) {
        CategoryResponse category = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Category created successfully", category));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategory(@PathVariable Long id) {
        CategoryResponse category = categoryService.getCategory(id);
        return ResponseEntity.ok(ApiResponse.success("Category retrieved", category));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCategoryRequest request) {
        CategoryResponse category = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(ApiResponse.success("Category updated successfully", category));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponse.success("Category deleted successfully", null));
    }
}
