package ltphat.inventory.backend.catalog.application.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VariantDto {
    @NotBlank
    private String size;

    @NotBlank
    private String color;

    private String designStyle;

    @Min(0)
    private Long variantPriceVnd;

    private String barcode;

    @Min(0)
    private Integer lowStockThreshold = 10;
}
