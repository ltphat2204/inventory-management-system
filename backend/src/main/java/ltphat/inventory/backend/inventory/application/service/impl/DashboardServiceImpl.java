package ltphat.inventory.backend.inventory.application.service.impl;

import lombok.RequiredArgsConstructor;
import ltphat.inventory.backend.inventory.application.dto.DashboardResponse;
import ltphat.inventory.backend.inventory.application.dto.InventoryOverviewResponse;
import ltphat.inventory.backend.inventory.application.service.IDashboardService;
import ltphat.inventory.backend.inventory.domain.repository.IInventoryRepository;
import ltphat.inventory.backend.inventory.domain.repository.ISaleRepository;
import ltphat.inventory.backend.shared.security.CustomUserDetails;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements IDashboardService {

    private final IInventoryRepository inventoryRepository;
    private final ISaleRepository saleRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(int lowStockLimit) {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long currentUserId = userDetails.getUser().getId();

        int safeLimit = Math.max(1, lowStockLimit);
        Pageable lowStockPageable = PageRequest.of(0, safeLimit, Sort.by(Sort.Direction.ASC, "currentQuantity"));

        long totalSkuCount = inventoryRepository.countTotalSkus();
        long totalStockValueVnd = inventoryRepository.sumTotalStockValueVnd();
        long lowStockCount = inventoryRepository.countActiveLowStock(currentUserId);

        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime startOfDay = now.toLocalDate().atStartOfDay(now.getZone());
        long todaysSalesTotalVnd = saleRepository.sumTotalVndBySaleAtBetween(startOfDay, startOfDay.plusDays(1));

        var lowStockItems = inventoryRepository.findActiveLowStockOverview(currentUserId, lowStockPageable)
                .map(item -> InventoryOverviewResponse.builder()
                        .variantId(item.getVariantId())
                        .variantSku(item.getVariantSku())
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .currentQuantity(item.getCurrentQuantity())
                        .lowStockThreshold(item.getLowStockThreshold())
                        .lowStock(item.getLowStock())
                        .build())
                .getContent();

        return DashboardResponse.builder()
                .totalSkuCount(totalSkuCount)
                .totalStockValueVnd(totalStockValueVnd)
                .lowStockCount(lowStockCount)
                .todaysSalesTotalVnd(todaysSalesTotalVnd)
                .lowStockItems(lowStockItems)
                .build();
    }
}
