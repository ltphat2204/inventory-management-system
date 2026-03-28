package ltphat.inventory.backend.inventory.domain.repository;

import ltphat.inventory.backend.inventory.domain.model.InventoryTransaction;

public interface InventoryTransactionRepository {
    InventoryTransaction save(InventoryTransaction transaction);
}
