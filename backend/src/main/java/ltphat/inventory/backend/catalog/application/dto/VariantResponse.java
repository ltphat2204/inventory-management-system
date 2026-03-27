package ltphat.inventory.backend.catalog.application.dto;

import lombok.Data;
import java.time.ZonedDateTime;

@Data
public class VariantResponse {
    private Long id;
    private String sku;
    private String size;
    private String color;
    private String designStyle;
    private Long variantPriceVnd;
    private String barcode;
    private Integer lowStockThreshold;
    private Boolean isActive;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    // Stock fields populated from the inventory module
    private Integer currentQuantity;
    private Boolean lowStock;

    // Populated only by the barcode lookup path
    private String productNameVn;
}
