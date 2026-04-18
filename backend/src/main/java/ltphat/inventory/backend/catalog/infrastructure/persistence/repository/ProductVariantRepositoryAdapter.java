package ltphat.inventory.backend.catalog.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import ltphat.inventory.backend.catalog.domain.model.ProductVariant;
import ltphat.inventory.backend.catalog.domain.repository.IProductVariantRepository;
import ltphat.inventory.backend.catalog.infrastructure.persistence.entity.JpaProduct;
import ltphat.inventory.backend.catalog.infrastructure.persistence.entity.JpaProductVariant;
import ltphat.inventory.backend.catalog.infrastructure.persistence.mapper.ProductMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ProductVariantRepositoryAdapter implements IProductVariantRepository {

    private final SpringDataProductVariantRepository springDataRepository;
    private final SpringDataProductRepository productRepository;
    private final ProductMapper mapper;

    @Override
    public ProductVariant save(ProductVariant variant) {
        JpaProductVariant entity = mapper.toEntityVariant(variant);
        JpaProduct product = productRepository.findById(variant.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        entity.setProduct(product);
        return mapper.toDomainVariant(springDataRepository.save(entity));
    }

    @Override
    public List<ProductVariant> saveAll(List<ProductVariant> variants) {
        List<JpaProductVariant> entities = variants.stream().map(v -> {
            JpaProductVariant entity = mapper.toEntityVariant(v);
            JpaProduct product = productRepository.findById(v.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found"));
            entity.setProduct(product);
            return entity;
        }).collect(Collectors.toList());
        return springDataRepository.saveAll(entities).stream()
                .map(mapper::toDomainVariant)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ProductVariant> findById(Long id) {
        return springDataRepository.findById(id).map(mapper::toDomainVariant);
    }

    @Override
    public Optional<ProductVariant> findBySku(String sku) {
        return springDataRepository.findBySku(sku).map(mapper::toDomainVariant);
    }

    @Override
    public Optional<ProductVariant> findByBarcode(String barcode) {
        return springDataRepository.findByBarcode(barcode).map(mapper::toDomainVariant);
    }

    @Override
    public boolean existsBySku(String sku) {
        return springDataRepository.existsBySku(sku);
    }

    @Override
    public List<ProductVariant> findByProductId(Long productId) {
        return springDataRepository.findByProductId(productId).stream()
                .map(mapper::toDomainVariant)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        springDataRepository.deleteById(id);
    }

    @Override
    public List<ProductVariant> findAllById(List<Long> ids) {
        return springDataRepository.findAllById(ids).stream()
                .map(mapper::toDomainVariant)
                .collect(Collectors.toList());
    }
}
