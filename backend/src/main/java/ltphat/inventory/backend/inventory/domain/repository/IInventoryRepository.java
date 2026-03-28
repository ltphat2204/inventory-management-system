package ltphat.inventory.backend.inventory.domain.repository;

import ltphat.inventory.backend.inventory.domain.model.InventoryOverview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ltphat.inventory.backend.inventory.domain.model.Inventory;
import java.util.Optional;
import java.util.List;

public interface IInventoryRepository {
    Inventory save(Inventory inventory);
    List<Inventory> saveAll(List<Inventory> inventories);
    Optional<Inventory> findByVariantId(Long variantId);
    Optional<Inventory> findByVariantIdWithLock(Long variantId);
    List<Inventory> findByVariantIdIn(List<Long> variantIds);
    Page<InventoryOverview> findInventoryOverview(Boolean lowStockOnly, Long productId, Pageable pageable);
}
