package ltphat.inventory.backend.inventory.infrastructure.persistence.mapper;

import ltphat.inventory.backend.inventory.domain.model.Sale;
import ltphat.inventory.backend.inventory.domain.model.SaleItem;
import ltphat.inventory.backend.inventory.infrastructure.persistence.entity.JpaSale;
import ltphat.inventory.backend.inventory.infrastructure.persistence.entity.JpaSaleItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SaleMapper {

    @Mapping(target = "items", source = "items")
    Sale toDomain(JpaSale entity);

    @Mapping(target = "items", source = "items")
    JpaSale toEntity(Sale domain);

    @Mapping(target = "sale", ignore = true)
    JpaSaleItem toEntity(SaleItem domain);

    SaleItem toDomain(JpaSaleItem entity);
}
