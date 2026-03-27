package ltphat.inventory.backend.catalog.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    private Long id;
    private String productCode;
    private String nameVn;
    private String nameEn;
    private Long categoryId;
    private Long basePriceVnd;
    private BigDecimal vatRate;
    private String description;
    private Boolean isActive;
    private Long createdBy;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    
    private List<ProductVariant> variants;
}
