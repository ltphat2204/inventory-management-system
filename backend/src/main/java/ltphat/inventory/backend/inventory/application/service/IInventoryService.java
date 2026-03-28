package ltphat.inventory.backend.inventory.application.service;

import ltphat.inventory.backend.inventory.application.dto.InventoryOverviewResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface IInventoryService {
    void initializeZeroStock(Long variantId);
    void initializeZeroStockForVariants(List<Long> variantIds);
    Integer getCurrentQuantity(Long variantId);
    Page<InventoryOverviewResponse> getInventoryOverview(int page, int limit, Boolean lowStockOnly, Long productId, String sort);
}
