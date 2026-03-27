package ltphat.inventory.backend.inventory.application.service;

import java.util.List;

public interface InventoryService {
    void initializeZeroStock(Long variantId);
    void initializeZeroStockForVariants(List<Long> variantIds);
    Integer getCurrentQuantity(Long variantId);
}
