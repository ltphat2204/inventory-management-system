package ltphat.inventory.backend.catalog.application.service.impl;

import lombok.RequiredArgsConstructor;
import ltphat.inventory.backend.catalog.application.dto.CreateProductRequest;
import ltphat.inventory.backend.catalog.application.dto.VariantDto;
import ltphat.inventory.backend.catalog.application.dto.ProductResponse;
import ltphat.inventory.backend.catalog.application.dto.VariantResponse;
import ltphat.inventory.backend.catalog.application.service.ProductService;
import ltphat.inventory.backend.catalog.domain.exception.DuplicateProductCodeException;
import ltphat.inventory.backend.catalog.domain.exception.DuplicateVariantSkuException;
import ltphat.inventory.backend.catalog.domain.exception.ProductNotFoundException;
import ltphat.inventory.backend.catalog.domain.model.Product;
import ltphat.inventory.backend.catalog.domain.model.ProductVariant;
import ltphat.inventory.backend.catalog.domain.repository.ProductRepository;
import ltphat.inventory.backend.catalog.domain.repository.ProductVariantRepository;
import ltphat.inventory.backend.catalog.infrastructure.persistence.entity.JpaProduct;
import ltphat.inventory.backend.catalog.infrastructure.persistence.repository.SpringDataProductRepository;
import ltphat.inventory.backend.inventory.application.service.InventoryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final InventoryService inventoryService;
    private final SpringDataProductRepository springDataProductRepository; // temp for pagination until Specification is modeled domain-wide

    @Override
    @Transactional
    public ProductResponse createProduct(CreateProductRequest request, Long authenticatedUserId) {
        if (productRepository.existsByProductCode(request.getProductCode())) {
            throw new DuplicateProductCodeException("Product code already exists: " + request.getProductCode());
        }

        Product product = Product.builder()
                .productCode(request.getProductCode())
                .nameVn(request.getNameVn())
                .nameEn(request.getNameEn())
                .categoryId(request.getCategoryId())
                .basePriceVnd(request.getBasePriceVnd())
                .vatRate(request.getVatRate())
                .description(request.getDescription())
                .isActive(true)
                .createdBy(authenticatedUserId)
                .createdAt(ZonedDateTime.now())
                .updatedAt(ZonedDateTime.now())
                .build();

        List<ProductVariant> variants = buildVariants(
            request.getProductCode(),
            request.getBasePriceVnd(),
            request.getVariants(),
            null
        );
        product.setVariants(variants);

        Product savedProduct = productRepository.save(product);

        List<Long> variantIds = savedProduct.getVariants().stream()
                .map(ProductVariant::getId)
                .collect(Collectors.toList());

        inventoryService.initializeZeroStockForVariants(variantIds);

        return mapToResponse(savedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getProducts(Pageable pageable, Long categoryId, Boolean isActive, String search) {
        Specification<JpaProduct> spec = Specification.where((root, query, cb) -> cb.conjunction());
        if (categoryId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("categoryId"), categoryId));
        }
        if (isActive != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("isActive"), isActive));
        }
        if (search != null && !search.trim().isEmpty()) {
            String likePattern = "%" + search.toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("productCode")), likePattern),
                    cb.like(cb.lower(root.get("nameVn")), likePattern)
            ));
        }

        return springDataProductRepository.findAll(spec, pageable).map(entity -> {
            ProductResponse response = new ProductResponse();
            response.setId(entity.getId());
            response.setProductCode(entity.getProductCode());
            response.setNameVn(entity.getNameVn());
            response.setNameEn(entity.getNameEn());
            response.setCategoryId(entity.getCategoryId());
            response.setBasePriceVnd(entity.getBasePriceVnd());
            response.setVatRate(entity.getVatRate());
            response.setDescription(entity.getDescription());
            response.setIsActive(entity.getIsActive());
            response.setCreatedBy(entity.getCreatedBy());
            response.setCreatedAt(entity.getCreatedAt());
            response.setUpdatedAt(entity.getUpdatedAt());
            response.setVariantCount(entity.getVariants() != null ? entity.getVariants().size() : 0);
            return response;
        });
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));
        return mapToResponse(product);
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long id, CreateProductRequest request) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));

        existing.setNameVn(request.getNameVn());
        existing.setNameEn(request.getNameEn());
        existing.setCategoryId(request.getCategoryId());
        existing.setBasePriceVnd(request.getBasePriceVnd());
        existing.setVatRate(request.getVatRate());
        existing.setDescription(request.getDescription());
        existing.setUpdatedAt(ZonedDateTime.now());

        List<ProductVariant> replacementVariants = buildVariants(
            existing.getProductCode(),
            request.getBasePriceVnd(),
            request.getVariants(),
            existing.getId()
        );
        existing.setVariants(replacementVariants);

        Product updated = productRepository.save(existing);

        List<Long> variantIds = updated.getVariants().stream()
            .map(ProductVariant::getId)
            .collect(Collectors.toList());
        inventoryService.initializeZeroStockForVariants(variantIds);

        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));
        
        // Soft delete check stock=0
        for (ProductVariant variant : existing.getVariants()) {
            Integer currentQuantity = inventoryService.getCurrentQuantity(variant.getId());
            if (currentQuantity != null && currentQuantity > 0) {
                throw new IllegalArgumentException("Cannot delete product: variant " + variant.getSku() + " has remaining stock.");
            }
        }
        
        existing.setIsActive(false);
        existing.getVariants().forEach(v -> v.setIsActive(false));
        productRepository.save(existing);
    }
    
    private String generateSku(String productCode, String size, String color, String design) {
        StringBuilder sku = new StringBuilder(productCode);
        if (size != null && !size.trim().isEmpty()) sku.append("-").append(size.trim().toUpperCase());
        if (color != null && !color.trim().isEmpty()) sku.append("-").append(color.trim().toUpperCase());
        if (design != null && !design.trim().isEmpty()) sku.append("-").append(design.trim().toUpperCase());
        return sku.toString();
    }

    private List<ProductVariant> buildVariants(
            String productCode,
            Long basePriceVnd,
            List<VariantDto> variantDtos,
            Long currentProductId
    ) {
        List<ProductVariant> variants = new ArrayList<>();
        Set<String> generatedSkus = new HashSet<>();

        for (VariantDto variantDto : variantDtos) {
            String sku = generateSku(productCode, variantDto.getSize(), variantDto.getColor(), variantDto.getDesignStyle());

            if (!generatedSkus.add(sku)) {
                throw new DuplicateVariantSkuException("Duplicate variant combination in request: " + sku);
            }

            variantRepository.findBySku(sku).ifPresent(existingVariant -> {
                boolean sameProduct = currentProductId != null && currentProductId.equals(existingVariant.getProductId());
                if (!sameProduct) {
                    throw new DuplicateVariantSkuException("Generated SKU already exists: " + sku);
                }
            });

            variants.add(ProductVariant.builder()
                    .sku(sku)
                    .size(variantDto.getSize())
                    .color(variantDto.getColor())
                    .designStyle(variantDto.getDesignStyle())
                    .variantPriceVnd(variantDto.getVariantPriceVnd() != null ? variantDto.getVariantPriceVnd() : basePriceVnd)
                    .barcode(variantDto.getBarcode())
                    .lowStockThreshold(variantDto.getLowStockThreshold())
                    .isActive(true)
                    .build());
        }

        return variants;
    }
    
    private ProductResponse mapToResponse(Product product) {
        ProductResponse res = new ProductResponse();
        res.setId(product.getId());
        res.setProductCode(product.getProductCode());
        res.setNameVn(product.getNameVn());
        res.setNameEn(product.getNameEn());
        res.setCategoryId(product.getCategoryId());
        res.setBasePriceVnd(product.getBasePriceVnd());
        res.setVatRate(product.getVatRate());
        res.setDescription(product.getDescription());
        res.setIsActive(product.getIsActive());
        res.setCreatedBy(product.getCreatedBy());
        res.setCreatedAt(product.getCreatedAt());
        res.setUpdatedAt(product.getUpdatedAt());
        
        if (product.getVariants() != null) {
            List<VariantResponse> vResList = product.getVariants().stream().map(v -> {
                VariantResponse vr = new VariantResponse();
                vr.setId(v.getId());
                vr.setSku(v.getSku());
                vr.setSize(v.getSize());
                vr.setColor(v.getColor());
                vr.setDesignStyle(v.getDesignStyle());
                vr.setVariantPriceVnd(v.getVariantPriceVnd());
                vr.setBarcode(v.getBarcode());
                vr.setLowStockThreshold(v.getLowStockThreshold());
                vr.setIsActive(v.getIsActive());
                vr.setCreatedAt(v.getCreatedAt());
                vr.setUpdatedAt(v.getUpdatedAt());
                Integer qty = inventoryService.getCurrentQuantity(v.getId());
                vr.setCurrentQuantity(qty);
                int threshold = v.getLowStockThreshold() != null ? v.getLowStockThreshold() : 0;
                vr.setLowStock(qty != null && qty < threshold);
                return vr;
            }).collect(Collectors.toList());
            res.setVariants(vResList);
        }
        return res;
    }
}
