package ltphat.inventory.backend.catalog.infrastructure.persistence.entity;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class CatalogEntitiesTest {

    @Test
    void jpaCategory_onCreate_shouldSetCreatedAtWhenNull() throws Exception {
        JpaCategory category = JpaCategory.builder()
                .nameVn("Ao")
                .nameEn("Shirt")
                .description("Clothing")
                .createdAt(null)
                .build();

        Method onCreate = JpaCategory.class.getDeclaredMethod("onCreate");
        onCreate.setAccessible(true);
        onCreate.invoke(category);

        assertThat(category.getCreatedAt()).isNotNull();
    }

    @Test
    void jpaCategory_onCreate_shouldKeepCreatedAtWhenAlreadySet() throws Exception {
        ZonedDateTime initial = ZonedDateTime.now().minusDays(1);
        JpaCategory category = JpaCategory.builder()
                .nameVn("Ao")
                .createdAt(initial)
                .build();

        Method onCreate = JpaCategory.class.getDeclaredMethod("onCreate");
        onCreate.setAccessible(true);
        onCreate.invoke(category);

        assertThat(category.getCreatedAt()).isEqualTo(initial);
    }

    @Test
    void jpaProduct_onCreate_shouldSetDefaultsWhenNull() throws Exception {
        JpaProduct product = JpaProduct.builder()
                .productCode("P001")
                .nameVn("Ao")
                .basePriceVnd(100000L)
                .createdAt(null)
                .updatedAt(null)
                .vatRate(null)
                .isActive(null)
                .build();

        Method onCreate = JpaProduct.class.getDeclaredMethod("onCreate");
        onCreate.setAccessible(true);
        onCreate.invoke(product);

        assertThat(product.getCreatedAt()).isNotNull();
        assertThat(product.getUpdatedAt()).isNotNull();
        assertThat(product.getVatRate()).isEqualByComparingTo(new BigDecimal("10.00"));
        assertThat(product.getIsActive()).isTrue();
    }

    @Test
    void jpaProduct_onUpdate_shouldRefreshUpdatedAt() throws Exception {
        JpaProduct product = JpaProduct.builder()
                .productCode("P001")
                .nameVn("Ao")
                .basePriceVnd(100000L)
                .build();

        Method onUpdate = JpaProduct.class.getDeclaredMethod("onUpdate");
        onUpdate.setAccessible(true);
        onUpdate.invoke(product);

        assertThat(product.getUpdatedAt()).isNotNull();
    }

    @Test
    void jpaProduct_onCreate_shouldKeepExistingValues() throws Exception {
        ZonedDateTime created = ZonedDateTime.now().minusDays(2);
        ZonedDateTime updated = ZonedDateTime.now().minusDays(1);
        JpaProduct product = JpaProduct.builder()
                .productCode("P001")
                .nameVn("Ao")
                .basePriceVnd(100000L)
                .createdAt(created)
                .updatedAt(updated)
                .vatRate(new BigDecimal("8.00"))
                .isActive(false)
                .build();

        Method onCreate = JpaProduct.class.getDeclaredMethod("onCreate");
        onCreate.setAccessible(true);
        onCreate.invoke(product);

        assertThat(product.getCreatedAt()).isEqualTo(created);
        assertThat(product.getUpdatedAt()).isEqualTo(updated);
        assertThat(product.getVatRate()).isEqualByComparingTo(new BigDecimal("8.00"));
        assertThat(product.getIsActive()).isFalse();
    }

    @Test
    void jpaProduct_addAndRemoveVariant_shouldManageRelationship() {
        JpaProduct product = JpaProduct.builder()
                .productCode("P001")
                .nameVn("Ao")
                .basePriceVnd(100000L)
                .build();
        JpaProductVariant variant = JpaProductVariant.builder().sku("P001-S-RED").build();

        product.addVariant(variant);

        assertThat(product.getVariants()).containsExactly(variant);
        assertThat(variant.getProduct()).isEqualTo(product);

        product.removeVariant(variant);

        assertThat(product.getVariants()).isEmpty();
        assertThat(variant.getProduct()).isNull();
    }

    @Test
    void jpaProductVariant_onCreate_shouldSetDefaultsWhenNull() throws Exception {
        JpaProductVariant variant = JpaProductVariant.builder()
                .sku("P001-S-RED")
                .createdAt(null)
                .updatedAt(null)
                .lowStockThreshold(null)
                .isActive(null)
                .build();

        Method onCreate = JpaProductVariant.class.getDeclaredMethod("onCreate");
        onCreate.setAccessible(true);
        onCreate.invoke(variant);

        assertThat(variant.getCreatedAt()).isNotNull();
        assertThat(variant.getUpdatedAt()).isNotNull();
        assertThat(variant.getLowStockThreshold()).isEqualTo(10);
        assertThat(variant.getIsActive()).isTrue();
    }

    @Test
    void jpaProductVariant_onUpdate_shouldRefreshUpdatedAt() throws Exception {
        JpaProductVariant variant = JpaProductVariant.builder().sku("P001-M-BLUE").build();

        Method onUpdate = JpaProductVariant.class.getDeclaredMethod("onUpdate");
        onUpdate.setAccessible(true);
        onUpdate.invoke(variant);

        assertThat(variant.getUpdatedAt()).isNotNull();
    }

    @Test
    void jpaProductVariant_onCreate_shouldKeepExistingValues() throws Exception {
        ZonedDateTime created = ZonedDateTime.now().minusDays(3);
        ZonedDateTime updated = ZonedDateTime.now().minusDays(1);
        JpaProductVariant variant = JpaProductVariant.builder()
                .sku("P001-M-BLUE")
                .createdAt(created)
                .updatedAt(updated)
                .lowStockThreshold(2)
                .isActive(false)
                .build();

        Method onCreate = JpaProductVariant.class.getDeclaredMethod("onCreate");
        onCreate.setAccessible(true);
        onCreate.invoke(variant);

        assertThat(variant.getCreatedAt()).isEqualTo(created);
        assertThat(variant.getUpdatedAt()).isEqualTo(updated);
        assertThat(variant.getLowStockThreshold()).isEqualTo(2);
        assertThat(variant.getIsActive()).isFalse();
    }
}