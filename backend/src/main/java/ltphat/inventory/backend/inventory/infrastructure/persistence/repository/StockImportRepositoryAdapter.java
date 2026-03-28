package ltphat.inventory.backend.inventory.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import ltphat.inventory.backend.inventory.domain.model.StockImport;
import ltphat.inventory.backend.inventory.domain.repository.IStockImportRepository;
import ltphat.inventory.backend.inventory.infrastructure.persistence.entity.JpaStockImport;
import ltphat.inventory.backend.inventory.infrastructure.persistence.mapper.StockImportMapper;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class StockImportRepositoryAdapter implements IStockImportRepository {

    private final SpringDataStockImportRepository springDataRepository;
    private final StockImportMapper mapper;

    @Override
    public StockImport save(StockImport stockImport) {
        JpaStockImport entity = mapper.toEntity(stockImport);
        // Map back references for items
        if (entity.getItems() != null) {
            entity.getItems().forEach(item -> item.setStockImport(entity));
        }
        return mapper.toDomain(springDataRepository.save(entity));
    }

    @Override
    public boolean existsByIdempotencyKey(String idempotencyKey) {
        return springDataRepository.existsByIdempotencyKey(idempotencyKey);
    }

    @Override
    public boolean existsByImportNumber(String importNumber) {
        return springDataRepository.existsByImportNumber(importNumber);
    }
}
