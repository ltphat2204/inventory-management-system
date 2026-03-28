package ltphat.inventory.backend.catalog.application.service;

import ltphat.inventory.backend.catalog.application.dto.CreateProductRequest;
import ltphat.inventory.backend.catalog.application.dto.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IProductService {
    ProductResponse createProduct(CreateProductRequest request, Long authenticatedUserId);
    Page<ProductResponse> getProducts(Pageable pageable, Long categoryId, Boolean isActive, String search);
    ProductResponse getProductById(Long id);
    ProductResponse updateProduct(Long id, CreateProductRequest request);
    void deleteProduct(Long id);
}
