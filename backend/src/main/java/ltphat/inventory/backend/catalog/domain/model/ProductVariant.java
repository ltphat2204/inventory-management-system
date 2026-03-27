package ltphat.inventory.backend.catalog.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariant {
    private Long id;
    private Long productId;
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
}
