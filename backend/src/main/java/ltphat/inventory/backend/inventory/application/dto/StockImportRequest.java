package ltphat.inventory.backend.inventory.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockImportRequest {

    @NotBlank(message = "Import number is required")
    @Size(max = 50, message = "Import number must not exceed 50 characters")
    private String importNumber;

    @Size(max = 200, message = "Supplier name must not exceed 200 characters")
    private String supplierName;

    private String notes;

    @NotBlank(message = "Idempotency key is required")
    private String idempotencyKey;

    @NotEmpty(message = "Import must contain at least one item")
    @Valid
    private List<StockImportItemRequest> items;
}
