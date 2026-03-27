package ltphat.inventory.backend.catalog.application.service.impl;

import lombok.RequiredArgsConstructor;
import ltphat.inventory.backend.catalog.application.dto.VariantDto;
import ltphat.inventory.backend.catalog.application.dto.VariantResponse;
import ltphat.inventory.backend.catalog.application.service.ProductVariantService;
import ltphat.inventory.backend.catalog.domain.exception.DuplicateVariantSkuException;
import ltphat.inventory.backend.catalog.domain.exception.ProductNotFoundException;
import ltphat.inventory.backend.catalog.domain.exception.VariantNotFoundException;
import ltphat.inventory.backend.catalog.domain.model.Product;
import ltphat.inventory.backend.catalog.domain.model.ProductVariant;
import ltphat.inventory.backend.catalog.domain.repository.ProductRepository;
import ltphat.inventory.backend.catalog.domain.repository.ProductVariantRepository;
import ltphat.inventory.backend.inventory.application.service.InventoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductVariantServiceImpl implements ProductVariantService {

    private final ProductVariantRepository variantRepository;
    private final ProductRepository productRepository;
    private final InventoryService inventoryService;

    @Override
    @Transactional(readOnly = true)
    public List<VariantResponse> getVariantsByProductId(Long productId, boolean includeStock) {
        if (productRepository.findById(productId).isEmpty()) {
            throw new ProductNotFoundException("Product not found with id: " + productId);
        }

        return variantRepository.findByProductId(productId).stream()
                .map(v -> {
                    VariantResponse vr = mapToResponse(v);
                    if (includeStock) {
                        vr.setCurrentQuantity(inventoryService.getCurrentQuantity(v.getId()));
                    }
                    return vr;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public VariantResponse addVariantToProduct(Long productId, VariantDto request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + productId));

        String sku = generateSku(product.getProductCode(), request.getSize(), request.getColor(), request.getDesignStyle());

        if (variantRepository.existsBySku(sku)) {
            throw new DuplicateVariantSkuException("Generated SKU already exists: " + sku);
        }

        ProductVariant newVariant = ProductVariant.builder()
                .productId(productId)
                .sku(sku)
                .size(request.getSize())
                .color(request.getColor())
                .designStyle(request.getDesignStyle())
                .variantPriceVnd(request.getVariantPriceVnd() != null ? request.getVariantPriceVnd() : product.getBasePriceVnd())
                .barcode(request.getBarcode())
                .lowStockThreshold(request.getLowStockThreshold())
                .isActive(true)
                .createdAt(ZonedDateTime.now())
                .updatedAt(ZonedDateTime.now())
                .build();

        ProductVariant saved = variantRepository.save(newVariant);
        inventoryService.initializeZeroStock(saved.getId());

        VariantResponse response = mapToResponse(saved);
        response.setCurrentQuantity(0);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public VariantResponse getVariant(Long productId, Long variantId) {
        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new VariantNotFoundException("Variant not found with id: " + variantId));

        if (!variant.getProductId().equals(productId)) {
            throw new IllegalArgumentException("Variant does not belong to the specified product.");
        }

        VariantResponse response = mapToResponse(variant);
        response.setCurrentQuantity(inventoryService.getCurrentQuantity(variant.getId()));
        return response;
    }

    @Override
    @Transactional
    public VariantResponse updateVariant(Long productId, Long variantId, VariantDto request) {
        ProductVariant existing = variantRepository.findById(variantId)
                .orElseThrow(() -> new VariantNotFoundException("Variant not found with id: " + variantId));

        if (!existing.getProductId().equals(productId)) {
            throw new IllegalArgumentException("Variant does not belong to the specified product.");
        }

        existing.setSize(request.getSize());
        existing.setColor(request.getColor());
        existing.setDesignStyle(request.getDesignStyle());
        existing.setVariantPriceVnd(request.getVariantPriceVnd());
        existing.setBarcode(request.getBarcode());
        existing.setLowStockThreshold(request.getLowStockThreshold());
        existing.setUpdatedAt(ZonedDateTime.now());
        
        // SKU generation may change if size/color/design changed
        // In this simple iteration, we'll keep the existing logic or update SKU manually if needed.
        // Usually SKUs shouldn't change after creation.
        
        ProductVariant saved = variantRepository.save(existing);
        
        VariantResponse response = mapToResponse(saved);
        response.setCurrentQuantity(inventoryService.getCurrentQuantity(variantId));
        return response;
    }

    @Override
    @Transactional
    public void deleteVariant(Long productId, Long variantId) {
        ProductVariant existing = variantRepository.findById(variantId)
                .orElseThrow(() -> new VariantNotFoundException("Variant not found with id: " + variantId));

        if (!existing.getProductId().equals(productId)) {
            throw new IllegalArgumentException("Variant does not belong to the specified product.");
        }
        
        Integer currentStock = inventoryService.getCurrentQuantity(variantId);
        if (currentStock != null && currentStock > 0) {
            throw new IllegalArgumentException("Cannot delete variant with positive stock.");
        }

        existing.setIsActive(false);
        variantRepository.save(existing);
    }

    @Override
    @Transactional(readOnly = true)
    public VariantResponse getVariantByBarcode(String barcode) {
        ProductVariant variant = variantRepository.findByBarcode(barcode)
                .orElseThrow(() -> new VariantNotFoundException("No variant found with barcode: " + barcode));
                
        VariantResponse response = mapToResponse(variant);
        response.setCurrentQuantity(inventoryService.getCurrentQuantity(variant.getId()));
        return response;
    }

    private String generateSku(String productCode, String size, String color, String design) {
        StringBuilder sku = new StringBuilder(productCode);
        if (size != null && !size.trim().isEmpty()) sku.append("-").append(size.trim().toUpperCase());
        if (color != null && !color.trim().isEmpty()) sku.append("-").append(color.trim().toUpperCase());
        if (design != null && !design.trim().isEmpty()) sku.append("-").append(design.trim().toUpperCase());
        return sku.toString();
    }

    private VariantResponse mapToResponse(ProductVariant variant) {
        VariantResponse vr = new VariantResponse();
        vr.setId(variant.getId());
        vr.setSku(variant.getSku());
        vr.setSize(variant.getSize());
        vr.setColor(variant.getColor());
        vr.setDesignStyle(variant.getDesignStyle());
        vr.setVariantPriceVnd(variant.getVariantPriceVnd());
        vr.setBarcode(variant.getBarcode());
        vr.setLowStockThreshold(variant.getLowStockThreshold());
        vr.setIsActive(variant.getIsActive());
        vr.setCreatedAt(variant.getCreatedAt());
        vr.setUpdatedAt(variant.getUpdatedAt());
        return vr;
    }
}
