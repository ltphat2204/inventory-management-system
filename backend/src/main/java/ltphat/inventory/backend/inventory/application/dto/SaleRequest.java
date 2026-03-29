package ltphat.inventory.backend.inventory.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
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
public class SaleRequest {

    @NotBlank(message = "Idempotency key is required")
    private String idempotencyKey;

    @Min(value = 0, message = "Discount cannot be negative")
    private Long discountVnd;

    private String notes;

    @NotEmpty(message = "Sale must contain at least one item")
    @Valid
    private List<SaleItemRequest> items;
}
