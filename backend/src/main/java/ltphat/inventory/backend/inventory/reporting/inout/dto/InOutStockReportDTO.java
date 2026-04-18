package ltphat.inventory.backend.inventory.reporting.inout.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InOutStockReportDTO {
    private Long variantId;
    private String sku;
    private String productNameVn;
    private String productNameEn;
    private String variantDetails; // e.g. "Size: S, Color: Red"

    private Integer beginningQty;
    private Long beginningValueVnd;

    private Integer importQty;
    private Long importValueVnd;

    private Integer exportQty;
    private Long exportValueVnd;

    private Integer endingQty;
    private Long endingValueVnd;
}
