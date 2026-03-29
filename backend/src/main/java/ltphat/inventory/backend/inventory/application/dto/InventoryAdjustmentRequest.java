package ltphat.inventory.backend.inventory.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryAdjustmentRequest {

    @NotBlank(message = "Idempotency key is required")
    private String idempotencyKey;

    @NotEmpty(message = "Adjustment must contain at least one item")
    @Valid
    private List<InventoryAdjustmentItemRequest> items;
}
