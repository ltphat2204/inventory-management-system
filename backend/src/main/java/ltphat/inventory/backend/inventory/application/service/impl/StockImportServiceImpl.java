package ltphat.inventory.backend.inventory.application.service.impl;

import lombok.RequiredArgsConstructor;
import ltphat.inventory.backend.inventory.application.InventoryApplicationMapper;
import ltphat.inventory.backend.inventory.application.dto.StockImportRequest;
import ltphat.inventory.backend.inventory.application.dto.StockImportResponse;
import ltphat.inventory.backend.inventory.application.service.IStockImportService;
import ltphat.inventory.backend.inventory.domain.exception.InventoryNotFoundException;
import ltphat.inventory.backend.inventory.domain.model.Inventory;
import ltphat.inventory.backend.inventory.domain.model.InventoryTransaction;
import ltphat.inventory.backend.inventory.domain.model.MovementType;
import ltphat.inventory.backend.inventory.domain.model.StockImport;
import ltphat.inventory.backend.inventory.domain.model.StockImportItem;
import ltphat.inventory.backend.inventory.domain.repository.IInventoryRepository;
import ltphat.inventory.backend.inventory.domain.repository.IInventoryTransactionRepository;
import ltphat.inventory.backend.inventory.domain.repository.IStockImportRepository;
import ltphat.inventory.backend.shared.api.exception.IdempotencyConflictException;
import ltphat.inventory.backend.shared.security.CustomUserDetails;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockImportServiceImpl implements IStockImportService {

    private final IStockImportRepository stockImportRepository;
    private final IInventoryRepository inventoryRepository;
    private final IInventoryTransactionRepository inventoryTransactionRepository;
        private final InventoryApplicationMapper inventoryApplicationMapper;

    @Override
    @Transactional
    public StockImportResponse importStock(StockImportRequest request) {
        // 1. Idempotency Check
        if (stockImportRepository.existsByIdempotencyKey(request.getIdempotencyKey())) {
            throw new IdempotencyConflictException("Duplicate idempotency key: " + request.getIdempotencyKey());
        }

        // 2. Unique Import Number Check
        if (stockImportRepository.existsByImportNumber(request.getImportNumber())) {
            throw new IllegalArgumentException("Import number already exists: " + request.getImportNumber());
        }

        // 3. Resolve Current user_id
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long currentUserId = userDetails.getUser().getId();

        ZonedDateTime now = ZonedDateTime.now();
        List<StockImportItem> importItems = new ArrayList<>();
        
        // 4. Create StockImport header first (or build it)
        StockImport stockImport = StockImport.builder()
                .importNumber(request.getImportNumber())
                .supplierName(request.getSupplierName())
                .userId(currentUserId)
                .notes(request.getNotes())
                .idempotencyKey(request.getIdempotencyKey())
                .importDate(now)
                .createdAt(now)
                .build();

        // Save later after processing items to get the reference ID for transactions
        StockImport savedImport = stockImportRepository.save(stockImport);

        // 5. Process Items with Pessimistic Locking
        for (var itemReq : request.getItems()) {
            // Pessimistic Lock the inventory row
            Inventory inventory = inventoryRepository.findByVariantIdWithLock(itemReq.getVariantId())
                    .orElseThrow(() -> new InventoryNotFoundException("Inventory record not found for variant ID: " + itemReq.getVariantId()));

            Integer prevQty = inventory.getCurrentQuantity();
            Long prevTotalValue = inventory.getTotalValueVnd();
            
            // Calculate new values
            Integer importedQty = itemReq.getQuantity();
            Long unitCost = itemReq.getUnitCostVnd();
            Long lineTotal = importedQty.longValue() * unitCost;
            
            Integer newQty = prevQty + importedQty;
            Long newTotalValue = prevTotalValue + lineTotal;

            // Update Inventory
            inventory.setCurrentQuantity(newQty);
            inventory.setTotalValueVnd(newTotalValue);
            inventory.setUpdatedAt(now);
            inventoryRepository.save(inventory);

            // 6. Create Inventory Transaction (Audit)
            InventoryTransaction transaction = InventoryTransaction.builder()
                    .variantId(itemReq.getVariantId())
                    .movementType(MovementType.IMPORT)
                    .quantityChange(importedQty)
                    .unitPriceVnd(unitCost)
                    .importId(savedImport.getId())
                    .idempotencyKey(request.getIdempotencyKey())
                    .performedAt(now)
                    .quantityChanged(importedQty)
                    .previousQuantity(prevQty)
                    .newQuantity(newQty)
                    .reason(itemReq.getReason() != null ? itemReq.getReason() : "Stock Import: " + savedImport.getImportNumber())
                    .referenceId(savedImport.getId())
                    .userId(currentUserId)
                    .createdAt(now)
                    .build();
            inventoryTransactionRepository.save(transaction);

            // Add to import record
            importItems.add(StockImportItem.builder()
                    .variantId(itemReq.getVariantId())
                    .quantity(importedQty)
                    .unitCostVnd(unitCost)
                    .lineTotalVnd(lineTotal)
                    .build());
        }

        savedImport.setItems(importItems);
        stockImportRepository.save(savedImport); // Cascade update items

        return inventoryApplicationMapper.toStockImportResponse(savedImport);
    }
}
