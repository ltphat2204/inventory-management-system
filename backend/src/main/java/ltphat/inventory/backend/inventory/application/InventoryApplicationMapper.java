package ltphat.inventory.backend.inventory.application;

import ltphat.inventory.backend.inventory.application.dto.StockImportItemResponse;
import ltphat.inventory.backend.inventory.application.dto.StockImportResponse;
import ltphat.inventory.backend.inventory.application.dto.SaleItemResponse;
import ltphat.inventory.backend.inventory.application.dto.SaleResponse;
import ltphat.inventory.backend.inventory.domain.model.Sale;
import ltphat.inventory.backend.inventory.domain.model.SaleItem;
import ltphat.inventory.backend.inventory.domain.model.StockImport;
import ltphat.inventory.backend.inventory.domain.model.StockImportItem;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface InventoryApplicationMapper {
    StockImportResponse toStockImportResponse(StockImport stockImport);
    StockImportItemResponse toStockImportItemResponse(StockImportItem item);
    SaleResponse toSaleResponse(Sale sale);
    SaleItemResponse toSaleItemResponse(SaleItem item);
}
