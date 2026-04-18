package ltphat.inventory.backend.inventory.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import ltphat.inventory.backend.inventory.domain.model.Sale;
import ltphat.inventory.backend.inventory.domain.repository.ISaleRepository;
import ltphat.inventory.backend.inventory.infrastructure.persistence.entity.JpaSale;
import ltphat.inventory.backend.inventory.infrastructure.persistence.mapper.SaleMapper;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;

@Repository
@RequiredArgsConstructor
public class SaleRepositoryAdapter implements ISaleRepository {

    private final SpringDataSaleRepository springDataRepository;
    private final SaleMapper mapper;

    @Override
    public Sale save(Sale sale) {
        JpaSale entity = mapper.toEntity(sale);
        if (entity.getItems() != null) {
            entity.getItems().forEach(item -> item.setSale(entity));
        }
        return mapper.toDomain(springDataRepository.save(entity));
    }

    @Override
    public boolean existsByIdempotencyKey(String idempotencyKey) {
        return springDataRepository.existsByIdempotencyKey(idempotencyKey);
    }

    @Override
    public boolean existsBySaleNumber(String saleNumber) {
        return springDataRepository.existsBySaleNumber(saleNumber);
    }

    @Override
    public Long sumTotalVndBySaleAtBetween(ZonedDateTime start, ZonedDateTime end) {
        return springDataRepository.sumTotalVndBySaleAtBetween(start, end);
    }

    @Override
    public java.util.List<Sale> findAllById(java.util.List<Long> ids) {
        return springDataRepository.findAllById(ids).stream()
                .map(mapper::toDomain)
                .collect(java.util.stream.Collectors.toList());
    }
}
