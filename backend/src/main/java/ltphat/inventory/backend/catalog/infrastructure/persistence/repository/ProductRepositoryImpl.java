package ltphat.inventory.backend.catalog.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import ltphat.inventory.backend.catalog.domain.model.Product;
import ltphat.inventory.backend.catalog.domain.repository.ProductRepository;
import ltphat.inventory.backend.catalog.infrastructure.persistence.entity.JpaProduct;
import ltphat.inventory.backend.catalog.infrastructure.persistence.entity.JpaProductVariant;
import ltphat.inventory.backend.catalog.infrastructure.persistence.mapper.ProductMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {

    private final SpringDataProductRepository springDataRepository;
    private final ProductMapper mapper;

    @Override
    public Product save(Product product) {
        JpaProduct entity = mapper.toEntity(product);
        if (product.getVariants() != null) {
            List<JpaProductVariant> variantEntities = product.getVariants().stream()
                    .map(mapper::toEntityVariant)
                    .collect(Collectors.toList());
            variantEntities.forEach(entity::addVariant);
        }
        JpaProduct saved = springDataRepository.save(entity);
        return mapToDomainWithVariants(saved);
    }

    @Override
    public Optional<Product> findById(Long id) {
        return springDataRepository.findById(id).map(this::mapToDomainWithVariants);
    }

    @Override
    public Optional<Product> findByProductCode(String productCode) {
        return springDataRepository.findByProductCode(productCode).map(this::mapToDomainWithVariants);
    }

    @Override
    public boolean existsByProductCode(String productCode) {
        return springDataRepository.existsByProductCode(productCode);
    }

    private Product mapToDomainWithVariants(JpaProduct entity) {
        Product domain = mapper.toDomain(entity);
        if (entity.getVariants() != null) {
            domain.setVariants(entity.getVariants().stream()
                    .map(mapper::toDomainVariant)
                    .collect(Collectors.toList()));
        }
        return domain;
    }
}
