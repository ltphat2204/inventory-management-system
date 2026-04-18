package ltphat.inventory.backend.inventory.application.service;

import lombok.RequiredArgsConstructor;
import ltphat.inventory.backend.catalog.domain.model.ProductVariant;
import ltphat.inventory.backend.catalog.domain.repository.IProductVariantRepository;
import ltphat.inventory.backend.inventory.application.dto.InventoryTransactionResponse;
import ltphat.inventory.backend.inventory.domain.model.InventoryTransaction;
import ltphat.inventory.backend.inventory.domain.model.MovementType;
import ltphat.inventory.backend.inventory.domain.repository.IInventoryTransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryTransactionServiceImpl implements IInventoryTransactionService {

    private final IInventoryTransactionRepository transactionRepository;
    private final IProductVariantRepository variantRepository;

    @Override
    public Page<InventoryTransactionResponse> getTransactions(Pageable pageable, Long userId, ZonedDateTime start, ZonedDateTime end, Long variantId, MovementType movementType) {
        Page<InventoryTransaction> transactions = transactionRepository.findAll(pageable, userId, start, end, variantId, movementType);

        List<Long> variantIds = transactions.getContent().stream()
                .map(InventoryTransaction::getVariantId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, ProductVariant> variantMap = variantRepository.findAllById(variantIds).stream()
                .collect(Collectors.toMap(ProductVariant::getId, v -> v));

        return transactions.map(t -> {
            ProductVariant variant = variantMap.get(t.getVariantId());
            return InventoryTransactionResponse.builder()
                    .id(t.getId())
                    .variantId(t.getVariantId())
                    .variantSku(variant != null ? variant.getSku() : "N/A")
                    .userId(t.getUserId())
                    .movementType(t.getMovementType())
                    .adjustmentSubtype(t.getAdjustmentSubtype())
                    .quantityChange(t.getQuantityChange())
                    .previousQuantity(t.getPreviousQuantity())
                    .newQuantity(t.getNewQuantity())
                    .unitPriceVnd(t.getUnitPriceVnd())
                    .reason(t.getReason())
                    .importId(t.getImportId())
                    .saleId(t.getSaleId())
                    .performedAt(t.getPerformedAt())
                    .build();
        });
    }
}
