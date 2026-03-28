package ltphat.inventory.backend.inventory.infrastructure.persistence.mapper;

import ltphat.inventory.backend.inventory.domain.model.StockImport;
import ltphat.inventory.backend.inventory.domain.model.StockImportItem;
import ltphat.inventory.backend.inventory.infrastructure.persistence.entity.JpaStockImport;
import ltphat.inventory.backend.inventory.infrastructure.persistence.entity.JpaStockImportItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface StockImportMapper {
    
    @Mapping(target = "items", source = "items")
    StockImport toDomain(JpaStockImport entity);

    @Mapping(target = "items", source = "items")
    JpaStockImport toEntity(StockImport domain);

    @Mapping(target = "stockImport", ignore = true)
    JpaStockImportItem toEntity(StockImportItem domain);

    StockImportItem toDomain(JpaStockImportItem entity);
}
