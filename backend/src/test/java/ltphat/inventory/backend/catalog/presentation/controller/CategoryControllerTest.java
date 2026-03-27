package ltphat.inventory.backend.catalog.presentation.controller;

import ltphat.inventory.backend.catalog.application.dto.CategoryResponse;
import ltphat.inventory.backend.catalog.application.dto.CreateCategoryRequest;
import ltphat.inventory.backend.catalog.application.dto.UpdateCategoryRequest;
import ltphat.inventory.backend.catalog.application.service.CategoryService;
import ltphat.inventory.backend.shared.api.response.ApiResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private CategoryController categoryController;

    @Test
    void listCategories_shouldReturn200() {
        CategoryResponse response = CategoryResponse.builder().id(1L).nameVn("Ao").build();
        Page<CategoryResponse> page = new PageImpl<>(List.of(response));
        when(categoryService.listCategories(1, 20, "nameVn")).thenReturn(page);

        ResponseEntity<ApiResponse<Page<CategoryResponse>>> result = categoryController.listCategories(1, 20, "nameVn");

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().isSuccess()).isTrue();
        assertThat(result.getBody().getData().getContent()).hasSize(1);
    }

    @Test
    void createCategory_shouldReturn201() {
        CreateCategoryRequest request = new CreateCategoryRequest();
        request.setNameVn("Ao");

        CategoryResponse response = CategoryResponse.builder().id(2L).nameVn("Ao").build();
        when(categoryService.createCategory(request)).thenReturn(response);

        ResponseEntity<ApiResponse<CategoryResponse>> result = categoryController.createCategory(request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getData().getId()).isEqualTo(2L);
    }

    @Test
    void getCategory_shouldReturn200() {
        CategoryResponse response = CategoryResponse.builder().id(3L).nameVn("Quan").build();
        when(categoryService.getCategory(3L)).thenReturn(response);

        ResponseEntity<ApiResponse<CategoryResponse>> result = categoryController.getCategory(3L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getData().getNameVn()).isEqualTo("Quan");
    }

    @Test
    void updateCategory_shouldReturn200() {
        UpdateCategoryRequest request = new UpdateCategoryRequest();
        request.setNameVn("Giay");

        CategoryResponse response = CategoryResponse.builder().id(4L).nameVn("Giay").build();
        when(categoryService.updateCategory(4L, request)).thenReturn(response);

        ResponseEntity<ApiResponse<CategoryResponse>> result = categoryController.updateCategory(4L, request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getData().getId()).isEqualTo(4L);
    }

    @Test
    void deleteCategory_shouldReturn200() {
        doNothing().when(categoryService).deleteCategory(5L);

        ResponseEntity<ApiResponse<Void>> result = categoryController.deleteCategory(5L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().isSuccess()).isTrue();
    }
}