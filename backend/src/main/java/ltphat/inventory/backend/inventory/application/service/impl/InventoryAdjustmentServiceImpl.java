package ltphat.inventory.backend.inventory.application.service.impl;

import lombok.RequiredArgsConstructor;
import ltphat.inventory.backend.inventory.application.dto.InventoryAdjustmentItemRequest;
import ltphat.inventory.backend.inventory.application.dto.InventoryAdjustmentItemResponse;
import ltphat.inventory.backend.inventory.application.dto.InventoryAdjustmentRequest;
import ltphat.inventory.backend.inventory.application.dto.InventoryAdjustmentResponse;
import ltphat.inventory.backend.inventory.application.service.IInventoryAdjustmentService;
import ltphat.inventory.backend.inventory.domain.exception.InsufficientStockException;
import ltphat.inventory.backend.inventory.domain.exception.InventoryNotFoundException;
import ltphat.inventory.backend.inventory.domain.model.Inventory;
import ltphat.inventory.backend.inventory.domain.model.InventoryTransaction;
import ltphat.inventory.backend.inventory.domain.model.MovementType;
import ltphat.inventory.backend.inventory.domain.repository.IInventoryRepository;
import ltphat.inventory.backend.inventory.domain.repository.IInventoryTransactionRepository;
import ltphat.inventory.backend.shared.api.exception.IdempotencyConflictException;
import ltphat.inventory.backend.shared.security.CustomUserDetails;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class InventoryAdjustmentServiceImpl implements IInventoryAdjustmentService {

    private final IInventoryRepository inventoryRepository;
    private final IInventoryTransactionRepository inventoryTransactionRepository;

    @Override
    @Transactional
    public InventoryAdjustmentResponse adjustStock(InventoryAdjustmentRequest request) {
        if (inventoryTransactionRepository.existsByIdempotencyKey(request.getIdempotencyKey())) {
            throw new IdempotencyConflictException("Duplicate idempotency key: " + request.getIdempotencyKey());
        }

        validateUniqueVariants(request.getItems());

        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long currentUserId = userDetails.getUser().getId();
        ZonedDateTime now = ZonedDateTime.now();

        List<InventoryAdjustmentItemRequest> orderedItems = request.getItems().stream()
                .sorted(Comparator.comparing(InventoryAdjustmentItemRequest::getVariantId))
                .toList();

        List<InventoryAdjustmentItemResponse> responseItems = new ArrayList<>();

        for (int i = 0; i < orderedItems.size(); i++) {
            InventoryAdjustmentItemRequest item = orderedItems.get(i);
            if (item.getQuantityChange() == 0) {
                throw new IllegalArgumentException("Quantity change must not be zero for variant ID: " + item.getVariantId());
            }

            Inventory inventory = inventoryRepository.findByVariantIdWithLock(item.getVariantId())
                    .orElseThrow(() -> new InventoryNotFoundException("Inventory record not found for variant ID: " + item.getVariantId()));

            Integer previousQuantity = inventory.getCurrentQuantity();
            Integer newQuantity = previousQuantity + item.getQuantityChange();
            if (newQuantity < 0) {
                throw new InsufficientStockException("Adjustment would make stock negative for variant ID: " + item.getVariantId());
            }

            inventory.setCurrentQuantity(newQuantity);
            inventory.setUpdatedAt(now);
            inventoryRepository.save(inventory);

            String idempotencyKey = i == 0
                    ? request.getIdempotencyKey()
                    : request.getIdempotencyKey() + "#" + i;

            InventoryTransaction transaction = InventoryTransaction.builder()
                    .variantId(item.getVariantId())
                    .movementType(MovementType.ADJUSTMENT)
                    .adjustmentSubtype(item.getAdjustmentType().name())
                    .quantityChange(item.getQuantityChange())
                    .idempotencyKey(idempotencyKey)
                    .performedAt(now)
                    .quantityChanged(item.getQuantityChange())
                    .previousQuantity(previousQuantity)
                    .newQuantity(newQuantity)
                    .reason(item.getReason().trim())
                    .referenceId(item.getVariantId())
                    .userId(currentUserId)
                    .createdAt(now)
                    .build();
            inventoryTransactionRepository.save(transaction);

            responseItems.add(InventoryAdjustmentItemResponse.builder()
                    .variantId(item.getVariantId())
                    .adjustmentType(item.getAdjustmentType().name())
                    .quantityChange(item.getQuantityChange())
                    .previousQuantity(previousQuantity)
                    .newQuantity(newQuantity)
                    .reason(item.getReason().trim())
                    .build());
        }

        return InventoryAdjustmentResponse.builder()
                .idempotencyKey(request.getIdempotencyKey())
                .adjustedAt(now)
                .items(responseItems)
                .build();
    }

    private void validateUniqueVariants(List<InventoryAdjustmentItemRequest> items) {
        Set<Long> uniqueIds = new HashSet<>();
        for (InventoryAdjustmentItemRequest item : items) {
            if (!uniqueIds.add(item.getVariantId())) {
                throw new IllegalArgumentException("Duplicate variant ID in adjustment items: " + item.getVariantId());
            }
        }
    }
}
