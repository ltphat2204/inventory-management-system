package ltphat.inventory.backend.catalog.application;

import ltphat.inventory.backend.catalog.application.dto.CategoryResponse;
import ltphat.inventory.backend.catalog.application.dto.ProductResponse;
import ltphat.inventory.backend.catalog.application.dto.VariantResponse;
import ltphat.inventory.backend.catalog.domain.model.Category;
import ltphat.inventory.backend.catalog.domain.model.Product;
import ltphat.inventory.backend.catalog.domain.model.ProductVariant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CatalogApplicationMapper {

    CategoryResponse toCategoryResponse(Category category);

    @Mapping(target = "variants", ignore = true)
    @Mapping(target = "variantCount", expression = "java(product.getVariants() != null ? product.getVariants().size() : 0)")
    ProductResponse toProductSummaryResponse(Product product);

    @Mapping(target = "variantCount", expression = "java(product.getVariants() != null ? product.getVariants().size() : 0)")
    ProductResponse toProductDetailResponse(Product product);

    @Mapping(target = "currentQuantity", ignore = true)
    @Mapping(target = "lowStock", ignore = true)
    @Mapping(target = "productNameVn", ignore = true)
    VariantResponse toVariantResponse(ProductVariant variant);
}
