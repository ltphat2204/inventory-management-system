package ltphat.inventory.backend.inventory.presentation.controller;

import lombok.RequiredArgsConstructor;
import ltphat.inventory.backend.inventory.application.dto.InventoryOverviewResponse;
import ltphat.inventory.backend.inventory.application.service.IInventoryService;
import ltphat.inventory.backend.shared.api.response.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final IInventoryService inventoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<InventoryOverviewResponse>>> getInventoryOverview(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) Boolean lowStockOnly,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) String sort) {
        Page<InventoryOverviewResponse> response = inventoryService.getInventoryOverview(page, limit, lowStockOnly, productId, sort);
        return ResponseEntity.ok(ApiResponse.success("Success", response));
    }
}
