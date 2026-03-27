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
    
    // Extracted from inventory module for barcode lookup and variant matrix
    private Integer currentQuantity; 
}
