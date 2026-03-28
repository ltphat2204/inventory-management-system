package ltphat.inventory.backend.inventory.application.service.impl;

import lombok.RequiredArgsConstructor;
import ltphat.inventory.backend.inventory.application.service.IInventoryService;
import ltphat.inventory.backend.inventory.domain.model.Inventory;
import ltphat.inventory.backend.inventory.domain.repository.IInventoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements IInventoryService {

    private final IInventoryRepository inventoryRepository;

    @Override
    @Transactional
    public void initializeZeroStock(Long variantId) {
        if (inventoryRepository.findByVariantId(variantId).isEmpty()) {
            inventoryRepository.save(Inventory.builder()
                    .variantId(variantId)
                    .currentQuantity(0)
                    .totalValueVnd(0L)
                    .createdAt(ZonedDateTime.now())
                    .updatedAt(ZonedDateTime.now())
                    .build());
        }
    }

    @Override
    @Transactional
    public void initializeZeroStockForVariants(List<Long> variantIds) {
        ZonedDateTime now = ZonedDateTime.now();
        List<Inventory> newInventories = variantIds.stream()
                .filter(id -> inventoryRepository.findByVariantId(id).isEmpty())
                .map(id -> Inventory.builder()
                        .variantId(id)
                        .currentQuantity(0)
                        .totalValueVnd(0L)
                        .createdAt(now)
                        .updatedAt(now)
                        .build())
                .collect(Collectors.toList());

        if (!newInventories.isEmpty()) {
            inventoryRepository.saveAll(newInventories);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getCurrentQuantity(Long variantId) {
        return inventoryRepository.findByVariantId(variantId)
                .map(Inventory::getCurrentQuantity)
                .orElse(0);
    }
}
