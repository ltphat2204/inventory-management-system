package ltphat.inventory.backend.inventory.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockImportItemResponse {
    private Long variantId;
    private Integer quantity;
    private Long unitCostVnd;
    private Long lineTotalVnd;
}
