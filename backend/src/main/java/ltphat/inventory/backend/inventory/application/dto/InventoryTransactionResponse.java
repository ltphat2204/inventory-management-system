package ltphat.inventory.backend.inventory.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ltphat.inventory.backend.inventory.domain.model.MovementType;

import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryTransactionResponse {
    private Long id;
    private Long variantId;
    private String variantSku;
    private Long userId;
    private MovementType movementType;
    private String adjustmentSubtype;
    private Integer quantityChange;
    private Integer previousQuantity;
    private Integer newQuantity;
    private Long unitPriceVnd;
    private String reason;
    private Long importId;
    private Long saleId;
    private ZonedDateTime performedAt;
}
