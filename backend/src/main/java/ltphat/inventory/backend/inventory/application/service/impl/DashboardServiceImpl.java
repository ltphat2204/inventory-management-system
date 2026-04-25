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
        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
            throw new org.springframework.security.authentication.InsufficientAuthenticationException("User must be authenticated to view dashboard");
        }
        Long currentUserId = userDetails.getUser().getId();

        int safeLimit = Math.max(1, lowStockLimit);
        Pageable lowStockPageable = PageRequest.of(0, safeLimit, Sort.by(Sort.Direction.ASC, "currentQuantity"));

        long totalSkuCount = inventoryRepository.countTotalSkus();
        long totalStockValueVnd = inventoryRepository.sumTotalStockValueVnd();
        long lowStockCount = inventoryRepository.countActiveLowStock(currentUserId);

        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime startOfDay = now.toLocalDate().atStartOfDay(now.getZone());
        ZonedDateTime endOfDay = startOfDay.plusDays(1);
        
        long todaysSalesTotalVnd = saleRepository.sumTotalVndBySaleAtBetween(startOfDay, endOfDay);
        long todaysSalesCount = saleRepository.countBySaleAtBetween(startOfDay, endOfDay);

        ZonedDateTime slowMovingThreshold = now.minusDays(90);
        Pageable slowMovingPageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.ASC, "currentQuantity"));
        var slowMovingItems = inventoryRepository.findSlowMovingProducts(slowMovingThreshold, slowMovingPageable);

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
                .todaysSalesCount(todaysSalesCount)
                .lowStockItems(lowStockItems)
                .slowMovingItems(slowMovingItems)
                .build();
    }
}
