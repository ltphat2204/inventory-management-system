package ltphat.inventory.backend.catalog.presentation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ltphat.inventory.backend.catalog.application.dto.CreateProductRequest;
import ltphat.inventory.backend.catalog.application.dto.ProductResponse;
import ltphat.inventory.backend.catalog.application.service.ProductService;
import ltphat.inventory.backend.shared.api.response.ApiResponse;
import ltphat.inventory.backend.shared.security.CustomUserDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getProducts(
            Pageable pageable,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false, name = "q") String search) {
        Page<ProductResponse> products = productService.getProducts(pageable, categoryId, isActive, search);
        return ResponseEntity.ok(ApiResponse.success("Success", products));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @Valid @RequestBody CreateProductRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        ProductResponse response = productService.createProduct(request, userDetails.getUser().getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Success", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Success", productService.getProductById(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody CreateProductRequest request) { 
        return ResponseEntity.ok(ApiResponse.success("Success", productService.updateProduct(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success("Success", null));
    }
}
