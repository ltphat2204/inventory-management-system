package ltphat.inventory.backend.inventory.domain.repository;

import ltphat.inventory.backend.inventory.domain.model.InventoryTransaction;

public interface IInventoryTransactionRepository {
    InventoryTransaction save(InventoryTransaction transaction);
    boolean existsByIdempotencyKey(String idempotencyKey);
}
