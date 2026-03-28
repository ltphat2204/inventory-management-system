package ltphat.inventory.backend.inventory.domain.repository;

import ltphat.inventory.backend.inventory.domain.model.StockImport;

public interface IStockImportRepository {
    StockImport save(StockImport stockImport);
    boolean existsByIdempotencyKey(String idempotencyKey);
    boolean existsByImportNumber(String importNumber);
}
