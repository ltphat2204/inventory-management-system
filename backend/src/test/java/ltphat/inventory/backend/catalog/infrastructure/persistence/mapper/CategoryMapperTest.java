package ltphat.inventory.backend.catalog.infrastructure.persistence.mapper;

import ltphat.inventory.backend.catalog.domain.model.Category;
import ltphat.inventory.backend.catalog.infrastructure.persistence.entity.JpaCategory;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class CategoryMapperTest {

    private final CategoryMapper mapper = new CategoryMapperImpl();

    @Test
    void toDomain_shouldMapFields() {
        ZonedDateTime now = ZonedDateTime.now();
        JpaCategory entity = JpaCategory.builder()
                .id(1L)
                .nameVn("Ao")
                .nameEn("Shirt")
                .description("Clothing")
                .createdAt(now)
                .build();

        Category domain = mapper.toDomain(entity);

        assertThat(domain.getId()).isEqualTo(1L);
        assertThat(domain.getNameVn()).isEqualTo("Ao");
        assertThat(domain.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void toEntity_shouldMapFields() {
        ZonedDateTime now = ZonedDateTime.now();
        Category domain = Category.builder()
                .id(2L)
                .nameVn("Quan")
                .nameEn("Pants")
                .description("Bottom")
                .createdAt(now)
                .build();

        JpaCategory entity = mapper.toEntity(domain);

        assertThat(entity.getId()).isEqualTo(2L);
        assertThat(entity.getNameEn()).isEqualTo("Pants");
        assertThat(entity.getDescription()).isEqualTo("Bottom");
    }

    @Test
    void shouldReturnNull_whenInputIsNull() {
        assertThat(mapper.toDomain(null)).isNull();
        assertThat(mapper.toEntity(null)).isNull();
    }
}