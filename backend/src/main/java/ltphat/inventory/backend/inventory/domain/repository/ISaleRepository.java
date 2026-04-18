package ltphat.inventory.backend.inventory.domain.repository;

import ltphat.inventory.backend.inventory.domain.model.Sale;

import java.time.ZonedDateTime;

public interface ISaleRepository {
    Sale save(Sale sale);
    boolean existsByIdempotencyKey(String idempotencyKey);
    boolean existsBySaleNumber(String saleNumber);
    Long sumTotalVndBySaleAtBetween(ZonedDateTime start, ZonedDateTime end);
    java.util.List<Sale> findAllById(java.util.List<Long> ids);
}
