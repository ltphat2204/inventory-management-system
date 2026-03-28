package ltphat.inventory.backend.catalog.infrastructure.persistence.repository;

import ltphat.inventory.backend.catalog.domain.model.ProductVariant;
import ltphat.inventory.backend.catalog.infrastructure.persistence.entity.JpaProduct;
import ltphat.inventory.backend.catalog.infrastructure.persistence.entity.JpaProductVariant;
import ltphat.inventory.backend.catalog.infrastructure.persistence.mapper.ProductMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductVariantRepositoryImplTest {

    @Mock
    private SpringDataProductVariantRepository springDataRepository;

    @Mock
    private SpringDataProductRepository productRepository;

    @Mock
    private ProductMapper mapper;

    @InjectMocks
    private ProductVariantRepositoryAdapter repository;

    @Test
    void save_shouldThrow_whenProductNotFound() {
        ProductVariant variant = ProductVariant.builder().productId(1L).sku("P001-S-RED").build();
        JpaProductVariant entity = JpaProductVariant.builder().sku("P001-S-RED").build();

        when(mapper.toEntityVariant(variant)).thenReturn(entity);
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> repository.save(variant))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Product not found");
    }

    @Test
    void save_shouldMapAndPersist_whenProductExists() {
        ProductVariant variant = ProductVariant.builder().productId(1L).sku("P001-S-RED").build();
        JpaProductVariant entity = JpaProductVariant.builder().sku("P001-S-RED").build();
        JpaProduct product = JpaProduct.builder().id(1L).build();
        JpaProductVariant savedEntity = JpaProductVariant.builder().id(2L).sku("P001-S-RED").product(product).build();
        ProductVariant saved = ProductVariant.builder().id(2L).productId(1L).sku("P001-S-RED").build();

        when(mapper.toEntityVariant(variant)).thenReturn(entity);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(springDataRepository.save(entity)).thenReturn(savedEntity);
        when(mapper.toDomainVariant(savedEntity)).thenReturn(saved);

        ProductVariant result = repository.save(variant);

        assertThat(result.getId()).isEqualTo(2L);
    }

    @Test
    void saveAll_shouldMapAndPersistAll() {
        ProductVariant v1 = ProductVariant.builder().productId(1L).sku("P001-S-RED").build();
        ProductVariant v2 = ProductVariant.builder().productId(1L).sku("P001-M-BLUE").build();
        JpaProduct product = JpaProduct.builder().id(1L).build();
        JpaProductVariant e1 = JpaProductVariant.builder().sku("P001-S-RED").build();
        JpaProductVariant e2 = JpaProductVariant.builder().sku("P001-M-BLUE").build();
        JpaProductVariant savedE1 = JpaProductVariant.builder().id(10L).sku("P001-S-RED").product(product).build();
        JpaProductVariant savedE2 = JpaProductVariant.builder().id(11L).sku("P001-M-BLUE").product(product).build();
        ProductVariant d1 = ProductVariant.builder().id(10L).productId(1L).sku("P001-S-RED").build();
        ProductVariant d2 = ProductVariant.builder().id(11L).productId(1L).sku("P001-M-BLUE").build();

        when(mapper.toEntityVariant(v1)).thenReturn(e1);
        when(mapper.toEntityVariant(v2)).thenReturn(e2);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(springDataRepository.saveAll(any())).thenReturn(List.of(savedE1, savedE2));
        when(mapper.toDomainVariant(savedE1)).thenReturn(d1);
        when(mapper.toDomainVariant(savedE2)).thenReturn(d2);

        List<ProductVariant> result = repository.saveAll(List.of(v1, v2));

        assertThat(result).hasSize(2);
        assertThat(result.getFirst().getId()).isEqualTo(10L);
    }

    @Test
    void saveAll_shouldThrow_whenAnyProductMissing() {
        ProductVariant v1 = ProductVariant.builder().productId(1L).sku("P001-S-RED").build();
        JpaProductVariant e1 = JpaProductVariant.builder().sku("P001-S-RED").build();

        when(mapper.toEntityVariant(v1)).thenReturn(e1);
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> repository.saveAll(List.of(v1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Product not found");
    }

    @Test
    void queryMethods_shouldMapOptionalsAndLists() {
        JpaProduct product = JpaProduct.builder().id(1L).build();
        JpaProductVariant entity = JpaProductVariant.builder().id(9L).sku("SKU-1").barcode("BC").product(product).build();
        ProductVariant domain = ProductVariant.builder().id(9L).productId(1L).sku("SKU-1").barcode("BC").build();

        when(springDataRepository.findById(9L)).thenReturn(Optional.of(entity));
        when(springDataRepository.findBySku("SKU-1")).thenReturn(Optional.of(entity));
        when(springDataRepository.findByBarcode("BC")).thenReturn(Optional.of(entity));
        when(springDataRepository.findByProductId(1L)).thenReturn(List.of(entity));
        when(mapper.toDomainVariant(entity)).thenReturn(domain);

        assertThat(repository.findById(9L)).isPresent();
        assertThat(repository.findBySku("SKU-1")).isPresent();
        assertThat(repository.findByBarcode("BC")).isPresent();
        assertThat(repository.findByProductId(1L)).hasSize(1);
    }

    @Test
    void existsBySku_andDeleteById_shouldDelegate() {
        when(springDataRepository.existsBySku("SKU-1")).thenReturn(true);

        assertThat(repository.existsBySku("SKU-1")).isTrue();

        repository.deleteById(9L);
        verify(springDataRepository).deleteById(9L);
    }
}