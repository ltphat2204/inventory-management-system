package ltphat.inventory.backend.inventory.application;

import ltphat.inventory.backend.inventory.application.dto.StockImportItemResponse;
import ltphat.inventory.backend.inventory.application.dto.StockImportResponse;
import ltphat.inventory.backend.inventory.domain.model.StockImport;
import ltphat.inventory.backend.inventory.domain.model.StockImportItem;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface InventoryApplicationMapper {
    StockImportResponse toStockImportResponse(StockImport stockImport);
    StockImportItemResponse toStockImportItemResponse(StockImportItem item);
}
