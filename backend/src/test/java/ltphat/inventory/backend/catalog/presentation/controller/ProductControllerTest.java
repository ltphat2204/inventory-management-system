package ltphat.inventory.backend.catalog.presentation.controller;

import ltphat.inventory.backend.catalog.application.dto.CreateProductRequest;
import ltphat.inventory.backend.catalog.application.dto.ProductResponse;
import ltphat.inventory.backend.catalog.application.service.ProductService;
import ltphat.inventory.backend.iam.domain.model.Role;
import ltphat.inventory.backend.iam.domain.model.User;
import ltphat.inventory.backend.shared.api.response.ApiResponse;
import ltphat.inventory.backend.shared.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    private CustomUserDetails userDetails;

    @BeforeEach
    void setUp() {
        Role role = Role.builder().id(1L).name("ADMIN").build();
        User user = User.builder().id(99L).username("admin").role(role).isActive(true).build();
        userDetails = new CustomUserDetails(user);
    }

    @Test
    void getProducts_shouldReturn200() {
        Pageable pageable = PageRequest.of(0, 10);
        ProductResponse product = new ProductResponse();
        product.setId(1L);
        product.setProductCode("P001");
        Page<ProductResponse> page = new PageImpl<>(List.of(product));

        when(productService.getProducts(pageable, 1L, true, "shirt")).thenReturn(page);

        ResponseEntity<ApiResponse<Page<ProductResponse>>> result =
                productController.getProducts(pageable, 1L, true, "shirt");

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().isSuccess()).isTrue();
        assertThat(result.getBody().getData().getContent()).hasSize(1);
    }

    @Test
    void createProduct_shouldReturn201() {
        CreateProductRequest request = new CreateProductRequest();
        request.setProductCode("P001");
        ProductResponse response = new ProductResponse();
        response.setId(10L);

        when(productService.createProduct(request, 99L)).thenReturn(response);

        ResponseEntity<ApiResponse<ProductResponse>> result = productController.createProduct(request, userDetails);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getData().getId()).isEqualTo(10L);
    }

    @Test
    void getProductById_shouldReturn200() {
        ProductResponse response = new ProductResponse();
        response.setId(11L);
        when(productService.getProductById(11L)).thenReturn(response);

        ResponseEntity<ApiResponse<ProductResponse>> result = productController.getProductById(11L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getData().getId()).isEqualTo(11L);
    }

    @Test
    void updateProduct_shouldReturn200() {
        CreateProductRequest request = new CreateProductRequest();
        ProductResponse response = new ProductResponse();
        response.setId(12L);
        when(productService.updateProduct(12L, request)).thenReturn(response);

        ResponseEntity<ApiResponse<ProductResponse>> result = productController.updateProduct(12L, request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getData().getId()).isEqualTo(12L);
    }

    @Test
    void deleteProduct_shouldReturn200() {
        doNothing().when(productService).deleteProduct(13L);

        ResponseEntity<ApiResponse<Void>> result = productController.deleteProduct(13L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().isSuccess()).isTrue();
    }
}