package ltphat.inventory.backend.catalog.infrastructure.persistence.repository;

import ltphat.inventory.backend.catalog.domain.model.Product;
import ltphat.inventory.backend.catalog.domain.model.ProductVariant;
import ltphat.inventory.backend.catalog.infrastructure.persistence.entity.JpaProduct;
import ltphat.inventory.backend.catalog.infrastructure.persistence.entity.JpaProductVariant;
import ltphat.inventory.backend.catalog.infrastructure.persistence.mapper.ProductMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductRepositoryImplTest {

    @Mock
    private SpringDataProductRepository springDataRepository;

    @Mock
    private ProductMapper mapper;

    @InjectMocks
    private ProductRepositoryAdapter repository;

    @Test
    void save_shouldMapVariants_whenDomainHasVariants() {
        Product product = Product.builder().id(1L).productCode("P001").variants(new ArrayList<>()).build();
        ProductVariant variant = ProductVariant.builder().id(2L).sku("P001-S-RED").build();
        product.getVariants().add(variant);

        JpaProduct entity = JpaProduct.builder().id(1L).productCode("P001").variants(new ArrayList<>()).build();
        JpaProductVariant variantEntity = JpaProductVariant.builder().id(2L).sku("P001-S-RED").build();
        JpaProduct savedEntity = JpaProduct.builder().id(1L).productCode("P001").variants(new ArrayList<>()).build();
        savedEntity.getVariants().add(variantEntity);
        Product mappedDomain = Product.builder().id(1L).productCode("P001").build();
        ProductVariant mappedVariant = ProductVariant.builder().id(2L).productId(1L).sku("P001-S-RED").build();

        when(mapper.toEntity(product)).thenReturn(entity);
        when(mapper.toEntityVariant(variant)).thenReturn(variantEntity);
        when(springDataRepository.save(entity)).thenReturn(savedEntity);
        when(mapper.toDomain(savedEntity)).thenReturn(mappedDomain);
        when(mapper.toDomainVariant(variantEntity)).thenReturn(mappedVariant);

        Product result = repository.save(product);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getVariants()).hasSize(1);
        assertThat(result.getVariants().getFirst().getSku()).isEqualTo("P001-S-RED");
    }

    @Test
    void save_shouldHandleNullVariants() {
        Product product = Product.builder().id(1L).productCode("P001").variants(null).build();
        JpaProduct entity = JpaProduct.builder().id(1L).productCode("P001").build();
        entity.setVariants(null);
        Product mappedDomain = Product.builder().id(1L).productCode("P001").build();

        when(mapper.toEntity(product)).thenReturn(entity);
        when(springDataRepository.save(entity)).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(mappedDomain);

        Product result = repository.save(product);

        assertThat(result.getProductCode()).isEqualTo("P001");
        assertThat(result.getVariants()).isNull();
    }

    @Test
    void findById_shouldMapWithVariants() {
        JpaProductVariant variantEntity = JpaProductVariant.builder().id(2L).sku("P001-S-RED").build();
        JpaProduct entity = JpaProduct.builder().id(1L).productCode("P001").variants(new ArrayList<>()).build();
        entity.getVariants().add(variantEntity);
        Product domain = Product.builder().id(1L).productCode("P001").build();
        ProductVariant domainVariant = ProductVariant.builder().id(2L).sku("P001-S-RED").build();

        when(springDataRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domain);
        when(mapper.toDomainVariant(variantEntity)).thenReturn(domainVariant);

        Optional<Product> result = repository.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getVariants()).hasSize(1);
    }

    @Test
    void findByProductCode_shouldMapOptional() {
        JpaProduct entity = JpaProduct.builder().id(1L).productCode("P001").variants(List.of()).build();
        Product domain = Product.builder().id(1L).productCode("P001").build();

        when(springDataRepository.findByProductCode("P001")).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domain);

        Optional<Product> result = repository.findByProductCode("P001");

        assertThat(result).isPresent();
        assertThat(result.get().getProductCode()).isEqualTo("P001");
    }

    @Test
    void existsByProductCode_shouldDelegate() {
        when(springDataRepository.existsByProductCode("P001")).thenReturn(true);

        boolean exists = repository.existsByProductCode("P001");

        assertThat(exists).isTrue();
        verify(springDataRepository).existsByProductCode("P001");
    }
}