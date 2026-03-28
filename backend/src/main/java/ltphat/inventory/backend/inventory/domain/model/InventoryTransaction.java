package ltphat.inventory.backend.inventory.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryTransaction {
    private Long id;
    private Long variantId;
    private MovementType movementType;
    private Integer quantityChanged;
    private Integer previousQuantity;
    private Integer newQuantity;
    private String reason;
    private Long referenceId; // The ID of the StockImport, Sale or Adjustment
    private Long userId;
    private ZonedDateTime createdAt;
}
