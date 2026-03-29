package ltphat.inventory.backend.inventory.application.service;

import ltphat.inventory.backend.inventory.application.dto.InventoryAdjustmentRequest;
import ltphat.inventory.backend.inventory.application.dto.InventoryAdjustmentResponse;

public interface IInventoryAdjustmentService {
    InventoryAdjustmentResponse adjustStock(InventoryAdjustmentRequest request);
}
