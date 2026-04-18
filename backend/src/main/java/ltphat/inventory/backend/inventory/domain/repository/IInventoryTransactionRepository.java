package ltphat.inventory.backend.inventory.domain.repository;

import ltphat.inventory.backend.inventory.domain.model.InventoryTransaction;

public interface IInventoryTransactionRepository {
    InventoryTransaction save(InventoryTransaction transaction);
    boolean existsByIdempotencyKey(String idempotencyKey);
    org.springframework.data.domain.Page<InventoryTransaction> findAll(
            org.springframework.data.domain.Pageable pageable, 
            Long userId, 
            java.time.ZonedDateTime start, 
            java.time.ZonedDateTime end, 
            Long variantId, 
            ltphat.inventory.backend.inventory.domain.model.MovementType movementType
    );
}
