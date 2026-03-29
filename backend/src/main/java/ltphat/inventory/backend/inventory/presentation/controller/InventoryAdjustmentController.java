package ltphat.inventory.backend.inventory.presentation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ltphat.inventory.backend.inventory.application.dto.InventoryAdjustmentRequest;
import ltphat.inventory.backend.inventory.application.dto.InventoryAdjustmentResponse;
import ltphat.inventory.backend.inventory.application.service.IInventoryAdjustmentService;
import ltphat.inventory.backend.shared.api.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/inventory-adjustments")
@RequiredArgsConstructor
public class InventoryAdjustmentController {

    private final IInventoryAdjustmentService inventoryAdjustmentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<InventoryAdjustmentResponse>> adjustStock(@Valid @RequestBody InventoryAdjustmentRequest request) {
        InventoryAdjustmentResponse response = inventoryAdjustmentService.adjustStock(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Inventory adjusted successfully", response));
    }
}
