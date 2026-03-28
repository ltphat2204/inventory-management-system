package ltphat.inventory.backend.inventory.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockImportItem {
    private Long id;
    private Long variantId;
    private Integer quantity;
    private Long unitCostVnd;
    private Long lineTotalVnd;
}
