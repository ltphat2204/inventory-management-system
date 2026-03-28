package ltphat.inventory.backend.inventory.application.service.impl;

import lombok.RequiredArgsConstructor;
import ltphat.inventory.backend.inventory.application.dto.InventoryOverviewResponse;
import ltphat.inventory.backend.inventory.application.service.IInventoryService;
import ltphat.inventory.backend.inventory.domain.model.Inventory;
import ltphat.inventory.backend.inventory.domain.model.InventoryOverview;
import ltphat.inventory.backend.inventory.domain.repository.IInventoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.JpaSort;
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

    @Override
    @Transactional(readOnly = true)
    public Page<InventoryOverviewResponse> getInventoryOverview(int page, int limit, Boolean lowStockOnly, Long productId, String sort) {
        int pageNumber = Math.max(0, page - 1);
        int pageSize = Math.max(1, limit);
        Pageable pageable = PageRequest.of(pageNumber, pageSize, resolveSort(sort));

        return inventoryRepository.findInventoryOverview(lowStockOnly, productId, pageable)
                .map(this::toInventoryOverviewResponse);
    }

    private InventoryOverviewResponse toInventoryOverviewResponse(InventoryOverview item) {
        return InventoryOverviewResponse.builder()
                .variantId(item.getVariantId())
                .variantSku(item.getVariantSku())
                .productId(item.getProductId())
                .productName(item.getProductName())
                .currentQuantity(item.getCurrentQuantity())
                .lowStockThreshold(item.getLowStockThreshold())
                .lowStock(item.getLowStock())
                .build();
    }

    private Sort resolveSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return JpaSort.unsafe(Sort.Direction.ASC, "v.id");
        }

        Sort.Direction direction = sort.startsWith("-") ? Sort.Direction.DESC : Sort.Direction.ASC;
        String requestedField = sort.startsWith("-") ? sort.substring(1) : sort;

        String expression = switch (requestedField) {
            case "currentQuantity" -> "i.currentQuantity";
            case "variantSku" -> "v.sku";
            case "productName" -> "COALESCE(p.nameVn, p.nameEn)";
            case "lowStockThreshold" -> "v.lowStockThreshold";
            default -> "v.id";
        };

        return JpaSort.unsafe(direction, expression);
    }
}
