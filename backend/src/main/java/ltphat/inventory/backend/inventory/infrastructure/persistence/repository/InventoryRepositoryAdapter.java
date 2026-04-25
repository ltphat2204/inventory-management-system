package ltphat.inventory.backend.inventory.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import ltphat.inventory.backend.inventory.domain.model.Inventory;
import ltphat.inventory.backend.inventory.domain.model.InventoryOverview;
import ltphat.inventory.backend.inventory.domain.repository.IInventoryRepository;
import ltphat.inventory.backend.inventory.infrastructure.persistence.entity.JpaInventory;
import ltphat.inventory.backend.inventory.infrastructure.persistence.mapper.InventoryMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class InventoryRepositoryAdapter implements IInventoryRepository {

    private final SpringDataInventoryRepository springDataRepository;
    private final InventoryMapper mapper;

    @Override
    public Inventory save(Inventory inventory) {
        JpaInventory entity = mapper.toEntity(inventory);
        return mapper.toDomain(springDataRepository.save(entity));
    }

    @Override
    public List<Inventory> saveAll(List<Inventory> inventories) {
        List<JpaInventory> entities = inventories.stream()
                .map(mapper::toEntity)
                .collect(Collectors.toList());
        return springDataRepository.saveAll(entities).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Inventory> findByVariantId(Long variantId) {
        return springDataRepository.findByVariantId(variantId).map(mapper::toDomain);
    }

    @Override
    public Optional<Inventory> findByVariantIdWithLock(Long variantId) {
        return springDataRepository.findByVariantIdWithLock(variantId).map(mapper::toDomain);
    }

    @Override
    public List<Inventory> findByVariantIdIn(List<Long> variantIds) {
        return springDataRepository.findByVariantIdIn(variantIds).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Page<InventoryOverview> findInventoryOverview(Boolean lowStockOnly, Long productId, Pageable pageable) {
        return springDataRepository.findInventoryOverview(lowStockOnly, productId, pageable)
                .map(projection -> {
                    Integer threshold = projection.getLowStockThreshold() == null ? 0 : projection.getLowStockThreshold();
                    Integer quantity = projection.getCurrentQuantity() == null ? 0 : projection.getCurrentQuantity();
                    return InventoryOverview.builder()
                            .variantId(projection.getVariantId())
                            .variantSku(projection.getVariantSku())
                            .productId(projection.getProductId())
                            .productName(projection.getProductName())
                            .currentQuantity(projection.getCurrentQuantity())
                            .lowStockThreshold(projection.getLowStockThreshold())
                            .lowStock(quantity <= threshold)
                            .build();
                });
    }

    @Override
    public Page<InventoryOverview> findActiveLowStockOverview(Long userId, Pageable pageable) {
        return springDataRepository.findActiveLowStockOverview(userId, pageable)
                .map(projection -> {
                    Integer threshold = projection.getLowStockThreshold() == null ? 0 : projection.getLowStockThreshold();
                    Integer quantity = projection.getCurrentQuantity() == null ? 0 : projection.getCurrentQuantity();
                    return InventoryOverview.builder()
                            .variantId(projection.getVariantId())
                            .variantSku(projection.getVariantSku())
                            .productId(projection.getProductId())
                            .productName(projection.getProductName())
                            .currentQuantity(projection.getCurrentQuantity())
                            .lowStockThreshold(projection.getLowStockThreshold())
                            .lowStock(quantity <= threshold)
                            .build();
                });
    }

    @Override
    public long countActiveLowStock(Long userId) {
        return springDataRepository.countActiveLowStock(userId);
    }

    @Override
    public long countTotalSkus() {
        return springDataRepository.countTotalSkus();
    }

    @Override
    public long sumTotalStockValueVnd() {
        return springDataRepository.sumTotalStockValueVnd();
    }

    @Override
    public boolean isVariantLowStock(Long variantId) {
        return springDataRepository.isVariantLowStock(variantId);
    }

    @Override
    public List<ltphat.inventory.backend.inventory.application.dto.SlowMovingItemResponse> findSlowMovingProducts(java.time.ZonedDateTime thresholdDate, Pageable pageable) {
        return springDataRepository.findSlowMovingProducts(thresholdDate, pageable).stream()
                .map(projection -> ltphat.inventory.backend.inventory.application.dto.SlowMovingItemResponse.builder()
                        .variantId(projection.getVariantId())
                        .sku(projection.getVariantSku())
                        .productName(projection.getProductName())
                        .currentQuantity(projection.getCurrentQuantity())
                        .lastMovementAt(projection.getLastMovementAt())
                        .build())
                .collect(Collectors.toList());
    }
}
