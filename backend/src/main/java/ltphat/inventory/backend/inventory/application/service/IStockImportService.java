package ltphat.inventory.backend.inventory.application.service;

import ltphat.inventory.backend.inventory.application.dto.StockImportRequest;
import ltphat.inventory.backend.inventory.application.dto.StockImportResponse;

public interface IStockImportService {
    StockImportResponse importStock(StockImportRequest request);
}
