package ltphat.inventory.backend.catalog.application.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

@Data
public class ProductResponse {
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
    private List<VariantResponse> variants;
}
