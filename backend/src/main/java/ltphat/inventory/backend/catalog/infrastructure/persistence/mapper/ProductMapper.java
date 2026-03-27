package ltphat.inventory.backend.catalog.infrastructure.persistence.mapper;

import ltphat.inventory.backend.catalog.domain.model.Product;
import ltphat.inventory.backend.catalog.domain.model.ProductVariant;
import ltphat.inventory.backend.catalog.infrastructure.persistence.entity.JpaProduct;
import ltphat.inventory.backend.catalog.infrastructure.persistence.entity.JpaProductVariant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    Product toDomain(JpaProduct entity);

    @Mapping(target = "variants", ignore = true) // Will map manually or handle variants independently to avoid loops
    JpaProduct toEntity(Product domain);

    @Mapping(target = "productId", source = "product.id")
    ProductVariant toDomainVariant(JpaProductVariant entity);
    
    @Mapping(target = "product", ignore = true)
    JpaProductVariant toEntityVariant(ProductVariant domain);
}
