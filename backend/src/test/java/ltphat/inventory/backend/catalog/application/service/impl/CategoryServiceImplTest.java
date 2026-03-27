package ltphat.inventory.backend.catalog.application.service.impl;

import ltphat.inventory.backend.catalog.application.dto.CategoryResponse;
import ltphat.inventory.backend.catalog.application.dto.CreateCategoryRequest;
import ltphat.inventory.backend.catalog.application.dto.UpdateCategoryRequest;
import ltphat.inventory.backend.catalog.domain.exception.CategoryHasProductsException;
import ltphat.inventory.backend.catalog.domain.exception.CategoryNotFoundException;
import ltphat.inventory.backend.catalog.domain.model.Category;
import ltphat.inventory.backend.catalog.domain.repository.CategoryRepository;
import ltphat.inventory.backend.catalog.infrastructure.persistence.mapper.CategoryMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category category;
    private CategoryResponse response;

    @BeforeEach
    void setUp() {
        category = Category.builder()
                .id(10L)
                .nameVn("Ao")
                .nameEn("Shirt")
                .description("Clothing")
                .build();

        response = new CategoryResponse();
        response.setId(10L);
        response.setNameVn("Ao");
        response.setNameEn("Shirt");
        response.setDescription("Clothing");
    }

    @Test
    void createCategory_shouldSaveAndMapResponse() {
        CreateCategoryRequest request = new CreateCategoryRequest();
        request.setNameVn("Ao");
        request.setNameEn("Shirt");
        request.setDescription("Clothing");

        when(categoryRepository.save(any(Category.class))).thenReturn(category);
        when(categoryMapper.toResponse(category)).thenReturn(response);

        CategoryResponse result = categoryService.createCategory(request);

        assertThat(result.getId()).isEqualTo(10L);
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void updateCategory_shouldThrow_whenCategoryNotFound() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.updateCategory(99L, new UpdateCategoryRequest()))
                .isInstanceOf(CategoryNotFoundException.class)
                .hasMessage("Category not found with id: 99");
    }

    @Test
    void updateCategory_shouldSaveUpdatedFields() {
        UpdateCategoryRequest request = new UpdateCategoryRequest();
        request.setNameVn("Quan");
        request.setNameEn("Pants");
        request.setDescription("Bottom");

        when(categoryRepository.findById(10L)).thenReturn(Optional.of(category));
        when(categoryRepository.save(any(Category.class))).thenReturn(category);
        when(categoryMapper.toResponse(any(Category.class))).thenReturn(response);

        CategoryResponse result = categoryService.updateCategory(10L, request);

        assertThat(result.getId()).isEqualTo(10L);
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void getCategory_shouldReturnMappedResponse_whenFound() {
        when(categoryRepository.findById(10L)).thenReturn(Optional.of(category));
        when(categoryMapper.toResponse(category)).thenReturn(response);

        CategoryResponse result = categoryService.getCategory(10L);

        assertThat(result.getNameVn()).isEqualTo("Ao");
    }

    @Test
    void getCategory_shouldThrow_whenNotFound() {
        when(categoryRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.getCategory(404L))
                .isInstanceOf(CategoryNotFoundException.class)
                .hasMessage("Category not found with id: 404");
    }

    @Test
    void listCategories_shouldHandleSortProvided() {
        Page<Category> categoryPage = new PageImpl<>(List.of(category));
        when(categoryRepository.findAll(any(Pageable.class))).thenReturn(categoryPage);
        when(categoryMapper.toResponse(any(Category.class))).thenReturn(response);

        Page<CategoryResponse> result = categoryService.listCategories(2, 20, "nameVn");

        assertThat(result.getContent()).hasSize(1);
        verify(categoryRepository).findAll(any(Pageable.class));
    }

    @Test
    void listCategories_shouldHandleSortNullAndPageLowerBound() {
        Page<Category> categoryPage = new PageImpl<>(List.of(category));
        when(categoryRepository.findAll(any(Pageable.class))).thenReturn(categoryPage);
        when(categoryMapper.toResponse(any(Category.class))).thenReturn(response);

        Page<CategoryResponse> result = categoryService.listCategories(0, 20, null);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void listCategories_shouldHandleSortEmpty() {
        Page<Category> categoryPage = new PageImpl<>(List.of(category));
        when(categoryRepository.findAll(any(Pageable.class))).thenReturn(categoryPage);
        when(categoryMapper.toResponse(any(Category.class))).thenReturn(response);

        Page<CategoryResponse> result = categoryService.listCategories(1, 20, "");

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void deleteCategory_shouldThrow_whenCategoryNotFound() {
        when(categoryRepository.findById(33L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.deleteCategory(33L))
                .isInstanceOf(CategoryNotFoundException.class)
                .hasMessage("Category not found with id: 33");

        verify(categoryRepository, never()).deleteById(any());
    }

    @Test
    void deleteCategory_shouldThrow_whenCategoryHasProducts() {
        when(categoryRepository.findById(10L)).thenReturn(Optional.of(category));
        when(categoryRepository.hasProducts(10L)).thenReturn(true);

        assertThatThrownBy(() -> categoryService.deleteCategory(10L))
                .isInstanceOf(CategoryHasProductsException.class)
                .hasMessage("Cannot delete category with id: 10 because it has linked products.");

        verify(categoryRepository, never()).deleteById(any());
    }

    @Test
    void deleteCategory_shouldDelete_whenNoProductsLinked() {
        when(categoryRepository.findById(10L)).thenReturn(Optional.of(category));
        when(categoryRepository.hasProducts(10L)).thenReturn(false);

        categoryService.deleteCategory(10L);

        verify(categoryRepository).deleteById(10L);
    }
}