package ltphat.inventory.backend.inventory.domain.repository;

import ltphat.inventory.backend.inventory.domain.model.Sale;

public interface ISaleRepository {
    Sale save(Sale sale);
    boolean existsByIdempotencyKey(String idempotencyKey);
    boolean existsBySaleNumber(String saleNumber);
}
