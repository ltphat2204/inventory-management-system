package ltphat.inventory.backend.inventory.domain.repository;

import ltphat.inventory.backend.inventory.domain.model.Inventory;
import java.util.Optional;
import java.util.List;

public interface InventoryRepository {
    Inventory save(Inventory inventory);
    List<Inventory> saveAll(List<Inventory> inventories);
    Optional<Inventory> findByVariantId(Long variantId);
    List<Inventory> findByVariantIdIn(List<Long> variantIds);
}
