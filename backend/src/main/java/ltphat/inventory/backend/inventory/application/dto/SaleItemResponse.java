package ltphat.inventory.backend.inventory.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleItemResponse {
    private Long id;
    private Long variantId;
    private Integer quantity;
    private Long unitPriceVnd;
    private BigDecimal vatRate;
    private Long vatAmountVnd;
    private Long lineTotalVnd;
}
