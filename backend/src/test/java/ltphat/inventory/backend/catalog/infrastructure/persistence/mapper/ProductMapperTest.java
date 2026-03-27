package ltphat.inventory.backend.catalog.infrastructure.persistence.mapper;

import ltphat.inventory.backend.catalog.domain.model.Product;
import ltphat.inventory.backend.catalog.domain.model.ProductVariant;
import ltphat.inventory.backend.catalog.infrastructure.persistence.entity.JpaProduct;
import ltphat.inventory.backend.catalog.infrastructure.persistence.entity.JpaProductVariant;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ProductMapperTest {

    private final ProductMapper mapper = new ProductMapperImpl();

    @Test
    void toDomain_shouldMapProductFields() {
        ZonedDateTime now = ZonedDateTime.now();
        JpaProduct entity = JpaProduct.builder()
                .id(1L)
                .productCode("P001")
                .nameVn("Ao")
                .nameEn("Shirt")
                .categoryId(3L)
                .basePriceVnd(100000L)
                .vatRate(new BigDecimal("10.00"))
                .description("Cotton")
                .isActive(true)
                .createdBy(10L)
                .createdAt(now)
                .updatedAt(now)
                .build();

        Product domain = mapper.toDomain(entity);

        assertThat(domain.getId()).isEqualTo(1L);
        assertThat(domain.getProductCode()).isEqualTo("P001");
        assertThat(domain.getBasePriceVnd()).isEqualTo(100000L);
        assertThat(domain.getCreatedBy()).isEqualTo(10L);
    }

    @Test
    void toEntity_shouldMapProductFields() {
        ZonedDateTime now = ZonedDateTime.now();
        Product domain = Product.builder()
                .id(2L)
                .productCode("P002")
                .nameVn("Quan")
                .nameEn("Pants")
                .categoryId(4L)
                .basePriceVnd(200000L)
                .vatRate(new BigDecimal("8.00"))
                .description("Denim")
                .isActive(false)
                .createdBy(11L)
                .createdAt(now)
                .updatedAt(now)
                .build();

        JpaProduct entity = mapper.toEntity(domain);

        assertThat(entity.getId()).isEqualTo(2L);
        assertThat(entity.getProductCode()).isEqualTo("P002");
        assertThat(entity.getCategoryId()).isEqualTo(4L);
        assertThat(entity.getIsActive()).isFalse();
    }

    @Test
    void toDomainVariant_shouldMapProductIdFromNestedProduct() {
        JpaProduct product = JpaProduct.builder().id(9L).build();
        JpaProductVariant entity = JpaProductVariant.builder()
                .id(7L)
                .product(product)
                .sku("P002-M-BLUE")
                .size("M")
                .color("Blue")
                .designStyle("Basic")
                .variantPriceVnd(220000L)
                .barcode("BC-1")
                .lowStockThreshold(5)
                .isActive(true)
                .build();

        ProductVariant domain = mapper.toDomainVariant(entity);

        assertThat(domain.getId()).isEqualTo(7L);
        assertThat(domain.getProductId()).isEqualTo(9L);
        assertThat(domain.getSku()).isEqualTo("P002-M-BLUE");
    }

    @Test
    void toDomain_shouldHandleNullVariantList() {
        JpaProduct entity = JpaProduct.builder()
                .id(5L)
                .productCode("P005")
                .nameVn("Null variants")
                .basePriceVnd(50000L)
                .build();
        entity.setVariants(null);

        Product domain = mapper.toDomain(entity);

        assertThat(domain.getId()).isEqualTo(5L);
        assertThat(domain.getVariants()).isNull();
    }

    @Test
    void toDomain_shouldMapVariantListElements() {
        JpaProduct parent = JpaProduct.builder().id(11L).build();
        JpaProductVariant variant = JpaProductVariant.builder().id(12L).sku("P011-S-WHITE").product(parent).build();
        JpaProduct entity = JpaProduct.builder()
                .id(11L)
                .productCode("P011")
                .nameVn("Variant list")
                .basePriceVnd(110000L)
                .build();
        entity.setVariants(java.util.List.of(variant));

        Product domain = mapper.toDomain(entity);

        assertThat(domain.getVariants()).hasSize(1);
        assertThat(domain.getVariants().getFirst().getSku()).isEqualTo("P011-S-WHITE");
    }

    @Test
    void toDomainVariant_shouldHandleNullNestedProduct() {
        JpaProductVariant entity = JpaProductVariant.builder()
                .id(13L)
                .product(null)
                .sku("P013")
                .build();

        ProductVariant domain = mapper.toDomainVariant(entity);

        assertThat(domain.getId()).isEqualTo(13L);
        assertThat(domain.getProductId()).isNull();
    }

    @Test
    void toEntityVariant_shouldMapVariantFields() {
        ProductVariant domain = ProductVariant.builder()
                .id(8L)
                .productId(9L)
                .sku("P002-L-RED")
                .size("L")
                .color("Red")
                .designStyle("Premium")
                .variantPriceVnd(250000L)
                .barcode("BC-2")
                .lowStockThreshold(6)
                .isActive(true)
                .build();

        JpaProductVariant entity = mapper.toEntityVariant(domain);

        assertThat(entity.getId()).isEqualTo(8L);
        assertThat(entity.getSku()).isEqualTo("P002-L-RED");
        assertThat(entity.getBarcode()).isEqualTo("BC-2");
    }

    @Test
    void shouldReturnNull_whenInputIsNull() {
        assertThat(mapper.toDomain(null)).isNull();
        assertThat(mapper.toEntity(null)).isNull();
        assertThat(mapper.toDomainVariant(null)).isNull();
        assertThat(mapper.toEntityVariant(null)).isNull();
    }
}