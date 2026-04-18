package ltphat.inventory.backend.inventory.presentation.controller;

import lombok.RequiredArgsConstructor;
import ltphat.inventory.backend.inventory.application.dto.InventoryTransactionResponse;
import ltphat.inventory.backend.inventory.application.service.IInventoryTransactionService;
import ltphat.inventory.backend.inventory.domain.model.MovementType;
import ltphat.inventory.backend.shared.api.response.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZonedDateTime;

@RestController
@RequestMapping("/api/v1/inventory-transactions")
@RequiredArgsConstructor
public class InventoryTransactionController {

    private final IInventoryTransactionService transactionService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getTransactions(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) ZonedDateTime dateFrom,
            @RequestParam(required = false) ZonedDateTime dateTo,
            @RequestParam(required = false) Long variantId,
            @RequestParam(required = false) MovementType movementType
    ) {
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by("performedAt").descending());
        Page<InventoryTransactionResponse> data = transactionService.getTransactions(pageable, userId, dateFrom, dateTo, variantId, movementType);
        return ResponseEntity.ok(ApiResponse.success("Inventory transactions retrieved", data));
    }
}
