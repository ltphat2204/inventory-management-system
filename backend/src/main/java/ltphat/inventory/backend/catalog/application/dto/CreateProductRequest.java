package ltphat.inventory.backend.catalog.application.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateProductRequest {
    @NotBlank
    private String productCode;

    @NotBlank
    private String nameVn;

    private String nameEn;
    private Long categoryId;

    @NotNull
    @Min(0)
    private Long basePriceVnd;

    private BigDecimal vatRate = new BigDecimal("10.00");
    private String description;

    @NotNull
    private List<VariantDto> variants;
}
