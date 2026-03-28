package ltphat.inventory.backend.catalog.presentation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ltphat.inventory.backend.catalog.application.dto.VariantDto;
import ltphat.inventory.backend.catalog.application.dto.VariantResponse;
import ltphat.inventory.backend.catalog.application.service.IProductVariantService;
import ltphat.inventory.backend.shared.api.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ProductVariantController {

    private final IProductVariantService variantService;

    @GetMapping("/products/{productId}/variants")
    public ResponseEntity<ApiResponse<List<VariantResponse>>> getProductVariants(
            @PathVariable Long productId,
            @RequestParam(required = false, defaultValue = "true") boolean includeStock) {
        List<VariantResponse> variants = variantService.getVariantsByProductId(productId, includeStock);
        return ResponseEntity.ok(ApiResponse.success("Success", variants));
    }

    @PostMapping("/products/{productId}/variants")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<VariantResponse>> addVariant(
            @PathVariable Long productId,
            @Valid @RequestBody VariantDto request) {
        VariantResponse response = variantService.addVariantToProduct(productId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Success", response));
    }

    @GetMapping("/products/{productId}/variants/{variantId}")
    public ResponseEntity<ApiResponse<VariantResponse>> getVariant(
            @PathVariable Long productId,
            @PathVariable Long variantId) {
        return ResponseEntity.ok(ApiResponse.success("Success", variantService.getVariant(productId, variantId)));
    }

    @PutMapping("/products/{productId}/variants/{variantId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<VariantResponse>> updateVariant(
            @PathVariable Long productId,
            @PathVariable Long variantId,
            @Valid @RequestBody VariantDto request) {
        return ResponseEntity.ok(ApiResponse.success("Success", variantService.updateVariant(productId, variantId, request)));
    }

    @DeleteMapping("/products/{productId}/variants/{variantId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteVariant(
            @PathVariable Long productId,
            @PathVariable Long variantId) {
        variantService.deleteVariant(productId, variantId);
        return ResponseEntity.ok(ApiResponse.success("Success", null));
    }

    @GetMapping("/variants/barcode/{barcode}")
    public ResponseEntity<ApiResponse<VariantResponse>> getVariantByBarcode(@PathVariable String barcode) {
        return ResponseEntity.ok(ApiResponse.success("Success", variantService.getVariantByBarcode(barcode)));
    }
}
