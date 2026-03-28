package ltphat.inventory.backend.catalog.application.service;

import ltphat.inventory.backend.catalog.application.dto.VariantDto;
import ltphat.inventory.backend.catalog.application.dto.VariantResponse;

import java.util.List;

public interface IProductVariantService {
    List<VariantResponse> getVariantsByProductId(Long productId, boolean includeStock);
    VariantResponse addVariantToProduct(Long productId, VariantDto request);
    VariantResponse getVariant(Long productId, Long variantId);
    VariantResponse updateVariant(Long productId, Long variantId, VariantDto request);
    void deleteVariant(Long productId, Long variantId);
    VariantResponse getVariantByBarcode(String barcode);
}
