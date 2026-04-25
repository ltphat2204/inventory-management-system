package ltphat.inventory.backend.inventory.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryOverview {
    private Long variantId;
    private String variantSku;
    private Long productId;
    private String productName;
    private Integer currentQuantity;
    private Integer lowStockThreshold;
    private Boolean lowStock;
    private Long priceVnd;
}
