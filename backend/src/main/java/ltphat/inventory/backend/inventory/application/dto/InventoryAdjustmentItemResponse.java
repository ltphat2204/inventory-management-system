package ltphat.inventory.backend.inventory.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryAdjustmentItemResponse {
    private Long variantId;
    private String adjustmentType;
    private Integer quantityChange;
    private Integer previousQuantity;
    private Integer newQuantity;
    private String reason;
}
