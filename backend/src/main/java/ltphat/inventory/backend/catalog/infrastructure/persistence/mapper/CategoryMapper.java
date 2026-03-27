package ltphat.inventory.backend.catalog.infrastructure.persistence.mapper;

import ltphat.inventory.backend.catalog.domain.model.Category;
import ltphat.inventory.backend.catalog.infrastructure.persistence.entity.JpaCategory;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    Category toDomain(JpaCategory entity);
    JpaCategory toEntity(Category domain);
    ltphat.inventory.backend.catalog.application.dto.CategoryResponse toResponse(Category domain);
}
